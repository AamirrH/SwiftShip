import { BellRing, CheckCheck } from "lucide-react";
import { useMemo, useState } from "react";
import { NotificationCard } from "../components/notifications/NotificationCard.jsx";
import { Button } from "../components/ui/Button.jsx";
import { Card } from "../components/ui/Card.jsx";
import { useApiResource } from "../hooks/useApiResource.js";
import { api } from "../lib/api.js";
import { mockNotifications } from "../data/mockData.js";

const CUSTOMER_ID = 1;

export function NotificationsPage() {
  const { data, status } = useApiResource(() => api.getCustomerNotifications(CUSTOMER_ID), mockNotifications, []);
  const [readOverrides, setReadOverrides] = useState({});
  const [filter, setFilter] = useState("all");

  const notifications = useMemo(
    () =>
      data
        .map((notification) => ({
          ...notification,
          readStatus: readOverrides[notification.notificationId] ?? notification.readStatus,
        }))
        .sort((a, b) => new Date(b.createdAt ?? 0) - new Date(a.createdAt ?? 0)),
    [data, readOverrides]
  );

  const unreadCount = notifications.filter((notification) => notification.readStatus === "UNREAD").length;
  const visibleNotifications =
    filter === "unread"
      ? notifications.filter((notification) => notification.readStatus === "UNREAD")
      : notifications;

  async function markRead(notificationId) {
    setReadOverrides((current) => ({ ...current, [notificationId]: "READ" }));
    try {
      await api.markNotificationRead(notificationId);
    } catch {
      // Keep the optimistic UI update when the notification service is offline.
    }
  }

  function markAllRead() {
    const nextOverrides = {};
    notifications.forEach((notification) => {
      nextOverrides[notification.notificationId] = "READ";
    });
    setReadOverrides((current) => ({ ...current, ...nextOverrides }));
  }

  return (
    <section className="page">
      <div className="page-header">
        <div>
          <span className="label-caps">Order notifications</span>
          <h1 className="page-title">Notification center</h1>
          <p className="muted">
            {status === "fallback"
              ? "Showing sample notifications until notification-service is running."
              : "Synced from notification-service."}
          </p>
        </div>
        <Button variant="secondary" onClick={markAllRead}>
          <CheckCheck size={18} />
          Mark all read
        </Button>
      </div>

      <div className="grid three" style={{ marginBottom: 24 }}>
        <SummaryCard label="Unread" value={unreadCount} />
        <SummaryCard label="Total notifications" value={notifications.length} />
        <SummaryCard label="Customer ID" value={CUSTOMER_ID} />
      </div>

      <div className="notification-toolbar">
        <button className={`button ${filter === "all" ? "primary" : "secondary"}`} onClick={() => setFilter("all")}>
          All
        </button>
        <button className={`button ${filter === "unread" ? "primary" : "secondary"}`} onClick={() => setFilter("unread")}>
          Unread
        </button>
      </div>

      <div className="grid" style={{ gap: 14 }}>
        {visibleNotifications.length === 0 ? (
          <Card>
            <BellRing size={24} style={{ color: "var(--primary)" }} />
            <p className="muted">No notifications in this view.</p>
          </Card>
        ) : (
          visibleNotifications.map((notification) => (
            <NotificationCard key={notification.notificationId} notification={notification} onMarkRead={markRead} />
          ))
        )}
      </div>
    </section>
  );
}

function SummaryCard({ label, value }) {
  return (
    <Card>
      <span className="label-caps">{label}</span>
      <div className="section-title" style={{ fontSize: 32, marginTop: 8 }}>
        {value}
      </div>
    </Card>
  );
}
