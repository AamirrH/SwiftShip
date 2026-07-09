import { useMemo, useState } from "react";
import { AppShell } from "./components/layout/AppShell.jsx";
import { HomePage } from "./pages/HomePage.jsx";
import { CatalogPage } from "./pages/CatalogPage.jsx";
import { ProductPage } from "./pages/ProductPage.jsx";
import { CartPage } from "./pages/CartPage.jsx";
import { OrdersPage } from "./pages/OrdersPage.jsx";
import { TrackingPage } from "./pages/TrackingPage.jsx";
import { AccountPage } from "./pages/AccountPage.jsx";
import { AuthPage } from "./pages/AuthPage.jsx";
import { mockCart, mockProducts } from "./data/mockData.js";

const pageTitles = {
  home: "Home",
  catalog: "Catalog",
  product: "Product",
  cart: "Cart",
  orders: "Orders",
  tracking: "Tracking",
  account: "Account",
  auth: "Sign in",
};

export default function App() {
  const [activePage, setActivePage] = useState("home");
  const [selectedProductId, setSelectedProductId] = useState(mockProducts[0].id);
  const [cart, setCart] = useState(mockCart);

  const selectedProduct = useMemo(
    () => mockProducts.find((product) => product.id === selectedProductId) ?? mockProducts[0],
    [selectedProductId]
  );

  function navigate(page) {
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
    account: <AccountPage />,
    auth: <AuthPage onNavigate={navigate} />,
  };

  return (
    <AppShell
      activePage={activePage}
      cartCount={cart.reduce((total, item) => total + item.quantity, 0)}
      onNavigate={navigate}
      title={pageTitles[activePage]}
    >
      {pages[activePage]}
    </AppShell>
  );
}
