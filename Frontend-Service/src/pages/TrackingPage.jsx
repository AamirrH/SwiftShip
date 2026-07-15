import { Clock3, Navigation, Route, Truck } from "lucide-react";
import { OrderTimeline } from "../components/orders/OrderTimeline.jsx";
import { TrackingMap } from "../components/tracking/TrackingMap.jsx";
import { Card } from "../components/ui/Card.jsx";
import { StatusBadge } from "../components/ui/StatusBadge.jsx";
import { api } from "../lib/api.js";
import { useApiResource } from "../hooks/useApiResource.js";
import { mockTracking } from "../data/mockData.js";

export function TrackingPage({ orderNumber }) {
  const trackingOrderNumber = orderNumber ?? mockTracking.orderNumber;
  const { data, status, error } = useApiResource(() => api.getTracking(trackingOrderNumber), mockTracking, [trackingOrderNumber]);
  const tracking = {
    ...mockTracking,
    ...data,
    steps: data.steps ?? mockTracking.steps,
    currentLocation: data.currentLocation ?? mockTracking.currentLocation,
  };

  return (
    <section className="page">
      <div className="page-header">
        <div>
          <span className="label-caps">Live order tracking</span>
          <h1 className="page-title">Order #{tracking.orderNumber}</h1>
          <p className="muted">
            {status === "fallback" ? fallbackMessage(error) : tracking.customerAddress}
          </p>
        </div>
        <StatusBadge>{String(tracking.status ?? tracking.trackingStatus).replaceAll("_", " ")}</StatusBadge>
      </div>

      <Card>
        <OrderTimeline steps={tracking.steps} />
        <div className="grid three">
          <Info icon={Clock3} label="Current ETA" value={`${Math.round(tracking.currentEtaMinutes)} min`} />
          <Info icon={Route} label="Remaining" value={`${tracking.remainingDistanceKm} km`} />
          <Info icon={Navigation} label="Location" value={tracking.currentLocation} />
        </div>
      </Card>

      <div className="split" style={{ marginTop: 24 }}>
        <TrackingMap />
        <Card>
          <span className="label-caps">Delivery partner</span>
          <h2 className="section-title" style={{ fontSize: 28, margin: "8px 0" }}>{tracking.driverName}</h2>
          <p className="muted">Your driver is on the way. We will keep this page updated as your order moves.</p>
          <div className="card card-pad" style={{ boxShadow: "none", marginTop: 20 }}>
            <Truck size={22} style={{ color: "var(--primary)" }} />
            <div className="label-caps" style={{ marginTop: 10 }}>Current status</div>
            <strong>{String(tracking.status ?? tracking.trackingStatus).replaceAll("_", " ")}</strong>
          </div>
        </Card>
      </div>
    </section>
  );
}

function fallbackMessage(error) {
  if (!error?.status) {
    return "We could not refresh tracking right now.";
  }
  if (error.status === 404) {
    return "Tracking session was not created for this order yet.";
  }
  return "We could not refresh tracking right now.";
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
