# SwiftShip - Distributed Order Fulfillment and Live Delivery Tracking

<img width="3096" height="3072" alt="SwiftShip_Icon" src="https://github.com/user-attachments/assets/1478f836-06ba-4ed1-ac4c-cc42e8c7168c" />

SwiftShip is a full-stack distributed order fulfillment and live delivery tracking platform built as a portfolio project. It simulates the kind of backend workflow used in quick-commerce and logistics systems: a customer places an order, stock is reserved, the nearest warehouse is selected, a route is calculated, a tracking session starts, and the customer receives delivery updates.

The project was built over roughly 2-3 months through many small, organic commits and real deployment/debugging iterations. The goal was not only to build APIs, but to understand how independent services communicate, fail, recover, and still complete one user-facing business flow.

## Live Links

| Resource | Link |
| --- | --- |
| Frontend demo | https://swift-ship-nu.vercel.app/ |
| Repository | https://github.com/AamirrH/SwiftShip |
| API Gateway | https://swiftship-api-gateway.onrender.com |
| Eureka Dashboard | https://swiftship-u7gw.onrender.com |

> The project is deployed on free-tier infrastructure, so cold starts and occasional latency can happen.

## What SwiftShip Does

From the customer side, SwiftShip supports a complete shopping and fulfillment experience:

- Sign up and log in using manual auth or Google OAuth2.
- Browse a product catalog.
- Search products.
- View product details.
- Add products to cart.
- Save and manage delivery addresses.
- Place an order.
- See grouped order history.
- Cancel eligible orders.
- Track delivery progress.
- Receive in-app notifications for order lifecycle updates.
- View account/profile state.

From the backend side, SwiftShip coordinates a multi-service order lifecycle:

```text
Customer checkout
  -> API-Gateway validates JWT and forwards request
  -> Order-Service creates order and snapshots delivery address
  -> Order-Service publishes ORDER_PLACED
  -> Inventory-Service reserves/reduces product stock
  -> Inventory-Service publishes ORDER_CONFIRMED
  -> Order-Service updates order status
  -> Warehouse-Service selects nearest active warehouse using PostGIS
  -> Warehouse-Service publishes WAREHOUSE_ASSIGNED
  -> Routing-Service calculates/selects route and falls back when external routing fails
  -> Routing-Service publishes ROUTE_CALCULATED
  -> Tracking-Service creates tracking session and assigns simulated driver
  -> Tracking-Service simulates delivery movement and ETA changes
  -> Notification-Service creates customer notifications and email updates
  -> Frontend shows order, tracking, notification, and account state
```

## Architecture Overview

SwiftShip is split into independently owned services. The frontend talks only to the API Gateway; the gateway routes requests to backend services and enforces authentication/authorization boundaries.

```text
React/Vite Frontend on Vercel
        |
        v
Spring Cloud API-Gateway on Render
        |
        |-- Auth-Service
        |-- Inventory-Service
        |-- Order-Service
        |-- Warehouse-Service
        |-- Routing-Service
        |-- Tracking-Service
        |-- Notification-Service
        |
        v
Eureka Discovery-Service

Kafka connects the lifecycle asynchronously.
Neon PostgreSQL stores service data.
PostGIS handles geospatial warehouse lookup.
Redis stores latest tracking state.
Aiven Kafka carries events.
Render hosts backend services.
Vercel hosts the frontend.
```

## Deployed Infrastructure

| Layer | Platform | Purpose |
| --- | --- | --- |
| Frontend | Vercel | React/Vite customer and admin console |
| Backend services | Render | Independently deployed Spring Boot services |
| Databases | Neon PostgreSQL | Separate databases for service-owned data |
| Messaging | Aiven Kafka | Event-driven communication between services |
| Cache/live state | Redis test DB | Latest tracking/session state |
| Service discovery | Eureka on Render | Service registry for gateway/service lookup |
| Email | Resend | Email notification layer |

## Services and Responsibilities

### API-Gateway

- Single public backend entry point.
- Routes frontend requests to internal services.
- Validates JWTs.
- Handles CORS.
- Protects customer/admin route groups.
- Keeps the frontend from directly depending on service URLs.

### Auth-Service

