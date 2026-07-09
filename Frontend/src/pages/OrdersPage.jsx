import { ArrowRight, PackageCheck } from "lucide-react";
import { Button } from "../components/ui/Button.jsx";
import { Card } from "../components/ui/Card.jsx";
import { StatusBadge } from "../components/ui/StatusBadge.jsx";
import { useApiResource } from "../hooks/useApiResource.js";
import { api } from "../lib/api.js";
import { mockOrders } from "../data/mockData.js";

export function OrdersPage({ onNavigate }) {
  const { data, status } = useApiResource(api.getOrders, mockOrders, []);
  const orders = data.map(normalizeOrder);

  return (
    <section className="page">
      <div className="page-header">
        <div>
          <span className="label-caps">Customer orders</span>
          <h1 className="page-title">Order history</h1>
          <p className="muted">
            {status === "fallback" ? "Showing local demo orders until the gateway is running." : "Synced from `/orders`."}
          </p>
        </div>
        <Button onClick={() => onNavigate("catalog")}>
          <PackageCheck size={18} /> New order
        </Button>
      </div>

      <Card padded={false}>
        {orders.map((order) => (
          <div className="order-row" key={order.id}>
            <div className="thumb" style={{ display: "grid", placeItems: "center" }}>
              <PackageCheck size={34} style={{ color: "var(--primary)" }} />
            </div>
            <div>
              <h3 className="section-title" style={{ fontSize: 20, margin: 0 }}>{order.number}</h3>
              <p className="muted" style={{ margin: "6px 0" }}>{order.itemCount} items to {order.destination}</p>
              <StatusBadge>{order.status}</StatusBadge>
            </div>
            <div className="row-action" style={{ textAlign: "right" }}>
              <div className="label-caps">{order.placedAt}</div>
              <strong>ETA {order.eta}</strong>
              <div style={{ marginTop: 12 }}>
                <Button variant="ghost" onClick={() => onNavigate("tracking")}>
                  Details <ArrowRight size={16} />
                </Button>
              </div>
            </div>
          </div>
        ))}
      </Card>
    </section>
  );
}

function normalizeOrder(order) {
  return {
    id: order.id,
    number: order.number ?? `ORD-${order.id}`,
    status: order.status ?? "Reserved",
    eta: order.eta ?? "Calculating",
    placedAt: order.placedAt ?? "Recently",
    total: order.total ?? order.totalPrice ?? 0,
    destination: order.destination ?? order.deliveryAddress ?? "Saved delivery address",
    itemCount: order.itemCount ?? order.items?.length ?? 0,
  };
}
