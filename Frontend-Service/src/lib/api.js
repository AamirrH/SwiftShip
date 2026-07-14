const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:9090";
const AUTH_BASE_URL = import.meta.env.VITE_AUTH_BASE_URL ?? "http://localhost:9040";
const ACCESS_TOKEN_STORAGE_KEY = "swiftship.accessToken";

export function getAccessToken() {
  return window.localStorage.getItem(ACCESS_TOKEN_STORAGE_KEY);
}

export function setAccessToken(accessToken) {
  if (!accessToken) return;
  window.localStorage.setItem(ACCESS_TOKEN_STORAGE_KEY, accessToken);
}

export function clearAccessToken() {
  window.localStorage.removeItem(ACCESS_TOKEN_STORAGE_KEY);
}

async function request(path, options = {}) {
  const accessToken = getAccessToken();
  const headers = {
    "Content-Type": "application/json",
    ...options.headers,
  };

  if (accessToken) {
    headers.Authorization = `Bearer ${accessToken}`;
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    credentials: "include",
    ...options,
    headers,
  });

  if (!response.ok) {
    let responseBody = null;
    try {
      responseBody = await response.json();
    } catch {
      responseBody = null;
    }

    const error = new Error(`SwiftShip API error ${response.status}`);
    error.status = response.status;
    error.body = responseBody;
    error.messageFromServer = responseBody?.message;
    throw error;
  }

  if (response.status === 204) {
    return null;
  }

  return response.json();
}

export const api = {
  getGoogleOAuthUrl: () => `${AUTH_BASE_URL}/oauth2/authorization/google`,
  getProducts: () => request("/products"),
  getProduct: (id) => request(`/products/${id}`),
  createOrder: (payload) =>
    request("/orders/createOrder", {
      method: "POST",
      body: JSON.stringify(payload),
    }),
  getOrders: () => request("/orders"),
  getCustomerAddresses: (customerId) => request(`/customers/${customerId}/addresses`),
  getTracking: (orderNumber) => request(`/tracking/orders/${orderNumber}`),
  getWarehouses: () => request("/admin/warehouses"),
  getWarehouse: (id) => request(`/admin/warehouses/${id}`),
  createWarehouse: (payload) =>
    request("/admin/warehouses", {
      method: "POST",
      body: JSON.stringify(payload),
    }),
  updateWarehouse: (id, payload) =>
    request(`/admin/warehouses/${id}`, {
      method: "PATCH",
      body: JSON.stringify(payload),
    }),
  deleteWarehouse: (id) =>
    request(`/admin/warehouses/${id}`, {
      method: "DELETE",
    }),
  findNearestWarehouse: ({ lat, lon }) => request(`/admin/warehouses/nearest?lat=${lat}&lon=${lon}`),
  calculateRoutes: (payload) =>
    request("/routes", {
      method: "POST",
      body: JSON.stringify(payload),
    }),
  getCustomerNotifications: (customerId) =>
    request(`/notifications/customer/${customerId}`),
  getUnreadCustomerNotifications: (customerId) =>
    request(`/notifications/customer/${customerId}/unread`),
  markNotificationRead: (notificationId) =>
    request(`/notifications/${notificationId}/read`, {
      method: "PATCH",
    }),
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
