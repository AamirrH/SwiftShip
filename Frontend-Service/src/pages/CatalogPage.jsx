import { Search, SlidersHorizontal } from "lucide-react";
import { useMemo, useState } from "react";
import { ProductCard } from "../components/products/ProductCard.jsx";
import { Button } from "../components/ui/Button.jsx";
import { api } from "../lib/api.js";
import { useApiResource } from "../hooks/useApiResource.js";
import { mockProducts } from "../data/mockData.js";

const categories = ["All", "Electronics", "Daily Essentials", "Personal Care", "Home", "Lifestyle", "Packaging"];

export function CatalogPage({ onAddToCart, onOpenProduct, onSearchQueryChange, searchQuery = "" }) {
  const [category, setCategory] = useState("All");
  const { data } = useApiResource(api.getProducts, mockProducts, []);

  const products = useMemo(
    () =>
      data.map((product, index) => ({
        ...mockProducts[index % mockProducts.length],
        ...product,
        image: product.image ?? mockProducts[index % mockProducts.length].image,
        category: product.category ?? mockProducts[index % mockProducts.length].category,
        status: product.stock < 10 ? "Low stock" : "In stock",
        description: product.description ?? mockProducts[index % mockProducts.length].description,
      })),
    [data]
  );

  const filtered = products.filter((product) => {
    const matchesCategory = category === "All" || product.category === category;
    const normalizedQuery = searchQuery.trim().toLowerCase();
    const searchableText = [
      product.productName,
      product.category,
      product.description,
    ]
      .filter(Boolean)
      .join(" ")
      .toLowerCase();
    const matchesQuery = !normalizedQuery || searchableText.includes(normalizedQuery);
    return matchesCategory && matchesQuery;
  });

  return (
    <section className="page">
      <div className="page-header">
        <div>
          <span className="label-caps">Customer catalog</span>
          <h1 className="page-title">Shop everything SwiftShip can move</h1>
          <p className="muted">Electronics, daily essentials, personal care, home goods, and shipping supplies from one fast catalog.</p>
        </div>
        <Button variant="secondary">
          <SlidersHorizontal size={18} />
          Filters
        </Button>
      </div>

      <div className="card card-pad" style={{ marginBottom: 24 }}>
        <div className="grid two">
          <div style={{ alignSelf: "start", minHeight: 42, position: "relative" }}>
            <Search size={18} style={{ left: 14, pointerEvents: "none", position: "absolute", top: 12 }} />
            <input
              className="input"
              placeholder="Search catalog..."
              style={{ paddingLeft: 42 }}
              value={searchQuery}
              onChange={(event) => onSearchQueryChange?.(event.target.value)}
            />
          </div>
          <div style={{ display: "flex", flexWrap: "wrap", gap: 8, justifyContent: "flex-end" }}>
            {categories.map((item) => (
              <button className={`button ${category === item ? "primary" : "secondary"}`} key={item} onClick={() => setCategory(item)}>
                {item}
              </button>
            ))}
          </div>
        </div>
      </div>

      <div className="grid four">
        {filtered.map((product) => (
          <ProductCard key={product.id} product={product} onAddToCart={onAddToCart} onOpenProduct={onOpenProduct} />
        ))}
        {filtered.length === 0 && (
          <div className="card card-pad muted">
            No products matched your search.
          </div>
        )}
      </div>
    </section>
  );
}
