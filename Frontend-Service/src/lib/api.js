const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:9090";
const ACCESS_TOKEN_STORAGE_KEY = "swiftship.accessToken";
const AUTH_USER_STORAGE_KEY = "swiftship.authUser";

export function getAccessToken() {
  return window.localStorage.getItem(ACCESS_TOKEN_STORAGE_KEY);
}

export function setAccessToken(accessToken) {
  if (!accessToken) return;
  window.localStorage.setItem(ACCESS_TOKEN_STORAGE_KEY, accessToken);
}

export function getAuthUser() {
  const storedUser = window.localStorage.getItem(AUTH_USER_STORAGE_KEY);
  if (!storedUser) return null;

  try {
    return JSON.parse(storedUser);
  } catch {
    window.localStorage.removeItem(AUTH_USER_STORAGE_KEY);
    return null;
  }
}

export function setAuthUser(authUser) {
  if (!authUser?.username) return;
  window.localStorage.setItem(AUTH_USER_STORAGE_KEY, JSON.stringify(authUser));
}

export function buildAuthUser({ accessToken, email, provider, username }) {
  const tokenPayload = decodeJwtPayload(accessToken);
  return {
    email: email ?? tokenPayload?.email,
    provider,
    role: tokenPayload?.role ?? "CUSTOMER",
    userId: tokenPayload?.sub,
    username: username ?? tokenPayload?.email ?? "SwiftShip user",
  };
}

export function clearAccessToken() {
  window.localStorage.removeItem(ACCESS_TOKEN_STORAGE_KEY);
}

export function clearAuthSession() {
  window.localStorage.removeItem(ACCESS_TOKEN_STORAGE_KEY);
  window.localStorage.removeItem(AUTH_USER_STORAGE_KEY);
}

function decodeJwtPayload(accessToken) {
  if (!accessToken) return null;
  const [, payload] = accessToken.split(".");
  if (!payload) return null;

  try {
    const normalizedPayload = payload.replace(/-/g, "+").replace(/_/g, "/");
    const decodedPayload = window.atob(normalizedPayload.padEnd(Math.ceil(normalizedPayload.length / 4) * 4, "="));
    return JSON.parse(decodedPayload);
  } catch {
    return null;
  }
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
  getGoogleOAuthUrl: () => `${API_BASE_URL}/oauth2/authorization/google`,
  getProducts: () => request("/products"),
  getProduct: (id) => request(`/products/${id}`),
  createOrder: (payload) =>
    request("/orders/createOrder", {
      method: "POST",
      body: JSON.stringify(payload),
    }),
  cancelOrder: (orderId) =>
    request(`/orders/cancelOrder/${orderId}`, {
      method: "PUT",
    }),
  getOrders: () => request("/orders"),
  getCustomerAddresses: (customerId) => request(`/customers/${customerId}/addresses`),
  createCustomerAddress: (customerId, payload) =>
    request(`/customers/${customerId}/addresses`, {
      method: "POST",
      body: JSON.stringify(payload),
    }),
  updateCustomerAddress: (customerId, addressId, payload) =>
    request(`/customers/${customerId}/addresses/${addressId}`, {
      method: "PATCH",
      body: JSON.stringify(payload),
    }),
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
