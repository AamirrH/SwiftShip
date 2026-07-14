import { Activity, ArrowLeft, RefreshCw, Server } from "lucide-react";
import { useEffect, useState } from "react";
import { Button } from "../components/ui/Button.jsx";
import { Card } from "../components/ui/Card.jsx";
import { StatusBadge } from "../components/ui/StatusBadge.jsx";
import { api } from "../lib/api.js";

export function HealthPage({ onNavigate }) {
  const [health, setHealth] = useState(null);
  const [status, setStatus] = useState("loading");
  const [errorMessage, setErrorMessage] = useState("");

  async function loadHealth() {
    setStatus("loading");
    setErrorMessage("");
    try {
      const response = await api.getPlatformHealth();
      setHealth(response);
      setStatus("ready");
    } catch (error) {
      setStatus("error");
      setErrorMessage(
        error.status === 403
          ? "Admin access is required to view platform health."
          : error.status
          ? `Gateway returned ${error.status} for platform health.`
          : "Gateway is not reachable."
      );
    }
  }

  useEffect(() => {
    loadHealth();
  }, []);

  const checks = health?.checks ?? [];
  const upCount = checks.filter((check) => check.status === "UP").length;

  return (
    <main className="health-page">
      <header className="health-header">
        <div>
          <span className="label-caps">SwiftShip Operations</span>
          <h1 className="page-title">Platform health</h1>
          <p className="muted">Gateway-owned checks for services, Kafka, Redis, and database connectivity.</p>
        </div>
        <div className="health-actions">
          <Button variant="secondary" onClick={() => onNavigate("home")}>
            <ArrowLeft size={18} /> Back to app
          </Button>
          <Button disabled={status === "loading"} onClick={loadHealth}>
            <RefreshCw size={18} /> Refresh
          </Button>
        </div>
      </header>

      <section className="health-summary">
        <Card>
          <Activity size={24} style={{ color: "var(--primary)" }} />
          <span className="label-caps">Overall</span>
          <h2 className="section-title">{health?.overallStatus ?? "Checking"}</h2>
        </Card>
        <Card>
          <Server size={24} style={{ color: "var(--secondary)" }} />
          <span className="label-caps">Reachable</span>
          <h2 className="section-title">{upCount}/{checks.length || "-"}</h2>
        </Card>
        <Card>
          <RefreshCw size={24} style={{ color: "var(--tertiary)" }} />
          <span className="label-caps">Checked at</span>
          <h2 className="section-title">{formatCheckedAt(health?.checkedAt)}</h2>
        </Card>
      </section>

      {status === "error" && (
        <Card>
          <StatusBadge variant="danger">Unavailable</StatusBadge>
          <p className="muted">{errorMessage}</p>
        </Card>
      )}

      <section className="health-grid">
        {checks.map((check) => (
          <Card className="health-check-card" key={`${check.type}-${check.name}`}>
            <div className="health-check-header">
              <div>
                <span className="label-caps">{check.type}</span>
                <h2 className="section-title">{check.name}</h2>
              </div>
              <StatusBadge variant={check.status === "UP" ? "success" : "danger"}>{check.status}</StatusBadge>
            </div>
            <p className="muted">{check.target}</p>
            <div className="health-meta">
              <span>{check.latencyMs ?? "-"} ms</span>
              {check.statusCode && <span>HTTP {check.statusCode}</span>}
            </div>
            <p className="muted">{check.message}</p>
          </Card>
        ))}
      </section>
    </main>
  );
}

function formatCheckedAt(value) {
  if (!value) return "-";
  return new Date(value).toLocaleTimeString([], {
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
  });
}
