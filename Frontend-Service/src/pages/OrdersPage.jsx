import { ArrowRight, PackageCheck } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { Button } from "../components/ui/Button.jsx";
import { Card } from "../components/ui/Card.jsx";
import { StatusBadge } from "../components/ui/StatusBadge.jsx";
import { useApiResource } from "../hooks/useApiResource.js";
import { api } from "../lib/api.js";

export function OrdersPage({ onNavigate, onOrderCancelled, onTrackOrder }) {
  const { data, status, error } = useApiResource(api.getMyOrders, [], []);
  const [statusOverrides, setStatusOverrides] = useState({});
  const orders = useMemo(() => groupOrderRows(data).map(normalizeOrder), [data]);
  const visibleOrders = orders.map((order) => ({
    ...order,
    status: statusOverrides[order.id] ?? order.status,
  }));
  const [selectedOrderId, setSelectedOrderId] = useState(orders[0]?.id);
  const selectedOrder = visibleOrders.find((order) => order.id === selectedOrderId) ?? visibleOrders[0];

  async function cancelOrder(orderId) {
    const confirmed = window.confirm(`Cancel order #${orderId}?`);
    if (!confirmed) return;

    try {
      await api.cancelOrder(orderId);
      setStatusOverrides((current) => ({ ...current, [orderId]: "CANCELLED" }));
      onOrderCancelled?.(`Order #${orderId} has been cancelled`);
    } catch (error) {
      onOrderCancelled?.(`Could not cancel order #${orderId} right now. Please try again.`);
    }
  }

  useEffect(() => {
    if (!orders.length) {
      setSelectedOrderId(undefined);
      return;
    }
    if (!orders.some((order) => order.id === selectedOrderId)) {
      setSelectedOrderId(orders[0].id);
    }
  }, [orders, selectedOrderId]);

  return (
    <section className="page">
      <div className="page-header">
        <div>
          <span className="label-caps">Customer orders</span>
          <h1 className="page-title">Order history</h1>
          <p className="muted">
            {status === "fallback" ? fallbackMessage(error, "orders") : "Your latest orders are ready."}
          </p>
        </div>
        <Button onClick={() => onNavigate("catalog")}>
          <PackageCheck size={18} /> New order
        </Button>
      </div>

      <Card padded={false}>
        {visibleOrders.map((order) => (
          <div
            className={`order-row interactive ${selectedOrder?.id === order.id ? "active" : ""}`}
            key={order.id}
            onClick={() => setSelectedOrderId(order.id)}
          >
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
                <Button
                  variant="ghost"
                  onClick={(event) => {
                    event.stopPropagation();
                    onTrackOrder(order.id);
                    onNavigate("tracking");
                  }}
                >
                  Details <ArrowRight size={16} />
                </Button>
                {canCancelOrder(order.status) && (
                  <Button
                    variant="secondary"
                    onClick={(event) => {
                      event.stopPropagation();
                      cancelOrder(order.id);
                    }}
                    style={{ marginLeft: 8 }}
                  >
                    Cancel
                  </Button>
                )}
              </div>
            </div>
          </div>
        ))}
        {visibleOrders.length === 0 && (
          <div className="card-pad muted">
            {status === "fallback"
              ? fallbackMessage(error, "orders")
              : "No orders yet. Your orders will appear here after checkout."}
          </div>
        )}
      </Card>
    </section>
  );
}

function normalizeOrder(order) {
  return {
    id: getOrderId(order),
    number: order.number ?? `ORD-${getOrderId(order)}`,
    status: order.status ?? order.orderStatus ?? "Reserved",
    eta: order.eta ?? "Calculating",
    placedAt: order.placedAt ?? "Recently",
    total: order.total ?? order.totalPrice ?? 0,
    destination: order.destination ?? order.deliveryAddress ?? "Saved delivery address",
    itemCount: order.itemCount ?? order.items?.length ?? 0,
  };
}

function groupOrderRows(rows) {
  const grouped = new Map();
  rows.forEach((row) => {
    const orderId = getOrderId(row);
    if (!orderId) return;

    const existing = grouped.get(orderId);
    if (!existing) {
      grouped.set(orderId, row);
      return;
    }

    grouped.set(orderId, {
      ...existing,
      ...row,
      items: mergeItems(existing.items, row.items),
      itemCount: Math.max(existing.itemCount ?? 0, row.itemCount ?? 0),
    });
  });
  return [...grouped.values()];
}

function mergeItems(currentItems = [], nextItems = []) {
  const items = new Map();
  [...currentItems, ...nextItems].forEach((item) => {
    const key = item.id ?? `${item.productId}-${item.quantity}`;
    items.set(key, item);
  });
  return [...items.values()];
}

function getOrderId(order) {
  return order.id ?? order.orderId ?? order.orderNumber;
}

function canCancelOrder(status) {
  return !["CANCELLED", "DELIVERED"].includes(String(status).toUpperCase());
}

function fallbackMessage(error, resourceName) {
  if (!error?.status) {
    return `We could not refresh your ${resourceName} right now.`;
  }
  return `We could not refresh your ${resourceName} right now.`;
}
