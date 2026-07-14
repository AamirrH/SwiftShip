import { CreditCard, Minus, Plus, Trash2 } from "lucide-react";
import { useState } from "react";
import { Button } from "../components/ui/Button.jsx";
import { Card } from "../components/ui/Card.jsx";
import { api } from "../lib/api.js";

export function CartPage({ cart, onNavigate, setCart }) {
  const [checkoutStatus, setCheckoutStatus] = useState("idle");
  const [checkoutError, setCheckoutError] = useState("");
  const subtotal = cart.reduce((sum, item) => sum + item.price * item.quantity, 0);
  const handling = cart.length ? 199 : 0;
  const total = subtotal + handling;

  function updateQuantity(id, nextQuantity) {
    setCart((items) =>
      items.flatMap((item) => (item.id === id ? (nextQuantity > 0 ? [{ ...item, quantity: nextQuantity }] : []) : [item]))
    );
  }

  async function placeOrder(event) {
    event.preventDefault();
    if (cart.length === 0) return;

    const form = new FormData(event.currentTarget);
    const payload = {
      customerId: Number(form.get("customerId")),
      customerAddressId: Number(form.get("customerAddressId")),
      deliveryAddress: form.get("deliveryAddress"),
      deliveryLat: Number(form.get("deliveryLat")),
      deliveryLng: Number(form.get("deliveryLng")),
      items: cart.map((item) => ({
        productId: item.productId,
        quantity: item.quantity,
      })),
    };

    setCheckoutStatus("submitting");
    setCheckoutError("");

    try {
      await api.createOrder(payload);
      setCheckoutStatus("success");
      setCart([]);
      onNavigate("orders");
    } catch (error) {
      setCheckoutStatus("offline");
      if (error.status === 401) {
        setCheckoutError("Please login again before placing the order.");
      } else if (error.status === 403) {
        setCheckoutError("Your account is not allowed to place this order.");
      } else if (error.status === 503) {
        setCheckoutError("Gateway is running, but Order-Service is not registered/reachable yet.");
      } else if (error.status) {
        setCheckoutError(`Backend rejected the order with status ${error.status}.`);
      } else {
        setCheckoutError("Gateway is not reachable yet. The order payload is ready for `/orders/createOrder`.");
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
            <div className="grid two" style={{ gap: 12, marginTop: 14 }}>
              <Field defaultValue="7" label="Customer ID" name="customerId" type="number" />
              <Field defaultValue="3" label="Address ID" name="customerAddressId" type="number" />
              <Field defaultValue="18.5515" label="Latitude" name="deliveryLat" step="0.0001" type="number" />
              <Field defaultValue="73.9349" label="Longitude" name="deliveryLng" step="0.0001" type="number" />
            </div>
            <label style={{ display: "block", marginTop: 12 }}>
              <span className="label-caps">Delivery address</span>
              <textarea
                className="input"
                defaultValue="EON Free Zone, Kharadi, Pune, Maharashtra, 411014"
                name="deliveryAddress"
                required
                rows="3"
                style={{ marginTop: 8, paddingBottom: 10, paddingTop: 10, resize: "vertical" }}
              />
            </label>

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

function Field({ label, ...props }) {
  return (
    <label>
      <span className="label-caps">{label}</span>
      <input className="input" required style={{ marginTop: 8 }} {...props} />
    </label>
  );
}

function SummaryRow({ label, strong, value }) {
  return (
    <div style={{ display: "flex", justifyContent: "space-between", marginTop: 14 }}>
      <span className={strong ? "" : "muted"}>{label}</span>
      <strong>Rs. {value.toLocaleString("en-IN")}</strong>
    </div>
  );
}
