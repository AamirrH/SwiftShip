import { Bell, Home, MapPinned, UserRound } from "lucide-react";
import { Button } from "../components/ui/Button.jsx";
import { Card } from "../components/ui/Card.jsx";

export function AccountPage() {
  return (
    <section className="page">
      <div className="page-header">
        <div>
          <span className="label-caps">Customer profile</span>
          <h1 className="page-title">Account settings</h1>
        </div>
        <Button>Save changes</Button>
      </div>

      <div className="split">
        <Card>
          <div style={{ display: "flex", gap: 16, marginBottom: 22 }}>
            <div className="brand-mark"><UserRound size={22} /></div>
            <div>
              <h2 className="section-title" style={{ margin: 0 }}>Aamir Customer</h2>
              <p className="muted" style={{ margin: "4px 0 0" }}>aamir@example.com</p>
            </div>
          </div>
          <div className="grid two">
            <Field label="Full name" value="Aamir Customer" />
            <Field label="Phone" value="+91 98765 43210" />
            <Field label="Email" value="aamir@example.com" />
            <Field label="Membership" value="SwiftShip customer" />
          </div>
        </Card>

        <div className="grid">
          <Preference icon={Home} label="Default address" value="EON Free Zone, Kharadi, Pune" />
          <Preference icon={MapPinned} label="Saved locations" value="Home, Work" />
          <Preference icon={Bell} label="Notifications" value="Order, ETA, delivered" />
        </div>
      </div>
    </section>
  );
}

function Field({ label, value }) {
  return (
    <label>
      <span className="label-caps">{label}</span>
      <input className="input" defaultValue={value} style={{ marginTop: 8 }} />
    </label>
  );
}

function Preference({ icon: Icon, label, value }) {
  return (
    <Card>
      <Icon size={22} style={{ color: "var(--primary)" }} />
      <div className="label-caps" style={{ marginTop: 12 }}>{label}</div>
      <strong>{value}</strong>
    </Card>
  );
}
