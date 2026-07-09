import { ArrowRight, Clock3, PackageCheck, Route, Truck } from "lucide-react";
import { ProductCard } from "../components/products/ProductCard.jsx";
import { Button } from "../components/ui/Button.jsx";
import { Card } from "../components/ui/Card.jsx";
import { api } from "../lib/api.js";
import { useApiResource } from "../hooks/useApiResource.js";
import { mockOrders, mockProducts } from "../data/mockData.js";

export function HomePage({ onAddToCart, onNavigate, onOpenProduct }) {
  const { data: products, status } = useApiResource(api.getProducts, mockProducts, []);
  const featured = products.slice(0, 4).map((product, index) => ({
    ...mockProducts[index],
    ...product,
    image: product.image ?? mockProducts[index % mockProducts.length].image,
    category: product.category ?? mockProducts[index % mockProducts.length].category,
    status: product.stock < 10 ? "Low stock" : "In stock",
    description: product.description ?? mockProducts[index % mockProducts.length].description,
  }));

  return (
    <section className="page">
      <div className="hero">
        <div className="hero-panel card">
          <div className="hero-copy">
            <span className="label-caps">Fast fulfillment, live delivery</span>
            <h1>Everything you need, delivered with live precision.</h1>
            <p>
              Shop daily essentials, electronics, and home supplies while SwiftShip handles stock reservation,
              warehouse assignment, route planning, and tracking in one clean customer flow.
            </p>
            <div style={{ display: "flex", flexWrap: "wrap", gap: 12, marginTop: 24 }}>
              <Button onClick={() => onNavigate("catalog")}>
                Shop catalog <ArrowRight size={18} />
              </Button>
              <Button variant="secondary" onClick={() => onNavigate("tracking")}>
                Track order <Route size={18} />
              </Button>
            </div>
          </div>
        </div>

        <div className="grid" style={{ gridTemplateRows: "repeat(3, 1fr)" }}>
          <MetricCard icon={PackageCheck} label="Stock-ready items" value={products.length || 6} tone="secondary" />
          <MetricCard icon={Truck} label="Active delivery" value={mockOrders[0].eta} tone="tertiary" />
          <MetricCard icon={Clock3} label="Gateway mode" value={status === "fallback" ? "Mock" : "Live"} tone="primary" />
        </div>
      </div>

      <div className="page-header">
        <div>
          <span className="label-caps">Popular now</span>
          <h2 className="section-title" style={{ fontSize: 26, margin: "6px 0 0" }}>Featured picks for today</h2>
        </div>
        <Button variant="ghost" onClick={() => onNavigate("catalog")}>
          View all <ArrowRight size={18} />
        </Button>
      </div>

      <div className="grid four">
        {featured.map((product) => (
          <ProductCard key={product.id} product={product} onAddToCart={onAddToCart} onOpenProduct={onOpenProduct} />
        ))}
      </div>
    </section>
  );
}

function MetricCard({ icon: Icon, label, tone, value }) {
  const color = tone === "secondary" ? "var(--secondary)" : tone === "tertiary" ? "var(--tertiary)" : "var(--primary)";
  return (
    <Card className="metric-card">
      <div style={{ color, display: "inline-grid", marginBottom: 8, placeItems: "center" }}>
        <Icon size={28} />
      </div>
      <div className="muted">{label}</div>
      <div className="metric-value">{value}</div>
    </Card>
  );
}
