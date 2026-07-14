# SwiftShip - Real-Time Order Fulfillment and Delivery Tracking

<img width="3096" height="3072" alt="SwiftShip_Icon" src="https://github.com/user-attachments/assets/1478f836-06ba-4ed1-ac4c-cc42e8c7168c" />

SwiftShip is a Spring Boot microservices project that simulates quick-commerce order fulfillment and delivery tracking. It is built as a portfolio/learning system for distributed backend patterns: API Gateway routing, JWT auth, Kafka event choreography, PostGIS warehouse selection, external route calculation, AI route choice, simulated tracking, Redis live state, notifications, and a React frontend.

The project is currently being prepared for deployment on free-tier infrastructure such as Render, Neon, Aiven Kafka, and a Redis test database, so recent changes deliberately reduce avoidable database/Kafka churn.

## Current State

SwiftShip is now implemented through the full backend order lifecycle:

```text
Customer signs in
  -> browses product catalog
  -> places an order with saved delivery address
  -> Order-Service stores the order as PLACED and publishes ORDER_PLACED
  -> Inventory-Service reserves/reduces stock and publishes ORDER_CONFIRMED
  -> Order-Service marks the order CONFIRMED
  -> Warehouse-Service selects nearest active warehouse using PostGIS
  -> Routing-Service calculates route alternatives and selects a route with Gemini/Spring AI
  -> Tracking-Service creates a tracking session, assigns a simulated driver, and moves delivery state
  -> Notification-Service stores in-app notifications and can send email through Resend
  -> Frontend shows catalog, cart, orders, notifications, tracking snapshot, auth state, and account/address management
```

True browser live tracking over WebSocket is the next planned tracking milestone. The backend currently updates tracking state on a scheduled simulation tick and exposes the latest state through REST/Redis-backed lookup.

## Services

| Service | Local Port | Current Role |
| --- | ---: | --- |
| `Discovery-Service` | `8761` | Eureka service registry |
| `Config-Server` | `9080` | Centralized Spring Cloud Config |
| `API-Gateway` | `9090` | Single frontend entry point, JWT validation, role route protection, OAuth routing |
| `Auth-Service` | `9040` local setup | Signup, login, Google OAuth, JWT access/refresh token generation |
| `Inventory-Service` | `9010` local setup | Product catalog, stock checks, stock reserve/release |
| `Order-Service` | `9020` local setup | Customers, addresses, orders, order items, order cancellation |
| `Notification-Service` | `9050` | In-app notification CRUD, Kafka listeners, Resend email service |
| `Warehouse-Service` | `9060` local setup | Warehouse CRUD, PostGIS nearest active warehouse selection |
| `Routing-Service` | `9070` local setup | OpenRouteService routes, Gemini/Spring AI route choice |
| `Tracking-Service` | `9099` | Driver CRUD, tracking sessions, Redis latest state, scheduled simulated delivery movement |
| `Frontend-Service` | `5173` Vite default | React customer/admin console |

Some local ports are stored in developer-local `application.properties` files and should be moved to environment/config-server values before production deployment.

## Architecture Snapshot

```text
React Frontend
  |
  v
API-Gateway :9090
  |
  |-- /auth/**, /oauth2/**, /login/oauth2/** -> Auth-Service
  |-- /products/**                          -> Inventory-Service
  |-- /orders/**, /customers/**             -> Order-Service
  |-- /admin/warehouses/**                  -> Warehouse-Service
  |-- /routes/**                            -> Routing-Service
  |-- /tracking/**, /drivers/**             -> Tracking-Service
  |-- /notifications/**, /emails/**         -> Notification-Service
  |
  v
Eureka Discovery-Service

Kafka carries lifecycle events.
PostgreSQL/Neon stores service data.
PostGIS powers nearest warehouse selection.
OpenRouteService calculates routes.
Gemini/Spring AI chooses routes.
Redis stores latest tracking state.
```

## Kafka Topics and Free-Tier Constraint

Aiven free tier limits the project to a small number of topics, so SwiftShip groups related events instead of creating one topic per event type.

Current topic style:

```text
order-events
fulfillment-events
tracking-events
```

Representative event flow:

