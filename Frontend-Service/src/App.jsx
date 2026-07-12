import { useEffect, useMemo, useState } from "react";
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
import { mockCart, mockNotifications, mockProducts } from "./data/mockData.js";
import { useLocalStorageState } from "./hooks/useLocalStorageState.js";
import { useApiResource } from "./hooks/useApiResource.js";
import { api } from "./lib/api.js";

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
  account: "Account",
  auth: "Sign in",
};

export default function App() {
  const [activePage, setActivePage] = useState(() => getPageFromHash());
  const [selectedProductId, setSelectedProductId] = useState(mockProducts[0].id);
  const [cart, setCart] = useLocalStorageState("swiftship.cart", mockCart);
  const { data: unreadNotifications } = useApiResource(
    () => api.getUnreadCustomerNotifications(1),
    mockNotifications.filter((notification) => notification.readStatus === "UNREAD"),
    []
  );

  useEffect(() => {
    function syncPageFromHash() {
      setActivePage(getPageFromHash());
    }

    window.addEventListener("hashchange", syncPageFromHash);
    return () => window.removeEventListener("hashchange", syncPageFromHash);
  }, []);

  const selectedProduct = useMemo(
    () => mockProducts.find((product) => product.id === selectedProductId) ?? mockProducts[0],
    [selectedProductId]
  );

  function navigate(page) {
    window.location.hash = page;
    setActivePage(page);
    window.scrollTo({ top: 0, behavior: "smooth" });
  }

  function openProduct(productId) {
    setSelectedProductId(productId);
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
  }

  const pages = {
    home: <HomePage onNavigate={navigate} onOpenProduct={openProduct} onAddToCart={addToCart} />,
    catalog: <CatalogPage onOpenProduct={openProduct} onAddToCart={addToCart} />,
    product: <ProductPage product={selectedProduct} onBack={() => navigate("catalog")} onAddToCart={addToCart} />,
    cart: <CartPage cart={cart} setCart={setCart} onNavigate={navigate} />,
    orders: <OrdersPage onNavigate={navigate} />,
    tracking: <TrackingPage />,
    notifications: <NotificationsPage />,
    adminWarehouses: <AdminWarehousesPage />,
    adminRoutes: <AdminRoutesPage />,
    account: <AccountPage />,
    auth: <AuthPage onNavigate={navigate} />,
  };

  return (
    <AppShell
      activePage={activePage}
      cartCount={cart.reduce((total, item) => total + item.quantity, 0)}
      notificationCount={unreadNotifications.length}
      onNavigate={navigate}
      title={pageTitles[activePage]}
    >
      {pages[activePage]}
    </AppShell>
  );
}

function getPageFromHash() {
  const page = window.location.hash.replace("#", "");
  return pageTitles[page] ? page : "home";
}
