import { Eye, ShoppingCart } from "lucide-react";
import { Button } from "../ui/Button.jsx";
import { Card } from "../ui/Card.jsx";
import { StatusBadge } from "../ui/StatusBadge.jsx";

export function ProductCard({ product, onAddToCart, onOpenProduct }) {
  return (
    <Card padded={false} className="product-card hover-card">
      <div className="product-media">
        <img src={product.image} alt={product.productName} />
        <div style={{ position: "absolute", right: 14, top: 14 }}>
          <StatusBadge>{product.status}</StatusBadge>
        </div>
      </div>
      <div className="card-pad" style={{ display: "flex", flex: 1, flexDirection: "column", gap: 14 }}>
        <div>
          <div className="label-caps">{product.category}</div>
          <h3 className="section-title" style={{ fontSize: 20, lineHeight: 1.25, margin: "6px 0" }}>
            {product.productName}
          </h3>
          <p className="muted" style={{ margin: 0, lineHeight: 1.5 }}>{product.description}</p>
        </div>
        <div style={{ alignItems: "center", display: "flex", justifyContent: "space-between", marginTop: "auto" }}>
          <span className="price">Rs. {product.productPrice.toLocaleString("en-IN")}</span>
          <div style={{ display: "flex", gap: 8 }}>
            <Button variant="secondary" onClick={() => onOpenProduct(product.id)} title="View details">
              <Eye size={17} />
            </Button>
            <Button onClick={() => onAddToCart(product)} title="Add to cart">
              <ShoppingCart size={17} />
            </Button>
          </div>
        </div>
      </div>
    </Card>
  );
}