```text
Order-Service      -- ORDER_PLACED       --> order-events
Inventory-Service  -- ORDER_CONFIRMED    --> order-events / fulfillment flow
Warehouse-Service  -- WAREHOUSE_ASSIGNED --> fulfillment-events
Routing-Service    -- ROUTE_CALCULATED   --> fulfillment-events
Tracking-Service   -- ETA_UPDATED        --> tracking-events
Tracking-Service   -- ORDER_DELIVERED    --> tracking-events
```

Notification-Service consumes lifecycle events for in-app/email notifications.

## Implemented Features

- Spring Boot microservices with service discovery through Eureka.
- Spring Cloud Gateway as the frontend-facing entry point.
- JWT login/signup and Google OAuth through the gateway.
- Gateway diagnostics for request route matching and auth filter decisions.
- Role-aware gateway protection for customer/admin routes.
- React frontend auth session state with visible logged-in user and logout.
- Frontend hides admin warehouse/route tabs for customer users.
- Customer account page supports saved address editing.
- Cart, order placement, and order cancellation confirmation messages.
- Daily rotating featured products on the frontend using already-fetched catalog data.
- Order-Service returns grouped order responses so one order renders as one row.
- Order cancellation restores stock through Inventory-Service.
- Inventory-Service owns product catalog and stock mutation.
- Warehouse-Service selects nearest active warehouse using PostGIS.
- Routing-Service integrates OpenRouteService and Gemini/Spring AI.
- Tracking-Service creates sessions from route-calculated events, assigns simulated drivers, and stores latest state in Redis when available.
- Tracking simulation now runs every 60 seconds instead of every 5 seconds to reduce DB/Redis/Kafka load for free-tier deployment.
- Notification-Service stores in-app notifications and has a Resend-backed email sender.

## Frontend

`Frontend-Service` is a React/Vite app.

Implemented screens:

- Home/catalog with daily rotating featured products.
- Product details and cart.
- Checkout with saved address selection.
- Orders page with grouped order rows, tracking navigation, and cancellation.
- Tracking page using initial REST state.
- Notifications page.
- Account page with editable saved addresses.
- Admin warehouse and route screens hidden unless the JWT role is `ADMIN`.

Local frontend config:

```text
VITE_API_BASE_URL=http://localhost:9090
```

All normal frontend traffic should go through API-Gateway, not directly to individual services.

## API Gateway Routes

Gateway runs locally at:

```text
http://localhost:9090
```

| Gateway Path | Routed Service | Role |
| --- | --- | --- |
| `/auth/**` | `Auth-Service` | public |
| `/oauth2/**` | `Auth-Service` | public |
| `/login/oauth2/**` | `Auth-Service` | public |
| `/products/**` | `Inventory-Service` | customer token |
| `/orders/**` | `Order-Service` | customer token |
| `/customers/**` | `Order-Service` | customer token |
| `/admin/warehouses/**` | `Warehouse-Service` | admin token |
| `/routes/**` | `Routing-Service` | admin token |
| `/tracking/**` | `Tracking-Service` | customer token |
| `/drivers/**` | `Tracking-Service` | customer token for now; gateway filters can be tightened later |
| `/notifications/**` | `Notification-Service` | customer token |
| `/emails/**` | `Notification-Service` | customer token |

Protected routes require:

```http
Authorization: Bearer <accessToken>
```

## Google OAuth Notes

OAuth runs through the gateway.

Google Console local config:

```text
Authorized JavaScript origins:
http://localhost:5173
http://localhost:9090

Authorized redirect URI:
http://localhost:9090/login/oauth2/code/google
```

Auth-Service must also use the gateway callback:

```properties
spring.security.oauth2.client.registration.google.redirect-uri=http://localhost:9090/login/oauth2/code/google
oauth2.success-redirect-url=http://localhost:5173/oauth/success
```

If OAuth fails with `invalid_id_token` and an `iat` claim error, sync the local system clock and restart Auth-Service.

## Key Service APIs

### Auth-Service

```text
POST /auth/signup
POST /auth/login
POST /auth/refresh
GET  /oauth2/authorization/google
GET  /login/oauth2/code/google
```

### Inventory-Service

```text
GET    /products
GET    /products/{id}
POST   /products/admin
PUT    /products/admin/{id}
PATCH  /products/admin/{id}
DELETE /products/admin/{id}
POST   /products/checkStock
PUT    /products/reduceStock
PUT    /products/addStock
```

### Order-Service

