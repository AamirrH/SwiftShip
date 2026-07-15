import { CreditCard, Minus, Plus, Trash2 } from "lucide-react";
import { useMemo, useState } from "react";
import { Button } from "../components/ui/Button.jsx";
import { Card } from "../components/ui/Card.jsx";
import { useApiResource } from "../hooks/useApiResource.js";
import { api } from "../lib/api.js";

const DEFAULT_CUSTOMER_ID = 7;
const fallbackAddresses = [
  {
    id: 3,
    label: "Work",
    addressLine: "EON Free Zone, Kharadi",
    city: "Pune",
    state: "Maharashtra",
    pincode: "411014",
    lat: 18.5515,
    lng: 73.9349,
    defaultAddress: true,
  },
  {
    id: 1,
    label: "Home",
    addressLine: "Flat 101, North Main Road, Koregaon Park",
    city: "Pune",
    state: "Maharashtra",
    pincode: "411001",
    lat: 18.5362,
    lng: 73.8939,
    defaultAddress: false,
  },
];

export function CartPage({ cart, onNavigate, onOrderPlaced, setCart }) {
  const [checkoutStatus, setCheckoutStatus] = useState("idle");
  const [checkoutError, setCheckoutError] = useState("");
  const { data: addresses, status: addressStatus } = useApiResource(
    () => api.getCustomerAddresses(DEFAULT_CUSTOMER_ID),
    fallbackAddresses,
    []
  );
  const subtotal = cart.reduce((sum, item) => sum + item.price * item.quantity, 0);
  const handling = cart.length ? 199 : 0;
  const total = subtotal + handling;
  const sortedAddresses = useMemo(
    () => [...addresses].sort((a, b) => Number(Boolean(b.defaultAddress)) - Number(Boolean(a.defaultAddress))),
    [addresses]
  );

  function updateQuantity(id, nextQuantity) {
    setCart((items) =>
      items.flatMap((item) => (item.id === id ? (nextQuantity > 0 ? [{ ...item, quantity: nextQuantity }] : []) : [item]))
    );
  }

  async function placeOrder(event) {
    event.preventDefault();
    if (cart.length === 0) return;

    const form = new FormData(event.currentTarget);
    const selectedAddressId = Number(form.get("customerAddressId"));
    const selectedAddress =
      sortedAddresses.find((address) => address.id === selectedAddressId) ?? sortedAddresses[0];
    const payload = {
      customerId: DEFAULT_CUSTOMER_ID,
      customerAddressId: selectedAddress.id,
      deliveryAddress: formatAddress(selectedAddress),
      deliveryLat: selectedAddress.lat,
      deliveryLng: selectedAddress.lng,
      items: cart.map((item) => ({
        productId: item.productId,
        quantity: item.quantity,
      })),
    };

    setCheckoutStatus("submitting");
    setCheckoutError("");

    try {
      const createdOrder = await api.createOrder(payload);
      setCheckoutStatus("success");
      setCart([]);
      onOrderPlaced?.(`Order #${createdOrder.id} has been placed successfully`);
      onNavigate("orders");
    } catch (error) {
      setCheckoutStatus("offline");
      if (error.status === 401) {
        setCheckoutError("Please login again before placing the order.");
      } else if (error.status === 403) {
        setCheckoutError("Your account is not allowed to place this order.");
      } else if (error.status === 503) {
        setCheckoutError("Checkout is temporarily unavailable. Please try again in a moment.");
      } else if (error.status) {
        setCheckoutError("We could not place this order right now. Please review your cart and try again.");
      } else {
        setCheckoutError("SwiftShip is having trouble connecting right now. Please try again.");
      }
    }
  }

  return (
    <section className="page">
      <div className="page-header">
        <div>
          <span className="label-caps">Checkout</span>
          <h1 className="page-title">Your cart</h1>
        </div>
        <Button variant="secondary" onClick={() => onNavigate("catalog")}>Continue shopping</Button>
      </div>

      <div className="split">
        <Card padded={false}>
          {cart.length === 0 && <div className="card-pad muted">Your cart is empty.</div>}
          {cart.map((item) => (
            <div className="cart-row" key={item.id}>
              <div className="thumb">
                <img src={item.image} alt={item.name} />
              </div>
              <div>
                <h3 className="section-title" style={{ fontSize: 18, margin: 0 }}>{item.name}</h3>
                <div className="muted">Rs. {item.price.toLocaleString("en-IN")} each</div>
                <div style={{ display: "flex", gap: 8, marginTop: 12 }}>
                  <button className="icon-button" onClick={() => updateQuantity(item.id, item.quantity - 1)} title="Decrease">
                    <Minus size={16} />
                  </button>
                  <input className="input" readOnly style={{ textAlign: "center", width: 68 }} value={item.quantity} />
                  <button className="icon-button" onClick={() => updateQuantity(item.id, item.quantity + 1)} title="Increase">
                    <Plus size={16} />
                  </button>
                </div>
              </div>
              <div className="row-action" style={{ textAlign: "right" }}>
                <div className="price">Rs. {(item.price * item.quantity).toLocaleString("en-IN")}</div>
                <button className="button ghost" onClick={() => updateQuantity(item.id, 0)} style={{ marginTop: 10 }}>
                  <Trash2 size={16} /> Remove
                </button>
              </div>
            </div>
          ))}
        </Card>

        <form onSubmit={placeOrder}>
          <Card>
            <span className="label-caps">Delivery details</span>
            <label style={{ display: "block", marginTop: 14 }}>
              <span className="label-caps">Deliver to</span>
              <select className="input" defaultValue={getDefaultAddressId(sortedAddresses)} name="customerAddressId" required style={{ marginTop: 8 }}>
                {sortedAddresses.map((address) => (
                  <option key={address.id} value={address.id}>
                    {formatAddressOption(address)}
                  </option>
                ))}
              </select>
            </label>
            {addressStatus === "fallback" && (
              <p className="muted" style={{ marginBottom: 0 }}>
                Your saved addresses are taking a moment to load.
              </p>
            )}

            <div style={{ borderTop: "1px solid rgba(89,65,57,.45)", marginTop: 18, paddingTop: 16 }}>
              <span className="label-caps">Order summary</span>
              <SummaryRow label="Subtotal" value={subtotal} />
              <SummaryRow label="Fulfillment handling" value={handling} />
              <SummaryRow label="Total" value={total} strong />
            </div>

            <Button disabled={cart.length === 0 || checkoutStatus === "submitting"} style={{ marginTop: 18, width: "100%" }}>
              <CreditCard size={18} /> {checkoutStatus === "submitting" ? "Placing order..." : "Place order"}
            </Button>
            {checkoutStatus === "offline" && (
              <p className="muted" style={{ marginBottom: 0 }}>
                {checkoutError}
              </p>
            )}
          </Card>
        </form>
      </div>
    </section>
  );
}

function getDefaultAddressId(addresses) {
  return addresses.find((address) => address.defaultAddress)?.id ?? addresses[0]?.id ?? "";
}

function formatAddressOption(address) {
  const label = address.label ? `${address.label} - ` : "";
  return `${label}${formatAddress(address)}`;
}

function formatAddress(address) {
  return [address.addressLine, address.city, address.state, address.pincode].filter(Boolean).join(", ");
}

function SummaryRow({ label, strong, value }) {
  return (
    <div style={{ display: "flex", justifyContent: "space-between", marginTop: 14 }}>
      <span className={strong ? "" : "muted"}>{label}</span>
      <strong>Rs. {value.toLocaleString("en-IN")}</strong>
    </div>
  );
}
