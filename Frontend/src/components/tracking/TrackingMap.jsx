import { Home, MapPin, Navigation, Truck } from "lucide-react";

export function TrackingMap() {
  return (
    <div className="tracking-map">
      <div className="route-line" />
      <div className="map-pin" style={{ left: 42, bottom: 70 }}>
        <MapPin size={21} />
      </div>
      <div className="map-pin" style={{ left: "52%", top: "42%", color: "var(--tertiary)" }}>
        <Truck size={21} />
      </div>
      <div className="map-pin" style={{ right: 42, top: 48, color: "var(--secondary)" }}>
        <Home size={21} />
      </div>
      <div className="card card-pad" style={{ bottom: 18, left: 18, position: "absolute", width: "min(320px, calc(100% - 36px))" }}>
        <span className="label-caps">Live Route</span>
        <h3 className="section-title" style={{ margin: "6px 0 8px" }}>Driver is closing in</h3>
        <p className="muted" style={{ margin: 0 }}>Warehouse pickup, traffic-aware route, and delivery drop are shown in one stream.</p>
      </div>
      <Navigation size={28} style={{ color: "var(--primary)", position: "absolute", right: 120, bottom: 104 }} />
    </div>
  );
}