```text
GET  /orders
POST /orders/{id}
POST /orders/createOrder
PUT  /orders/cancelOrder/{id}

GET    /customers
GET    /customers/{customerId}
POST   /customers
PATCH  /customers/{customerId}
DELETE /customers/{customerId}

GET    /customers/{customerId}/addresses
GET    /customers/{customerId}/addresses/{addressId}
POST   /customers/{customerId}/addresses
PATCH  /customers/{customerId}/addresses/{addressId}
DELETE /customers/{customerId}/addresses/{addressId}
```

`POST /orders/{id}` is still non-standard and should eventually become `GET /orders/{id}`.

### Warehouse-Service

```text
GET    /admin/warehouses
GET    /admin/warehouses/{id}
POST   /admin/warehouses
PATCH  /admin/warehouses/{id}
DELETE /admin/warehouses/{id}
GET    /admin/warehouses/nearest?lat={lat}&lon={lon}
```

### Routing-Service

```text
POST /routes
```

The main production-style flow is Kafka-driven from `WAREHOUSE_ASSIGNED`.

### Tracking-Service

```text
GET    /tracking/orders/{orderNumber}

GET    /drivers
GET    /drivers/{driverId}
GET    /drivers/status/{driverStatus}
POST   /drivers
PUT    /drivers/{driverId}
PATCH  /drivers/{driverId}
DELETE /drivers/{driverId}
```

### Notification-Service

```text
GET    /notifications
GET    /notifications/{notificationId}
GET    /notifications/customer/{customerId}
GET    /notifications/customer/{customerId}/unread
POST   /notifications
PATCH  /notifications/{notificationId}/read
DELETE /notifications/{notificationId}

POST   /emails
```

## Tracking and Free-Tier Load

The simulated delivery scheduler now runs every 60 seconds:

```text
@Scheduled(fixedRate = 60000)
```

Why 60 seconds:

- 5 seconds is too write-heavy for free-tier Neon/Redis/Kafka.
- 30 seconds is better but still twice the load of 60 seconds.
- 60 seconds cuts scheduler work by 12x compared to the original 5-second tick while still giving a reasonable demo cadence.

The distance movement calculation uses the same 60-second tick so ETA/distance remain logically consistent.

## Suggested Local Startup Order

1. Start PostgreSQL/PostGIS.
2. Start Kafka.
3. Start Redis or Memurai if testing Redis-backed latest tracking state.
4. Start `Discovery-Service`.
5. Start `Config-Server`.
6. Start domain services:
   - `Auth-Service`
   - `Inventory-Service`
   - `Order-Service`
   - `Warehouse-Service`
   - `Routing-Service`
   - `Tracking-Service`
   - `Notification-Service`
7. Start `API-Gateway`.
8. Start `Frontend-Service`.

Frontend:

```bash
cd Frontend-Service
npm install
npm run dev
```

Backend services:

```bash
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

## Deployment Notes

Target free-tier infrastructure:

- Render for services/frontend.
- Neon for PostgreSQL databases.
- Aiven for Kafka with limited topics/partitions.
- Redis test database or compatible Redis provider for live tracking state.

Important deployment constraints:

- Move all secrets/API keys out of committed `application.properties`.
- Prefer config-server/environment variables for deployment values.
- Keep Kafka topic count low.
- Avoid high-frequency schedulers and polling.
- WebSocket live tracking should publish only when simulation state changes.
- Frontend should continue using the gateway as its single backend base URL.

## Current Limitations / Next Steps

- WebSocket live tracking is not implemented yet. Frontend tracking currently loads the latest tracking state by REST.
- Deployment manifests/environment variables still need final cleanup.
- Kafka retry/DLT handling should be strengthened.
- Event classes are duplicated between services; a shared events module or schema registry can be added later.
- Some internal/admin route protection can be tightened further.
- Add Docker Compose or deployment docs for reproducible infrastructure startup.
- Add broader integration tests for the full lifecycle.

## Interview Pitch

SwiftShip is a distributed order fulfillment and delivery tracking platform. It uses Kafka for asynchronous order lifecycle orchestration, PostGIS for nearest warehouse selection, OpenRouteService for route alternatives, Gemini/Spring AI for route choice, Redis for latest tracking state, and a React frontend behind a JWT-protected API Gateway. The current system supports product browsing, checkout, stock reservation, warehouse assignment, routing, tracking-session creation, notifications, and frontend role-aware customer/admin navigation.
