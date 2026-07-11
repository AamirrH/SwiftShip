const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:9090";
const NOTIFICATION_API_BASE_URL = import.meta.env.VITE_NOTIFICATION_API_BASE_URL ?? "http://localhost:8086";

async function request(path, options = {}, baseUrl = API_BASE_URL) {
  const response = await fetch(`${baseUrl}${path}`, {
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
      ...options.headers,
    },
    ...options,
  });

  if (!response.ok) {
    throw new Error(`SwiftShip API error ${response.status}`);
  }

  if (response.status === 204) {
    return null;
  }

  return response.json();
}

export const api = {
  getProducts: () => request("/products"),
  getProduct: (id) => request(`/products/${id}`),
  createOrder: (payload) =>
    request("/orders/createOrder", {
      method: "POST",
      body: JSON.stringify(payload),
    }),
  getOrders: () => request("/orders"),
  getTracking: (orderNumber) => request(`/tracking/${orderNumber}`),
  getCustomerNotifications: (customerId) =>
    request(`/notifications/customer/${customerId}`, {}, NOTIFICATION_API_BASE_URL),
  getUnreadCustomerNotifications: (customerId) =>
    request(`/notifications/customer/${customerId}/unread`, {}, NOTIFICATION_API_BASE_URL),
  markNotificationRead: (notificationId) =>
    request(
      `/notifications/${notificationId}/read`,
      {
        method: "PATCH",
      },
      NOTIFICATION_API_BASE_URL
    ),
  login: (payload) =>
    request("/auth/login", {
      method: "POST",
      body: JSON.stringify(payload),
    }),
  signup: (payload) =>
    request("/auth/signup", {
      method: "POST",
      body: JSON.stringify(payload),
    }),
};
