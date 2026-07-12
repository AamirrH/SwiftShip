import { MapPin, Plus, Radar, Warehouse } from "lucide-react";
import { useMemo, useState } from "react";
import { Button } from "../components/ui/Button.jsx";
import { Card } from "../components/ui/Card.jsx";
import { StatusBadge } from "../components/ui/StatusBadge.jsx";
import { useApiResource } from "../hooks/useApiResource.js";
import { api } from "../lib/api.js";
import { mockWarehouses } from "../data/mockData.js";

export function AdminWarehousesPage() {
  const { data, status } = useApiResource(api.getWarehouses, mockWarehouses, []);
  const [localWarehouses, setLocalWarehouses] = useState([]);
  const [selectedWarehouseId, setSelectedWarehouseId] = useState(data[0]?.id);
  const [nearestWarehouse, setNearestWarehouse] = useState(null);
  const [formStatus, setFormStatus] = useState("idle");

  const warehouses = useMemo(() => mergeWarehouses(data, localWarehouses), [data, localWarehouses]);
  const selectedWarehouse = warehouses.find((warehouse) => warehouse.id === selectedWarehouseId) ?? warehouses[0];
  const activeCount = warehouses.filter((warehouse) => warehouse.active).length;
  const totalCapacity = warehouses.reduce((sum, warehouse) => sum + Number(warehouse.capacity ?? 0), 0);

  async function handleCreateWarehouse(event) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const payload = {
      warehouseName: form.get("warehouseName"),
      city: form.get("city"),
      lat: Number(form.get("lat")),
      lng: Number(form.get("lng")),
      capacity: Number(form.get("capacity")),
      active: form.get("active") === "on",
    };

    setFormStatus("saving");
    try {
      const createdWarehouse = await api.createWarehouse(payload);
      setLocalWarehouses((current) => [...current, createdWarehouse]);
      setSelectedWarehouseId(createdWarehouse.id);
      setFormStatus("saved");
      event.currentTarget.reset();
    } catch {
      const draftWarehouse = {
        ...payload,
        id: crypto.randomUUID(),
      };
      setLocalWarehouses((current) => [...current, draftWarehouse]);
      setSelectedWarehouseId(draftWarehouse.id);
      setFormStatus("offline");
    }
  }

  async function handleNearest(event) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const coords = {
      lat: Number(form.get("lat")),
      lon: Number(form.get("lon")),
    };

    try {
      setNearestWarehouse(await api.findNearestWarehouse(coords));
    } catch {
      setNearestWarehouse(findNearestMockWarehouse(warehouses, coords));
    }
  }

  return (
    <section className="page">
      <div className="page-header">
        <div>
          <span className="label-caps">Admin warehouse operations</span>
          <h1 className="page-title">Warehouse control room</h1>
          <p className="muted">
            {status === "fallback"
              ? "Showing local sample warehouses until warehouse-service is reachable."
              : "Synced from `/admin/warehouses`."}
          </p>
        </div>
      </div>

      <div className="grid three" style={{ marginBottom: 24 }}>
        <SummaryCard icon={Warehouse} label="Warehouses" value={warehouses.length} />
        <SummaryCard icon={Radar} label="Active" value={activeCount} />
        <SummaryCard icon={Plus} label="Total capacity" value={totalCapacity.toLocaleString("en-IN")} />
      </div>

      <div className="admin-workspace">
        <Card padded={false}>
          {warehouses.map((warehouse) => (
            <div
              className={`admin-row ${selectedWarehouse?.id === warehouse.id ? "active" : ""}`}
              key={warehouse.id}
              onClick={() => setSelectedWarehouseId(warehouse.id)}
            >
              <div className="admin-row-icon">
                <Warehouse size={22} />
              </div>
              <div>
                <h3 className="section-title" style={{ fontSize: 19, margin: 0 }}>{warehouse.warehouseName}</h3>
                <p className="muted" style={{ margin: "5px 0 8px" }}>
                  {warehouse.city} · {warehouse.lat.toFixed(4)}, {warehouse.lng.toFixed(4)}
                </p>
                <StatusBadge>{warehouse.active ? "Active" : "Inactive"}</StatusBadge>
              </div>
              <strong>{Number(warehouse.capacity).toLocaleString("en-IN")}</strong>
            </div>
          ))}
        </Card>

        <div className="grid" style={{ gap: 18 }}>
          <Card>
            <span className="label-caps">Selected warehouse</span>
            <h2 className="section-title" style={{ fontSize: 26, margin: "8px 0" }}>
              {selectedWarehouse?.warehouseName ?? "No warehouse selected"}
            </h2>
            {selectedWarehouse && (
              <div className="admin-detail-grid">
                <Detail label="City" value={selectedWarehouse.city} />
                <Detail label="Capacity" value={Number(selectedWarehouse.capacity).toLocaleString("en-IN")} />
                <Detail label="Latitude" value={selectedWarehouse.lat.toFixed(5)} />
                <Detail label="Longitude" value={selectedWarehouse.lng.toFixed(5)} />
              </div>
            )}
          </Card>

          <Card>
            <span className="label-caps">Nearest warehouse tester</span>
            <form className="compact-form" onSubmit={handleNearest}>
              <Field defaultValue="12.9352" label="Customer lat" name="lat" step="0.0001" type="number" />
              <Field defaultValue="77.6245" label="Customer lon" name="lon" step="0.0001" type="number" />
              <Button>
                <MapPin size={18} />
                Find nearest
              </Button>
            </form>
            {nearestWarehouse && (
              <div className="nearest-result">
                <strong>{nearestWarehouse.warehouseName}</strong>
                <span>{nearestWarehouse.city}</span>
              </div>
            )}
          </Card>

          <Card>
            <span className="label-caps">Create warehouse</span>
            <form className="admin-form" onSubmit={handleCreateWarehouse}>
              <Field label="Warehouse name" name="warehouseName" placeholder="Bellandur Micro Hub" />
              <Field label="City" name="city" placeholder="Bengaluru" />
              <Field label="Latitude" name="lat" placeholder="12.9352" step="0.0001" type="number" />
              <Field label="Longitude" name="lng" placeholder="77.6245" step="0.0001" type="number" />
              <Field label="Capacity" name="capacity" placeholder="6000" type="number" />
              <label className="toggle-row">
                <input defaultChecked name="active" type="checkbox" />
                Active warehouse
              </label>
              <Button>
                <Plus size={18} />
                Save warehouse
              </Button>
            </form>
            {formStatus === "offline" && (
              <p className="muted">Warehouse-service is offline, so this was added locally for preview.</p>
            )}
          </Card>
        </div>
      </div>
    </section>
  );
}

function SummaryCard({ icon: Icon, label, value }) {
  return (
    <Card>
      <Icon size={22} style={{ color: "var(--primary)" }} />
      <div className="label-caps" style={{ marginTop: 12 }}>{label}</div>
      <div className="section-title" style={{ fontSize: 32, marginTop: 6 }}>{value}</div>
    </Card>
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

function Detail({ label, value }) {
  return (
    <div>
      <span className="label-caps">{label}</span>
      <strong>{value}</strong>
    </div>
  );
}

function mergeWarehouses(remoteWarehouses, localWarehouses) {
  const warehouseMap = new Map();
  [...remoteWarehouses, ...localWarehouses].forEach((warehouse) => {
    warehouseMap.set(warehouse.id, warehouse);
  });
  return Array.from(warehouseMap.values());
}

function findNearestMockWarehouse(warehouses, coords) {
  return warehouses
    .filter((warehouse) => warehouse.active)
    .map((warehouse) => ({
      ...warehouse,
      distance: Math.hypot(warehouse.lat - coords.lat, warehouse.lng - coords.lon),
    }))
    .sort((a, b) => a.distance - b.distance)[0];
}
