import { ArrowRight, BellRing, PackageCheck } from "lucide-react";
import { useMemo, useState } from "react";
import { NotificationCard } from "../components/notifications/NotificationCard.jsx";
import { Button } from "../components/ui/Button.jsx";
import { Card } from "../components/ui/Card.jsx";
import { StatusBadge } from "../components/ui/StatusBadge.jsx";
import { useApiResource } from "../hooks/useApiResource.js";
import { api } from "../lib/api.js";
import { mockNotifications, mockOrders } from "../data/mockData.js";

const CUSTOMER_ID = 1;

export function OrdersPage({ onNavigate }) {
  const { data, status } = useApiResource(api.getOrders, mockOrders, []);
  const { data: notifications } = useApiResource(
    () => api.getCustomerNotifications(CUSTOMER_ID),
    mockNotifications,
    []
  );
  const [readOverrides, setReadOverrides] = useState({});
  const orders = data.map(normalizeOrder);
  const [selectedOrderId, setSelectedOrderId] = useState(orders[0]?.id);
  const selectedOrder = orders.find((order) => order.id === selectedOrderId) ?? orders[0];
  const selectedOrderNotifications = useMemo(
    () =>
      notifications
        .filter((notification) => notification.orderNumber === selectedOrder?.id)
        .map((notification) => ({
          ...notification,
          readStatus: readOverrides[notification.notificationId] ?? notification.readStatus,
        }))
        .sort((a, b) => new Date(b.createdAt ?? 0) - new Date(a.createdAt ?? 0)),
    [notifications, readOverrides, selectedOrder]
  );

  async function markRead(notificationId) {
    setReadOverrides((current) => ({ ...current, [notificationId]: "READ" }));
    try {
      await api.markNotificationRead(notificationId);
    } catch {
      // Keep the optimistic read state while notification-service is offline.
    }
  }

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

      <div className="orders-workspace">
        <Card padded={false}>
          {orders.map((order) => {
            const relatedUnreadCount = notifications.filter(
              (notification) => notification.orderNumber === order.id && notification.readStatus === "UNREAD"
            ).length;

            return (
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
                  <div style={{ display: "flex", flexWrap: "wrap", gap: 8 }}>
                    <StatusBadge>{order.status}</StatusBadge>
                    {relatedUnreadCount > 0 && <span className="status warning">{relatedUnreadCount} unread</span>}
                  </div>
                </div>
                <div className="row-action" style={{ textAlign: "right" }}>
                  <div className="label-caps">{order.placedAt}</div>
                  <strong>ETA {order.eta}</strong>
                  <div style={{ marginTop: 12 }}>
                    <Button
                      variant="ghost"
                      onClick={(event) => {
                        event.stopPropagation();
                        onNavigate("tracking");
                      }}
                    >
                      Details <ArrowRight size={16} />
                    </Button>
                  </div>
                </div>
              </div>
            );
          })}
        </Card>

        <Card>
          <span className="label-caps">Updates for selected order</span>
          <h2 className="section-title" style={{ fontSize: 24, margin: "8px 0" }}>
            {selectedOrder?.number ?? "No order selected"}
          </h2>
          <p className="muted" style={{ marginTop: 0 }}>
            Notifications are filtered by order number, so customer updates stay tied to the order they belong to.
          </p>
          <div className="grid" style={{ gap: 12, marginTop: 18 }}>
            {selectedOrderNotifications.length === 0 ? (
              <div className="empty-state">
                <BellRing size={24} />
                <p>No updates for this order yet.</p>
              </div>
            ) : (
              selectedOrderNotifications.map((notification) => (
                <NotificationCard
                  key={notification.notificationId}
                  notification={notification}
                  onMarkRead={markRead}
                />
              ))
            )}
          </div>
        </Card>
      </div>
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