- Manual signup/login.
- Password hashing.
- Google OAuth2 login.
- JWT access token generation.
- Auth success redirect flow for the deployed frontend.

### Order-Service

- Owns customers, saved addresses, orders, and order items.
- Creates orders from checkout payloads.
- Stores delivery address snapshots on the order.
- Groups order items so one order appears once in the frontend.
- Supports order cancellation.
- Publishes order lifecycle events to Kafka.
- Handles stock restoration during cancellation where applicable.

### Inventory-Service

- Owns product catalog and stock state.
- Serves product list/detail/search-style frontend use cases.
- Checks stock availability.
- Reserves/reduces stock asynchronously after order placement.
- Restores stock when orders are cancelled.
- Publishes order confirmation events after successful reservation.

### Warehouse-Service

- Owns warehouse data and active/inactive warehouse state.
- Stores warehouse coordinates using PostgreSQL/PostGIS geography points.
- Selects nearest active fulfillment hub using spatial distance queries.
- Avoids manual Java-side coordinate loops by pushing geospatial work to the database.
- Publishes warehouse assignment events.

Nearest warehouse logic conceptually works like this:

```text
customer latitude/longitude
  -> build PostGIS point
  -> compare against active warehouse geography points
  -> use spatial distance/indexing
  -> return nearest fulfillment warehouse
```

### Routing-Service

- Consumes warehouse assignment events.
- Builds route requests from warehouse/customer coordinates.
- Integrates with OpenRouteService for route alternatives.
- Uses Gemini/Spring AI to select a route from available alternatives.
- Stores selected route records.
- Publishes route calculated events.
- Includes fallback route calculation so external API failure does not stop the fulfillment pipeline.

### Tracking-Service

- Consumes route calculated events.
- Creates tracking sessions.
- Assigns simulated drivers.
- Moves active deliveries over time.
- Updates remaining distance, ETA, and current coordinates.
- Stores latest state in Redis where available.
- Exposes tracking lookup for customer order tracking.
- Provides the backend foundation for live tracking/WebSocket updates.

### Notification-Service

- Consumes lifecycle events from Kafka.
- Stores in-app notifications.
- Supports read/unread notification state.
- Sends email-style notifications through Resend where configured.
- Gives the frontend a notification center tied to the customer/order lifecycle.

### Discovery-Service

- Eureka registry for deployed services.
- Helps visualize which backend services are currently registered and alive.

### Frontend-Service

- React/Vite frontend deployed on Vercel.
- Customer and admin console.
- Talks to the backend through API-Gateway only.

## Kafka Event Flow

SwiftShip uses Kafka for asynchronous event choreography. To stay within Aiven free-tier topic limits, related events are grouped into a small number of topics and differentiated by `eventType`.

Topics:

```text
order-events
fulfillment-events
tracking-events
```

Lifecycle events:

```text
ORDER_PLACED
ORDER_CONFIRMED
WAREHOUSE_ASSIGNED
ROUTE_CALCULATED
ETA_UPDATED
ORDER_DELIVERED
```

Flow:

```text
Order-Service
  publishes ORDER_PLACED
        |
        v
Inventory-Service
  reserves stock
  publishes ORDER_CONFIRMED
        |
        v
Warehouse-Service
  selects nearest active warehouse using PostGIS
  publishes WAREHOUSE_ASSIGNED
        |
        v
Routing-Service
  calculates/selects delivery route
  publishes ROUTE_CALCULATED
        |
        v
Tracking-Service
  creates session, assigns driver, simulates movement
  publishes tracking updates
        |
        v
Notification-Service
  stores/sends customer lifecycle updates
```

## Frontend Features

The frontend is built with React and Vite and deployed on Vercel.

Implemented screens/features:

- Home page with featured products.
- Catalog page with product search.
- Product detail page.
- Cart and checkout.
- Saved address selection and address creation.
- Order placement confirmation.
- Order cancellation confirmation.
- Grouped customer order history.
- Tracking screen with animated delivery route UI.
- Notification center with read/unread state.
- Account/profile screen.
- Editable customer address information.
- Admin/customer role-based navigation.
- Warehouse admin pages.
- Route/admin pages.
- Standalone health page for service connectivity.
- Responsive layout for desktop, laptop, tablet, and mobile.

