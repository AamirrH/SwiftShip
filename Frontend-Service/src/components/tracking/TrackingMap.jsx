import { Home, MapPin, Navigation, Truck } from "lucide-react";

export function TrackingMap({ tracking }) {
  const progress = calculateProgress(tracking);
  const truckPosition = calculateTruckPosition(progress);

  return (
    <div className="tracking-map">
      <div className="route-line" />
      <div className="map-pin" style={{ left: 42, bottom: 70 }}>
        <MapPin size={21} />
      </div>
      <div
        className="map-pin tracking-truck-pin"
        style={{ left: `${truckPosition.left}%`, top: `${truckPosition.top}%`, color: "var(--tertiary)" }}
      >
        <Truck size={21} />
      </div>
      <div className="map-pin" style={{ right: 42, top: 48, color: "var(--secondary)" }}>
        <Home size={21} />
      </div>
      <div className="card card-pad" style={{ bottom: 18, left: 18, position: "absolute", width: "min(320px, calc(100% - 36px))" }}>
        <span className="label-caps">Live Route</span>
        <h3 className="section-title" style={{ margin: "6px 0 8px" }}>{Math.round(progress * 100)}% complete</h3>
        <p className="muted" style={{ margin: 0 }}>
          Driver position updates automatically as the simulated delivery moves.
        </p>
      </div>
      <Navigation size={28} style={{ color: "var(--primary)", position: "absolute", right: 120, bottom: 104 }} />
    </div>
  );
}

function calculateProgress(tracking) {
  const totalDistance = Number(tracking?.totalDistanceKm ?? 0);
  const remainingDistance = Number(tracking?.remainingDistanceKm ?? totalDistance);
  if (totalDistance <= 0) {
    return tracking?.trackingStatus === "DELIVERED" ? 1 : 0;
  }
  return Math.min(1, Math.max(0, (totalDistance - remainingDistance) / totalDistance));
}

function calculateTruckPosition(progress) {
  if (progress < 0.62) {
    return {
      left: 8 + progress * 108,
      top: 71,
    };
  }

  const verticalProgress = (progress - 0.62) / 0.38;
  return {
    left: 75,
    top: 71 - verticalProgress * 54,
  };
}
