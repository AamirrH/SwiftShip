import { BellRing, CheckCheck } from "lucide-react";
import { Button } from "../ui/Button.jsx";
import { Card } from "../ui/Card.jsx";

export function NotificationCard({ notification, onMarkRead }) {
  const unread = notification.readStatus === "UNREAD";

  return (
    <Card className={`notification-card ${unread ? "unread" : ""}`}>
      <div className="notification-icon">
        <BellRing size={20} />
      </div>
      <div>
        <div className="notification-card-header">
          <span className="label-caps">{formatType(notification.notificationType)}</span>
          <span className="muted">{formatDate(notification.createdAt)}</span>
        </div>
        <h3 className="section-title" style={{ fontSize: 19, margin: "6px 0" }}>
          {notification.title}
        </h3>
        <p className="muted" style={{ lineHeight: 1.55, margin: 0 }}>
          {notification.message}
        </p>
        {notification.orderNumber && (
          <div className="notification-order">Order #{notification.orderNumber}</div>
        )}
      </div>
      {unread && (
        <Button variant="ghost" onClick={() => onMarkRead(notification.notificationId)}>
          <CheckCheck size={16} />
          Mark read
        </Button>
      )}
    </Card>
  );
}

function formatType(type) {
  return String(type ?? "Notification").replaceAll("_", " ").toLowerCase();
}

function formatDate(value) {
  if (!value) return "Recently";
  return new Intl.DateTimeFormat("en-IN", {
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
    month: "short",
  }).format(new Date(value));
}
