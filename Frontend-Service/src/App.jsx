import { useEffect, useRef, useState } from "react";
import { AppShell } from "./components/layout/AppShell.jsx";
import { HomePage } from "./pages/HomePage.jsx";
import { CatalogPage } from "./pages/CatalogPage.jsx";
import { ProductPage } from "./pages/ProductPage.jsx";
import { CartPage } from "./pages/CartPage.jsx";
import { OrdersPage } from "./pages/OrdersPage.jsx";
import { TrackingPage } from "./pages/TrackingPage.jsx";
import { AccountPage } from "./pages/AccountPage.jsx";
import { AuthPage } from "./pages/AuthPage.jsx";
import { NotificationsPage } from "./pages/NotificationsPage.jsx";
import { AdminWarehousesPage } from "./pages/AdminWarehousesPage.jsx";
import { AdminRoutesPage } from "./pages/AdminRoutesPage.jsx";
import { HealthPage } from "./pages/HealthPage.jsx";
import { mockCart, mockNotifications, mockProducts } from "./data/mockData.js";
import { useLocalStorageState } from "./hooks/useLocalStorageState.js";
import { useApiResource } from "./hooks/useApiResource.js";
import { api, buildAuthUser, clearAuthSession, getAuthUser, setAccessToken, setAuthUser } from "./lib/api.js";

const pageTitles = {
  home: "Home",
  catalog: "Catalog",
  product: "Product",
  cart: "Cart",
  orders: "Orders",
  tracking: "Tracking",
  notifications: "Notifications",
  adminWarehouses: "Admin Warehouses",
  adminRoutes: "Admin Routes",
  health: "Platform Health",
  account: "Account",
  auth: "Sign in",
};

const DEFAULT_CUSTOMER_ID = 7;
const adminPages = new Set(["adminWarehouses", "adminRoutes", "health"]);

export default function App() {
  const [activePage, setActivePage] = useState(() => getPageFromHash());
  const [selectedProduct, setSelectedProduct] = useState(mockProducts[0]);
  const [selectedOrderNumber, setSelectedOrderNumber] = useState(null);
  const [authUser, setCurrentAuthUser] = useState(() => getAuthUser());
  const [toastMessage, setToastMessage] = useState("");
  const toastTimeoutRef = useRef(null);
  const [cart, setCart] = useLocalStorageState("swiftship.cart", mockCart);
  const { data: unreadNotifications } = useApiResource(
    () => api.getUnreadCustomerNotifications(DEFAULT_CUSTOMER_ID),
    mockNotifications.filter((notification) => notification.readStatus === "UNREAD"),
    []
  );

  useEffect(() => {
    const queryParams = new URLSearchParams(window.location.search);
    const oauthAccessToken = queryParams.get("accessToken");
    const oauthUsername = queryParams.get("username");

    if (oauthAccessToken) {
      setAccessToken(oauthAccessToken);
      if (oauthUsername) {
        saveAuthUser(buildAuthUser({ accessToken: oauthAccessToken, provider: "Google", username: oauthUsername }));
      }
      window.history.replaceState({}, document.title, "/#home");
      setActivePage("home");
    }

    function syncPageFromHash() {
      setActivePage(getPageFromHash());
    }

    window.addEventListener("hashchange", syncPageFromHash);
    return () => window.removeEventListener("hashchange", syncPageFromHash);
  }, []);

  useEffect(() => {
    if (adminPages.has(activePage) && authUser?.role !== "ADMIN") {
      navigate("home");
    }
  }, [activePage, authUser]);

  function navigate(page) {
    window.location.hash = page;
    setActivePage(page);
    window.scrollTo({ top: 0, behavior: "smooth" });
  }

  function openProduct(product) {
    setSelectedProduct(product);
    navigate("product");
  }

  function addToCart(product, quantity = 1) {
    setCart((current) => {
      const existing = current.find((item) => item.productId === product.id);
      if (existing) {
        return current.map((item) =>
          item.productId === product.id ? { ...item, quantity: item.quantity + quantity } : item
        );
      }
      return [
        ...current,
        {
          id: crypto.randomUUID(),
          productId: product.id,
          name: product.productName,
          price: product.productPrice,
          image: product.image,
          quantity,
        },
      ];
    });
    showToast(`${product.productName} has been added to Cart`);
  }

  function showToast(message) {
    setToastMessage(message);
    window.clearTimeout(toastTimeoutRef.current);
    toastTimeoutRef.current = window.setTimeout(() => setToastMessage(""), 2600);
  }

  function saveAuthUser(nextAuthUser) {
    setAuthUser(nextAuthUser);
    setCurrentAuthUser(nextAuthUser);
  }

  function logout() {
    clearAuthSession();
    setCurrentAuthUser(null);
    navigate("home");
  }

  const pages = {
    home: <HomePage onNavigate={navigate} onOpenProduct={openProduct} onAddToCart={addToCart} />,
    catalog: <CatalogPage onOpenProduct={openProduct} onAddToCart={addToCart} />,
    product: <ProductPage product={selectedProduct} onBack={() => navigate("catalog")} onAddToCart={addToCart} />,
    cart: <CartPage cart={cart} onOrderPlaced={showToast} setCart={setCart} onNavigate={navigate} />,
    orders: <OrdersPage onNavigate={navigate} onOrderCancelled={showToast} onTrackOrder={setSelectedOrderNumber} />,
    tracking: <TrackingPage orderNumber={selectedOrderNumber} />,
    notifications: <NotificationsPage />,
    adminWarehouses: <AdminWarehousesPage />,
    adminRoutes: <AdminRoutesPage />,
    health: <HealthPage onNavigate={navigate} />,
    account: <AccountPage authUser={authUser} />,
    auth: <AuthPage onAuthSuccess={saveAuthUser} onNavigate={navigate} />,
  };

  if (activePage === "health" && authUser?.role === "ADMIN") {
    return <HealthPage onNavigate={navigate} />;
  }

  return (
    <AppShell
      activePage={activePage}
      cartCount={cart.reduce((total, item) => total + item.quantity, 0)}
      notificationCount={unreadNotifications.length}
      authUser={authUser}
      onLogout={logout}
      onNavigate={navigate}
      title={pageTitles[activePage]}
    >
      {pages[activePage]}
      {toastMessage && <div className="app-toast">{toastMessage}</div>}
    </AppShell>
  );
}

function getPageFromHash() {
  const page = window.location.hash.replace("#", "");
  return pageTitles[page] ? page : "home";
}
