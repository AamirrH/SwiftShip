# SwiftShip Frontend Service

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
VITE_AUTH_BASE_URL=http://localhost:9090
```

The backend gateway routes auth, product, order, customer, warehouse, routing, tracking, and
notification traffic through port `9090`. Login responses return an access token that the frontend
stores locally and sends as a `Bearer` token on gateway-protected requests.

Google OAuth starts at `${VITE_AUTH_BASE_URL}/oauth2/authorization/google` and returns with an
`accessToken` query parameter, which the app stores automatically. If the gateway does not yet route
`/oauth2/**` and `/login/oauth2/**`, point `VITE_AUTH_BASE_URL` directly at Auth-Service during
local OAuth testing.

## Current Customer Flow

- Browse products from `/products` with mock fallback data.
- Add products to a persistent local cart.
- Login or signup through `/auth/login`, `/auth/signup`, or Google OAuth.
- Submit checkout payloads to `/orders/createOrder`.
- View order history, tracking, and notifications through gateway routes.
- Use admin panels for warehouse and route operations when logged in as an admin.
