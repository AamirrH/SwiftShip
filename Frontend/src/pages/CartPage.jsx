import { CreditCard, Minus, Plus, Trash2 } from "lucide-react";
import { Button } from "../components/ui/Button.jsx";
import { Card } from "../components/ui/Card.jsx";

export function CartPage({ cart, onNavigate, setCart }) {
  const subtotal = cart.reduce((sum, item) => sum + item.price * item.quantity, 0);
  const handling = cart.length ? 199 : 0;
  const total = subtotal + handling;

  function updateQuantity(id, nextQuantity) {
    setCart((items) =>
      items.flatMap((item) => (item.id === id ? (nextQuantity > 0 ? [{ ...item, quantity: nextQuantity }] : []) : [item]))
    );
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

        <Card>
          <span className="label-caps">Order summary</span>
          <SummaryRow label="Subtotal" value={subtotal} />
          <SummaryRow label="Fulfillment handling" value={handling} />
          <div style={{ borderTop: "1px solid rgba(89,65,57,.45)", marginTop: 16, paddingTop: 16 }}>
            <SummaryRow label="Total" value={total} strong />
          </div>
          <Button className="row-action" onClick={() => onNavigate("orders")} style={{ marginTop: 18, width: "100%" }}>
            <CreditCard size={18} /> Place order
          </Button>
        </Card>
      </div>
    </section>
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
