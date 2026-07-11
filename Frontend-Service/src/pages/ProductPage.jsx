import { ArrowLeft, Minus, Plus, ShieldCheck, ShoppingCart, Truck } from "lucide-react";
import { useState } from "react";
import { Button } from "../components/ui/Button.jsx";
import { Card } from "../components/ui/Card.jsx";
import { StatusBadge } from "../components/ui/StatusBadge.jsx";

export function ProductPage({ onAddToCart, onBack, product }) {
  const [quantity, setQuantity] = useState(1);

  return (
    <section className="page">
      <Button variant="secondary" onClick={onBack}>
        <ArrowLeft size={18} /> Back to catalog
      </Button>

      <div className="split" style={{ alignItems: "start", marginTop: 24 }}>
        <Card padded={false} style={{ overflow: "hidden" }}>
          <div className="product-media" style={{ aspectRatio: "16 / 11" }}>
            <img src={product.image} alt={product.productName} />
          </div>
        </Card>

        <Card>
          <StatusBadge>{product.status}</StatusBadge>
          <h1 className="page-title" style={{ fontSize: 36, marginTop: 14 }}>{product.productName}</h1>
          <p className="muted" style={{ lineHeight: 1.65 }}>{product.description}</p>
          <div className="price" style={{ fontSize: 34, margin: "20px 0" }}>Rs. {product.productPrice.toLocaleString("en-IN")}</div>

          <div style={{ display: "flex", gap: 10, marginBottom: 18 }}>
            <button className="icon-button" onClick={() => setQuantity(Math.max(1, quantity - 1))} title="Decrease quantity">
              <Minus size={18} />
            </button>
            <input className="input" readOnly style={{ textAlign: "center", width: 80 }} value={quantity} />
            <button className="icon-button" onClick={() => setQuantity(quantity + 1)} title="Increase quantity">
              <Plus size={18} />
            </button>
          </div>

          <Button className="row-action" onClick={() => onAddToCart(product, quantity)}>
            <ShoppingCart size={18} /> Add to cart
          </Button>

          <div className="grid two" style={{ marginTop: 24 }}>
            <Info icon={Truck} label="Fulfillment" value="Nearest warehouse selection" />
            <Info icon={ShieldCheck} label="Inventory" value={`${product.stock} units available`} />
          </div>
        </Card>
      </div>
    </section>
  );
}

function Info({ icon: Icon, label, value }) {
  return (
    <div className="card card-pad" style={{ boxShadow: "none" }}>
      <Icon size={22} style={{ color: "var(--primary)" }} />
      <div className="label-caps" style={{ marginTop: 10 }}>{label}</div>
      <strong>{value}</strong>
    </div>
  );
}
