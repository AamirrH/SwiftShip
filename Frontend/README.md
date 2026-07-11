# SwiftShip Frontend

Customer-facing React UI for SwiftShip. The app uses the Google Stitch visual direction:
dark logistics console, Signal Orange actions, Sage/Amber status accents, Manrope headings,
and Inter body text.

## Run Locally

```bash
npm install
npm run dev
```

Vite starts on `http://localhost:5173` by default. If that port is occupied, it will pick the
next available port.

## Build

```bash
npm run build
```

## Environment

Copy `.env.example` to `.env` when you need to change the backend gateway URL.

```bash
VITE_API_BASE_URL=http://localhost:9090
VITE_NOTIFICATION_API_BASE_URL=http://localhost:8086
```

The current backend gateway routes product, order, customer, and auth traffic through port `9090`.
Notifications can be read directly from `notification-service` on port `8086` until `/notifications/**`
is added to the gateway.

## Current Customer Flow

- Browse products from `/products` with mock fallback data.
- Add products to a persistent local cart.
- Submit checkout payloads to `/orders/createOrder`.
- View order history and tracking screens while tracking integration continues to evolve.

Admin-facing panels are intentionally not included yet.
