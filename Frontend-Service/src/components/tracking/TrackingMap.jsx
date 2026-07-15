import { Home, Truck } from "lucide-react";

const ROUTE_PATH =
  "M 8 72 C 18 56 28 84 38 64 S 55 42 64 56 S 78 76 84 49 S 83 25 92 23";

export function TrackingMap({ tracking }) {
  const progress = calculateProgress(tracking);
  const truckPosition = calculateTruckPosition(progress);

  return (
    <div className="tracking-map">
      <svg className="tracking-route-svg" viewBox="0 0 100 100" preserveAspectRatio="none" aria-hidden="true">
        <path className="tracking-route-shadow" d={ROUTE_PATH} />
        <path className="tracking-route-path" d={ROUTE_PATH} />
      </svg>
      <div
        className="map-pin tracking-truck-pin"
        style={{ left: `${truckPosition.left}%`, top: `${truckPosition.top}%`, color: "var(--tertiary)" }}
      >
        <Truck size={21} />
      </div>
      <div className="map-pin tracking-home-pin" style={{ left: "92%", top: "23%", color: "var(--secondary)" }}>
        <Home size={21} />
      </div>
      <div className="card card-pad" style={{ bottom: 18, left: 18, position: "absolute", width: "min(320px, calc(100% - 36px))" }}>
        <span className="label-caps">Live Route</span>
        <h3 className="section-title" style={{ margin: "6px 0 8px" }}>{Math.round(progress * 100)}% complete</h3>
        <p className="muted" style={{ margin: 0 }}>
          Driver position updates automatically as the simulated delivery moves.
        </p>
      </div>
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
  const clampedProgress = Math.min(1, Math.max(0, progress));
  const routePoints = sampleRoutePoints();
  const targetIndex = Math.min(routePoints.length - 1, Math.round(clampedProgress * (routePoints.length - 1)));
  const point = routePoints[targetIndex];

  return {
    left: point.x,
    top: point.y,
  };
}

function sampleRoutePoints() {
  const segments = [
    {
      start: { x: 8, y: 72 },
      control1: { x: 18, y: 56 },
      control2: { x: 28, y: 84 },
      end: { x: 38, y: 64 },
    },
    {
      start: { x: 38, y: 64 },
      control1: { x: 48, y: 44 },
      control2: { x: 55, y: 42 },
      end: { x: 64, y: 56 },
    },
    {
      start: { x: 64, y: 56 },
      control1: { x: 73, y: 70 },
      control2: { x: 78, y: 76 },
      end: { x: 84, y: 49 },
    },
    {
      start: { x: 84, y: 49 },
      control1: { x: 90, y: 22 },
      control2: { x: 83, y: 25 },
      end: { x: 92, y: 23 },
    },
  ];

  return segments.flatMap((segment, segmentIndex) =>
    Array.from({ length: 24 }, (_, index) => {
      const step = segmentIndex === 0 ? index : index + 1;
      return cubicBezierPoint(segment, step / 24);
    })
  );
}

function cubicBezierPoint(segment, time) {
  const inverseTime = 1 - time;
  return {
    x:
      inverseTime ** 3 * segment.start.x +
      3 * inverseTime ** 2 * time * segment.control1.x +
      3 * inverseTime * time ** 2 * segment.control2.x +
      time ** 3 * segment.end.x,
    y:
      inverseTime ** 3 * segment.start.y +
      3 * inverseTime ** 2 * time * segment.control1.y +
      3 * inverseTime * time ** 2 * segment.control2.y +
      time ** 3 * segment.end.y,
  };
}
