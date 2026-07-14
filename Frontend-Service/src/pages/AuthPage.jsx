import {
  ArrowRight,
  BadgeCheck,
  CheckCircle2,
  Chrome,
  KeyRound,
  LogIn,
  Mail,
  ShieldCheck,
  UserRound,
  UserPlus,
} from "lucide-react";
import { useMemo, useState } from "react";
import { Button } from "../components/ui/Button.jsx";
import { Card } from "../components/ui/Card.jsx";
import { api, buildAuthUser, setAccessToken } from "../lib/api.js";

const authBenefits = [
  "Track every order from stock reservation to delivery",
  "Receive in-app ETA, warehouse, and route notifications",
  "Reuse saved customer details across checkout flows",
];

export function AuthPage({ onAuthSuccess, onNavigate }) {
  const [mode, setMode] = useState("login");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [message, setMessage] = useState({ type: "idle", text: "" });
  const title = mode === "login" ? "Welcome back" : "Create customer account";
  const submitLabel = mode === "login" ? "Login securely" : "Create account";

  const helperText = useMemo(
    () =>
      mode === "login"
        ? "Use your SwiftShip username and password, or continue with your Google account."
        : "Customer signup maps to the backend auth DTO with the CUSTOMER role selected for you.",
    [mode]
  );

  function switchMode(nextMode) {
    setMode(nextMode);
    setMessage({ type: "idle", text: "" });
  }

  function handleGoogleAuth() {
    window.location.assign(api.getGoogleOAuthUrl());
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setIsSubmitting(true);
    setMessage({ type: "idle", text: "" });

    const form = new FormData(event.currentTarget);
    const username = String(form.get("username") ?? "").trim();
    const password = String(form.get("password") ?? "");
    const email = String(form.get("email") ?? "").trim();

    try {
      if (mode === "login") {
        const loginResponse = await api.login({ username, password });
        setAccessToken(loginResponse.accessToken);
        onAuthSuccess(buildAuthUser({
          accessToken: loginResponse.accessToken,
          provider: "Password",
          username: loginResponse.username ?? username,
        }));
        setMessage({ type: "success", text: "Logged in successfully. Taking you to SwiftShip." });
        onNavigate("home");
        return;
      }

      await api.signup({
        username,
        email,
        password,
        role: "CUSTOMER",
      });
      const loginResponse = await api.login({ username, password });
      setAccessToken(loginResponse.accessToken);
      onAuthSuccess(buildAuthUser({
        accessToken: loginResponse.accessToken,
        email,
        provider: "Password",
        username: loginResponse.username ?? username,
      }));
      setMessage({ type: "success", text: "Account created and logged in successfully." });
      onNavigate("home");
    } catch (error) {
      const serverMessage = error.messageFromServer;
      setMessage({
        type: "error",
        text:
          serverMessage
            ? serverMessage
            : error.status === 401
            ? "Those credentials were not accepted. Check the username and password."
            : error.status === 403
            ? "You are not allowed to access this auth action."
            : error.status === 409
            ? "A user with that username or email already exists."
            : error.status === 500
            ? "Auth service hit a backend error. Check the Auth-Service logs."
            : "Gateway is not reachable yet, or the auth route is not available through the gateway.",
      });
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <section className="page auth-page">
      <div className="auth-shell">
        <div className="auth-story">
          <span className="label-caps">Customer access</span>
          <h1 className="page-title">Order faster, track cleaner, stay notified.</h1>
          <p className="muted">
            Sign in before checkout so SwiftShip can reserve inventory, attach delivery updates to your account,
            and keep notifications tied to the same customer flow.
          </p>

          <div className="auth-benefits">
            {authBenefits.map((benefit) => (
              <div className="auth-benefit" key={benefit}>
                <CheckCircle2 size={18} />
                <span>{benefit}</span>
              </div>
            ))}
          </div>

          <div className="auth-service-strip">
            <ServiceStep icon={UserRound} label="Customer" value="Profile secured" />
            <ServiceStep icon={BadgeCheck} label="JWT" value="Bearer access" />
            <ServiceStep icon={ShieldCheck} label="Refresh" value="HTTP-only cookie" />
          </div>
        </div>

        <Card className="auth-panel">
          <div className="auth-panel-header">
            <div>
              <span className="label-caps">SwiftShip ID</span>
              <h2 className="section-title">{title}</h2>
              <p className="muted">{helperText}</p>
            </div>
            <div className="auth-panel-icon">
              {mode === "login" ? <LogIn size={22} /> : <UserPlus size={22} />}
            </div>
          </div>

          <div className="auth-mode-toggle" role="tablist" aria-label="Authentication mode">
            <button
              aria-selected={mode === "login"}
              className={mode === "login" ? "active" : ""}
              onClick={() => switchMode("login")}
              role="tab"
              type="button"
            >
              <LogIn size={16} />
              Login
            </button>
            <button
              aria-selected={mode === "signup"}
              className={mode === "signup" ? "active" : ""}
              onClick={() => switchMode("signup")}
              role="tab"
              type="button"
            >
              <UserPlus size={16} />
              Signup
            </button>
          </div>

          <button className="oauth-button" onClick={handleGoogleAuth} type="button">
            <Chrome size={18} />
            Continue with Google
            <ArrowRight size={17} />
          </button>

          <div className="auth-divider">
            <span>or continue with password</span>
          </div>

          <form className="auth-form" onSubmit={handleSubmit}>
            <Field icon={UserRound} label="Username" name="username" placeholder="aamir_customer" />
            {mode === "signup" && (
              <Field icon={Mail} label="Email" name="email" placeholder="aamir@example.com" type="email" />
            )}
            <Field icon={KeyRound} label="Password" name="password" placeholder="Password" type="password" />
            <Button disabled={isSubmitting}>
              {submitLabel}
              <ArrowRight size={17} />
            </Button>
          </form>

          {message.text && <p className={`auth-message ${message.type}`}>{message.text}</p>}
        </Card>
      </div>
    </section>
  );
}

function Field({ icon: Icon, label, ...props }) {
  return (
    <label className="auth-field">
      <span className="label-caps">{label}</span>
      <span>
        <Icon size={18} />
        <input className="input" required {...props} />
      </span>
    </label>
  );
}

function ServiceStep({ icon: Icon, label, value }) {
  return (
    <div>
      <Icon size={19} />
      <span className="label-caps">{label}</span>
      <strong>{value}</strong>
    </div>
  );
}
