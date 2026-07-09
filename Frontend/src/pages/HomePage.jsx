import { ArrowRight, Clock3, MapPinned, Route, Sparkles } from "lucide-react";
import { ProductCard } from "../components/products/ProductCard.jsx";
import { Button } from "../components/ui/Button.jsx";
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
            <h1>Everything you need, delivered in {mockOrders[0].eta}.</h1>
            <p>
              Shop electronics, daily essentials, personal care, and home supplies while SwiftShip handles stock,
              warehouse assignment, route planning, and live tracking behind the scenes.
            </p>
            <div className="hero-highlights" aria-label="SwiftShip promises">
              <div>
                <Clock3 size={18} />
                <strong>{mockOrders[0].eta}</strong>
                <span>average delivery</span>
              </div>
              <div>
                <MapPinned size={18} />
                <strong>Live</strong>
                <span>order tracking</span>
              </div>
              <div>
                <Sparkles size={18} />
                <strong>8</strong>
                <span>trial categories</span>
              </div>
            </div>
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
      </div>

      <div className="page-header">
        <div>
          <span className="label-caps">Popular now</span>
          <h2 className="section-title" style={{ fontSize: 26, margin: "6px 0 0" }}>Featured picks for today</h2>
          {status === "fallback" && <p className="muted">Showing trial products while your backend catalog is offline.</p>}
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
