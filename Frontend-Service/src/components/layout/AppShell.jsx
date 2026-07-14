import {
  Bell,
  Boxes,
  Building2,
  Activity,
  Home,
  LogIn,
  LogOut,
  MapPinned,
  PackageCheck,
  Route,
  Search,
  Settings,
  ShoppingCart,
  Truck,
  UserRound,
  Zap,
} from "lucide-react";
import { Button } from "../ui/Button.jsx";

const navItems = [
  { id: "home", label: "Home", icon: Home },
  { id: "catalog", label: "Catalog", icon: Boxes },
  { id: "orders", label: "Orders", icon: Truck },
  { id: "tracking", label: "Track", icon: MapPinned },
  { id: "notifications", label: "Alerts", icon: Bell },
  { id: "account", label: "Account", icon: UserRound },
];

const adminNavItems = [
  { id: "adminWarehouses", label: "Warehouses", icon: Building2 },
  { id: "adminRoutes", label: "Routes", icon: Route },
  { id: "health", label: "Health", icon: Activity },
];

export function AppShell({ activePage, authUser, cartCount, children, notificationCount = 0, onLogout, onNavigate, title }) {
  const isAdmin = authUser?.role === "ADMIN";
  const primaryNavItems = authUser
    ? navItems
    : [...navItems.filter((item) => item.id !== "account"), { id: "auth", label: "Sign in", icon: LogIn }];
  const visibleAdminNavItems = isAdmin ? adminNavItems : [];

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <div className="brand-mark">
            <Zap size={24} fill="currentColor" />
          </div>
          <div>
            <div className="brand-title">SwiftShip</div>
            <span className="label-caps">Fast Delivery</span>
          </div>
        </div>

        <nav className="nav-list" aria-label="Primary navigation">
          {primaryNavItems.map((item) => (
            <button
              className={`nav-item ${activePage === item.id ? "active" : ""}`}
              key={item.id}
              onClick={() => onNavigate(item.id)}
            >
              <item.icon size={20} />
              {item.label}
            </button>
          ))}
          {authUser && (
            <button className="nav-item" onClick={onLogout}>
              <LogOut size={20} />
              Logout
            </button>
          )}
          {visibleAdminNavItems.length > 0 && (
            <>
              <div className="nav-section-label">Admin</div>
              {visibleAdminNavItems.map((item) => (
                <button
                  className={`nav-item ${activePage === item.id ? "active" : ""}`}
                  key={item.id}
                  onClick={() => onNavigate(item.id)}
                >
                  <item.icon size={20} />
                  {item.label}
                </button>
              ))}
            </>
          )}
        </nav>

        <Button onClick={() => onNavigate("catalog")}>
          <PackageCheck size={18} />
          Start Order
        </Button>
      </aside>

      <main className="content">
        <header className="topbar">
          <div>
            <span className="label-caps">Customer Console</span>
            <div className="section-title" style={{ fontSize: 22, fontWeight: 800 }}>
              {title}
            </div>
          </div>
          <div className="topbar-search" style={{ width: "min(420px, 38vw)" }}>
            <div style={{ position: "relative" }}>
              <Search
                size={18}
                style={{ left: 14, position: "absolute", top: "50%", transform: "translateY(-50%)" }}
              />
              <input className="input" placeholder="Search products, orders, tracking..." style={{ paddingLeft: 42 }} />
            </div>
          </div>
          <div style={{ alignItems: "center", display: "flex", gap: 10 }}>
            {authUser ? (
              <button className="user-pill" onClick={() => onNavigate("account")} title="Account">
                <UserRound size={17} />
                <span>{authUser.username}</span>
              </button>
            ) : (
              <button className="user-pill" onClick={() => onNavigate("auth")} title="Sign in">
                <LogIn size={17} />
                <span>Sign in</span>
              </button>
            )}
            <button className="icon-button" onClick={() => onNavigate("cart")} title="Cart">
              <ShoppingCart size={19} />
              {cartCount > 0 && <span className="label-caps" style={{ color: "var(--primary)" }}>{cartCount}</span>}
            </button>
            <button className="icon-button" onClick={() => onNavigate("notifications")} title="Notifications">
              <Bell size={19} />
              {notificationCount > 0 && (
                <span className="label-caps" style={{ color: "var(--primary)" }}>
                  {notificationCount}
                </span>
              )}
            </button>
            <button className="icon-button" onClick={() => onNavigate("account")} title="Settings">
              <Settings size={19} />
            </button>
          </div>
        </header>

        {children}
      </main>

      <nav className="bottom-nav" aria-label="Mobile navigation">
        {primaryNavItems.map((item) => (
          <button
            className={activePage === item.id ? "active" : ""}
            key={item.id}
            onClick={() => onNavigate(item.id)}
            title={item.label}
          >
            <item.icon size={20} />
          </button>
        ))}
        {authUser && (
          <button onClick={onLogout} title="Logout">
            <LogOut size={20} />
          </button>
        )}
        {visibleAdminNavItems.map((item) => (
          <button
            className={activePage === item.id ? "active" : ""}
            key={item.id}
            onClick={() => onNavigate(item.id)}
            title={item.label}
          >
            <item.icon size={20} />
          </button>
        ))}
      </nav>
    </div>
  );
}