Frontend production base URL:

```text
VITE_API_BASE_URL=https://swiftship-api-gateway.onrender.com
```

## Security and Access Control

- JWT-based backend authentication.
- Google OAuth2 login support.
- API Gateway validates tokens before forwarding protected requests.
- Frontend stores and sends access tokens for authenticated calls.
- Customer/admin role awareness in frontend navigation.
- Admin routes hidden from customer users in the UI.
- Gateway route groups separate public, customer, admin, and internal-style endpoints.

## Resilience and Failure Handling

SwiftShip includes several production-style safeguards:

- Resilience4j retry patterns.
- Circuit breakers around external/service-sensitive paths.
- Rate limiters to reduce abuse and free-tier overload.
- Routing fallback when OpenRouteService fails or returns invalid data.
- Null-safe event handling in routing paths.
- Redis used for fast latest tracking state where available.
- Slower tracking scheduler to reduce database/cache churn on free-tier infrastructure.
- Kafka topic consolidation for Aiven free-tier limits.
- Service keepalive pages/endpoints for Render free-tier deployments.

## Gateway Route Map

| Gateway Path | Routed Service | Access |
| --- | --- | --- |
| `/auth/**` | Auth-Service | Public |
| `/oauth2/**` | Auth-Service | Public |
| `/login/oauth2/**` | Auth-Service | Public |
| `/products/**` | Inventory-Service | Authenticated |
| `/orders/**` | Order-Service | Authenticated customer |
| `/customers/**` | Order-Service | Authenticated customer |
| `/admin/warehouses/**` | Warehouse-Service | Admin |
| `/routes/**` | Routing-Service | Admin/internal style access |
| `/tracking/**` | Tracking-Service | Authenticated customer |
| `/drivers/**` | Tracking-Service | Gateway-protected route |
| `/notifications/**` | Notification-Service | Authenticated customer |
| `/emails/**` | Notification-Service | Authenticated/customer flow |

## Technical Stack

- Java 21
- Spring Boot
- Spring Cloud Gateway
- Spring Security
- OAuth2 Client
- Eureka Discovery
- Spring Cloud Config support
- Spring Kafka
- Apache Kafka / Aiven Kafka
- PostgreSQL / Neon
- PostGIS
- Redis
- OpenFeign
- OpenRouteService
- Spring AI / Gemini
- Resilience4j
- Docker
- React
- Vite
- Vercel
- Render
- Resend

## Local Development

Suggested startup order:

1. PostgreSQL/PostGIS
2. Kafka
3. Redis or Memurai
4. Discovery-Service
5. API-Gateway
6. Auth-Service
7. Inventory-Service
8. Order-Service
9. Warehouse-Service
10. Routing-Service
11. Tracking-Service
12. Notification-Service
13. Frontend-Service

Frontend:

```bash
cd Frontend-Service
npm install
npm run dev
```

Backend service example:

```powershell
cd Order-Service
.\mvnw.cmd spring-boot:run
```

## Current Limitations

SwiftShip is a portfolio/learning project, not a production SaaS. Some bugs and edge cases may still exist.

Future improvements:

- Add dedicated dead-letter topics and stronger Kafka retry/DLT handling.
- Move duplicated event DTOs into a shared contract module or schema registry.
- Add more integration tests across the full lifecycle.
- Add centralized logging/tracing dashboards.
- Improve deployment automation.
- Tighten internal/admin route access further.
- Continue polishing WebSocket-driven live tracking behavior.
- Add richer admin analytics and operational dashboards.

## Interview Pitch

SwiftShip is a deployed distributed order fulfillment and live delivery tracking platform. It uses Spring Boot microservices, Spring Cloud Gateway, JWT/OAuth2 authentication, Kafka event choreography, PostGIS nearest-warehouse selection, OpenRouteService/Gemini route calculation, Redis tracking state, Resilience4j fault tolerance, Neon PostgreSQL databases, Render backend deployments, and a Vercel React frontend. The project demonstrates service ownership, asynchronous workflows, cloud deployment, fault-tolerant fallbacks, role-based access, and end-to-end debugging across a real multi-service system.
