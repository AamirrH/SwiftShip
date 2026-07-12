import { BrainCircuit, MapPin, Navigation, Route, Timer } from "lucide-react";
import { useMemo, useState } from "react";
import { Button } from "../components/ui/Button.jsx";
import { Card } from "../components/ui/Card.jsx";
import { api } from "../lib/api.js";
import { mockRouteOptions, mockSelectedRoute, mockSelectedRouteRecords, mockWarehouses } from "../data/mockData.js";

export function AdminRoutesPage() {
  const [routeOptions, setRouteOptions] = useState(mockRouteOptions);
  const [selectedRouteId, setSelectedRouteId] = useState(mockSelectedRoute.routeId);
  const [routeStatus, setRouteStatus] = useState("idle");
  const selectedRoute = useMemo(
    () => routeOptions.find((route) => route.routeId === selectedRouteId) ?? routeOptions[0],
    [routeOptions, selectedRouteId]
  );

  async function handleCalculateRoutes(event) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const payload = {
      coordinates: [
        [Number(form.get("warehouseLng")), Number(form.get("warehouseLat"))],
        [Number(form.get("customerLng")), Number(form.get("customerLat"))],
      ],
      alternative_routes: {
        target_count: Number(form.get("targetCount")),
        weight_factor: Number(form.get("weightFactor")),
      },
    };

    setRouteStatus("calculating");
    try {
      const routes = await api.calculateRoutes(payload);
      setRouteOptions(routes);
      setSelectedRouteId(routes[0]?.routeId);
      setRouteStatus("live");
    } catch {
      setRouteOptions(mockRouteOptions);
      setSelectedRouteId(mockSelectedRoute.routeId);
      setRouteStatus("offline");
    }
  }

  return (
    <section className="page">
      <div className="page-header">
        <div>
          <span className="label-caps">Admin routing operations</span>
          <h1 className="page-title">Route optimization desk</h1>
          <p className="muted">
            Compare alternatives from `/routes` and inspect which route should be selected for delivery.
          </p>
        </div>
      </div>

      <div className="admin-workspace">
        <Card>
          <span className="label-caps">Route request</span>
          <form className="admin-form" onSubmit={handleCalculateRoutes}>
            <div className="grid two">
              <Field defaultValue={mockWarehouses[0].lat} label="Warehouse lat" name="warehouseLat" step="0.0001" type="number" />
              <Field defaultValue={mockWarehouses[0].lng} label="Warehouse lng" name="warehouseLng" step="0.0001" type="number" />
              <Field defaultValue="12.9716" label="Customer lat" name="customerLat" step="0.0001" type="number" />
              <Field defaultValue="77.5946" label="Customer lng" name="customerLng" step="0.0001" type="number" />
              <Field defaultValue="3" label="Route count" name="targetCount" type="number" />
              <Field defaultValue="1.6" label="Weight factor" name="weightFactor" step="0.1" type="number" />
            </div>
            <Button>
              <Route size={18} />
              {routeStatus === "calculating" ? "Calculating..." : "Calculate alternatives"}
            </Button>
          </form>
          {routeStatus === "offline" && (
            <p className="muted">routing-service is offline, so sample alternatives are shown.</p>
          )}
        </Card>

        <Card>
          <span className="label-caps">Selected route</span>
          <h2 className="section-title" style={{ fontSize: 30, margin: "8px 0" }}>
            Route #{selectedRoute?.routeId}
          </h2>
          <div className="admin-detail-grid">
            <Detail icon={Navigation} label="Distance" value={`${selectedRoute?.totalDistance?.toFixed(2)} km`} />
            <Detail icon={Timer} label="ETA" value={`${selectedRoute?.timeToReach?.toFixed(1)} min`} />
          </div>
          <div className="route-reasoning">
            <BrainCircuit size={20} />
            <p>{mockSelectedRoute.reasoning}</p>
          </div>
        </Card>
      </div>

      <div className="route-admin-grid">
        <Card className="route-map-card">
          <span className="label-caps">Route comparison map</span>
          <div className="admin-route-map">
            {routeOptions.map((route, index) => (
              <button
                className={`route-track route-${index + 1} ${route.routeId === selectedRoute?.routeId ? "active" : ""}`}
                key={route.routeId}
                onClick={() => setSelectedRouteId(route.routeId)}
                title={`Route ${route.routeId}`}
              />
            ))}
            <div className="map-pin admin-origin">
              <MapPin size={20} />
            </div>
            <div className="map-pin admin-destination">
              <Navigation size={20} />
            </div>
          </div>
        </Card>

        <Card padded={false}>
          {routeOptions.map((route) => (
            <div
              className={`admin-row ${route.routeId === selectedRoute?.routeId ? "active" : ""}`}
              key={route.routeId}
              onClick={() => setSelectedRouteId(route.routeId)}
            >
              <div className="admin-row-icon">
                <Route size={21} />
              </div>
              <div>
                <h3 className="section-title" style={{ fontSize: 19, margin: 0 }}>Route #{route.routeId}</h3>
                <p className="muted" style={{ margin: "5px 0 0" }}>
                  {route.totalDistance.toFixed(2)} km - {route.timeToReach.toFixed(1)} min
                </p>
              </div>
              <strong>{scoreRoute(route)}</strong>
            </div>
          ))}
        </Card>
      </div>

      <Card padded={false} style={{ marginTop: 24 }}>
        <div className="admin-table-header">
          <div>
            <span className="label-caps">Selected route records</span>
            <h2 className="section-title" style={{ fontSize: 24, margin: "6px 0 0" }}>
              Persisted route decisions
            </h2>
          </div>
          <span className="status success">{mockSelectedRouteRecords.length} stored</span>
        </div>
        {mockSelectedRouteRecords.map((record) => (
          <div className="selected-route-row" key={record.serialId}>
            <div>
              <span className="label-caps">Order</span>
              <strong>#{record.orderId}</strong>
            </div>
            <div>
              <span className="label-caps">Route</span>
              <strong>#{record.selectedRouteId}</strong>
            </div>
            <div>
              <span className="label-caps">Customer</span>
              <strong>{record.customerId}</strong>
            </div>
            <div>
              <span className="label-caps">Distance</span>
              <strong>{record.totalDistance.toFixed(2)} km</strong>
            </div>
            <div>
              <span className="label-caps">ETA</span>
              <strong>{record.timeToReach.toFixed(1)} min</strong>
            </div>
            <p className="muted">{record.reasoning}</p>
          </div>
        ))}
      </Card>
    </section>
  );
}

function Field({ label, ...props }) {
  return (
    <label>
      <span className="label-caps">{label}</span>
      <input className="input" required style={{ marginTop: 8 }} {...props} />
    </label>
  );
}

function Detail({ icon: Icon, label, value }) {
  return (
    <div>
      <Icon size={18} style={{ color: "var(--primary)" }} />
      <span className="label-caps">{label}</span>
      <strong>{value}</strong>
    </div>
  );
}

function scoreRoute(route) {
  const score = Math.max(1, 100 - route.totalDistance * 3 - route.timeToReach * 1.4);
  return `${Math.round(score)}%`;
}
