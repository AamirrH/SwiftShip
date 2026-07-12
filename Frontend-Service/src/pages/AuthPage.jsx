import { LogIn, UserPlus } from "lucide-react";
import { useState } from "react";
import { Button } from "../components/ui/Button.jsx";
import { Card } from "../components/ui/Card.jsx";
import { api, setAccessToken } from "../lib/api.js";

export function AuthPage({ onNavigate }) {
  const [mode, setMode] = useState("login");
  const [message, setMessage] = useState("");

  async function handleSubmit(event) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const payload = Object.fromEntries(form.entries());
    try {
      if (mode === "login") {
        const loginResponse = await api.login({ username: payload.username, password: payload.password });
        setAccessToken(loginResponse.accessToken);
      } else {
        await api.signup(payload);
      }
      setMessage("Connected to SwiftShip successfully.");
      onNavigate("home");
    } catch {
      setMessage("Backend is not reachable yet. The frontend is ready for the gateway.");
    }
  }

  return (
    <section className="page">
      <div className="split" style={{ alignItems: "center" }}>
        <div>
          <span className="label-caps">Secure access</span>
          <h1 className="page-title">Sign in to your SwiftShip account</h1>
          <p className="muted">Uses `/auth/login` and `/auth/signup` through the API gateway with refresh-token cookie support.</p>
        </div>
        <Card>
          <div style={{ display: "flex", gap: 8, marginBottom: 20 }}>
            <button className={`button ${mode === "login" ? "primary" : "secondary"}`} onClick={() => setMode("login")}>
              <LogIn size={17} /> Login
            </button>
            <button className={`button ${mode === "signup" ? "primary" : "secondary"}`} onClick={() => setMode("signup")}>
              <UserPlus size={17} /> Signup
            </button>
          </div>
          <form onSubmit={handleSubmit} style={{ display: "grid", gap: 14 }}>
            {mode === "signup" && <Field label="Name" name="name" placeholder="Your name" />}
            <Field label="Username" name="username" placeholder="aamir" />
            {mode === "signup" && <Field label="Email" name="email" placeholder="aamir@example.com" />}
            <Field label="Password" name="password" placeholder="Password" type="password" />
            <Button>{mode === "login" ? "Login" : "Create account"}</Button>
          </form>
          {message && <p className="muted" style={{ marginBottom: 0 }}>{message}</p>}
        </Card>
      </div>
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
