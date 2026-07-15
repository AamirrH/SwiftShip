# SwiftShip - Distributed Order Fulfillment and Live Delivery Tracking

<img width="3096" height="3072" alt="SwiftShip_Icon" src="https://github.com/user-attachments/assets/1478f836-06ba-4ed1-ac4c-cc42e8c7168c" />

SwiftShip is a full-stack microservices-based order fulfillment and delivery tracking platform built as a portfolio project. It simulates the backend flow of a quick-commerce/logistics system: customers place orders, inventory is reserved, the nearest warehouse is selected, a route is calculated, a driver/tracking session is created, and the customer receives live delivery updates and notifications.

The project was built over roughly 2-3 months through many small, organic commits and real deployment/debugging iterations. The goal was to go beyond CRUD APIs and build a system that demonstrates service boundaries, asynchronous event choreography, fault tolerance, production configuration, and frontend/backend integration.

## Live Links

| Resource | Link |
| --- | --- |
| Frontend demo | https://swift-ship-nu.vercel.app/ |
| Repository | https://github.com/AamirrH/SwiftShip |
| API Gateway | https://swiftship-api-gateway.onrender.com |
| Eureka Dashboard | https://swiftship-u7gw.onrender.com |

> Note: services are deployed on free-tier infrastructure, so cold starts or temporary latency can happen.

## Deployed Infrastructure

SwiftShip is deployed across several free-tier/cloud services:

| Layer | Platform | Purpose |
| --- | --- | --- |
| Frontend | Vercel | React/Vite customer and admin console |
| Backend services | Render | Independently deployed Spring Boot services |
| Databases | Neon PostgreSQL | Separate service databases for auth, orders, inventory, warehouse, routing, tracking, notifications |
| Kafka | Aiven Kafka | Event-driven communication between services |
| Redis | Redis test DB | Latest tracking state / live delivery cache |
| Service discovery | Eureka on Render | Runtime service registration and gateway discovery |
| Email provider | Resend | Email notification layer |

## Microservices

| Service | Role |
| --- | --- |
| `API-Gateway` | Single backend entry point for the frontend, JWT validation, CORS, route forwarding, role-aware access |
| `Auth-Service` | Manual signup/login, Google OAuth2, JWT access token generation |
| `Inventory-Service` | Product catalog, stock checks, stock reservation/reduction/restoration |
| `Order-Service` | Customers, saved addresses, cart checkout, order creation, grouped order views, cancellation |
| `Warehouse-Service` | Warehouse data, PostGIS nearest active warehouse selection, warehouse assignment events |
| `Routing-Service` | Route calculation using OpenRouteService, Gemini/Spring AI route choice, local route fallback |
| `Tracking-Service` | Driver assignment, tracking sessions, Redis latest state, simulated delivery movement, WebSocket-ready tracking flow |
| `Notification-Service` | In-app notifications, Kafka lifecycle listeners, Resend email integration |
| `Discovery-Service` | Eureka service registry for deployed backend services |
| `Frontend-Service` | React/Vite customer/admin dashboard |

## End-to-End Flow

```text
Customer logs in through JWT/OAuth2
  -> browses products from Inventory-Service
  -> adds items to cart
  -> selects/saves delivery address
  -> places order through API-Gateway
  -> Order-Service persists order and emits ORDER_PLACED
  -> Inventory-Service consumes event and reserves stock
  -> Inventory-Service emits ORDER_CONFIRMED
  -> Order-Service updates order status
  -> Warehouse-Service selects nearest active warehouse using PostGIS
  -> Warehouse-Service emits WAREHOUSE_ASSIGNED
  -> Routing-Service calculates/falls back to route alternatives
  -> Routing-Service emits ROUTE_CALCULATED
  -> Tracking-Service creates tracking session and assigns simulated driver
  -> Tracking-Service updates delivery state and ETA
  -> Notification-Service stores customer notifications and sends email where enabled
  -> Frontend displays orders, tracking, notifications, and account state
```

## Event-Driven Architecture

SwiftShip uses Kafka for the fulfillment pipeline. Because Aiven's free tier has topic limits, events are grouped into a small set of topics with event-type fields instead of creating one topic per event.

```text
order-events
fulfillment-events
tracking-events
```

Representative events:

```text
ORDER_PLACED
ORDER_CONFIRMED
WAREHOUSE_ASSIGNED
ROUTE_CALCULATED
ETA_UPDATED
ORDER_DELIVERED
```

This keeps the system closer to real event-driven architecture while staying deployable on free-tier infrastructure.

## Technical Features

- Spring Boot microservices with independent service ownership.
- Spring Cloud Gateway as the single public backend entry point.
- Eureka service discovery across Render-deployed services.
- JWT authentication and role-aware customer/admin routing.
- Google OAuth2 login integrated through the gateway.
- Kafka-based asynchronous service choreography.
- Aiven Kafka configuration using SASL_SSL.
- Separate Neon PostgreSQL databases per service.
- PostGIS geography queries for nearest warehouse selection.
- OpenRouteService integration for route alternatives.
- Gemini/Spring AI assisted route selection.
- Local routing fallback so external API failures do not break the order pipeline.
- Redis-backed latest tracking state.
- Simulated driver assignment and delivery movement.
- WebSocket-ready live tracking architecture.
- Resend-backed email notification layer.
- In-app notification center.
- Resilience4j retry, circuit breaker, and rate limiter patterns.
- Dockerfiles for Render deployment.
- Frontend health page for service visibility.
- Responsive React frontend for desktop, laptop, and mobile.
- Customer-facing language cleanup to avoid exposing internal route/service details.

## Frontend Features

The frontend is a React/Vite app deployed on Vercel.

Implemented screens and flows:

- Customer signup/login and Google OAuth login.
- Product catalog with search and daily rotating featured products.
- Product cards, product detail view, and cart management.
- Checkout with saved address selection and address creation.
- Order placement and cancellation confirmations.
- Grouped order history so each order appears once.
- Tracking page with animated delivery route UI.
- Notification center with read/unread state.
- Account page with profile and address management.
- Admin-only warehouse and route sections hidden from customer accounts.
- Standalone health/status page for deployed service visibility.

Frontend config:

```text
VITE_API_BASE_URL=https://swiftship-api-gateway.onrender.com
```

All frontend traffic is designed to go through the API Gateway, not directly to individual services.

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

## Deployment Notes

The app is intentionally deployed on constrained free-tier platforms. That shaped several production-style decisions:

- Kafka topics are consolidated to stay within Aiven topic limits.
- Tracking simulation uses a slower scheduler to reduce Neon/Redis writes.
- Services use small DB connection pools where needed.
- External route failures degrade to local fallback route calculation.
- API keys/secrets are supplied through deployment environment variables.
- Each backend service has a lightweight keepalive endpoint/page for uptime pings.

## Local Development

Suggested startup order:

1. PostgreSQL/PostGIS
2. Kafka
3. Redis/Memurai
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

SwiftShip is a portfolio/learning project, not a production SaaS. Some bugs or edge cases may still exist.

Known areas for future improvement:

- Add dead-letter topics and stronger Kafka retry/DLT handling.
- Move duplicated event DTOs into a shared contract module or schema registry.
- Add broader integration tests for the full order lifecycle.
- Add stronger observability with centralized logs/tracing.
- Improve deployment automation and infrastructure documentation.
- Tighten internal-only route access further.
- Continue polishing WebSocket live tracking behavior.

## Interview Pitch

SwiftShip is a distributed order fulfillment and live delivery tracking platform built with Spring Boot microservices, Kafka event choreography, PostGIS warehouse selection, OpenRouteService/Gemini route calculation, Redis tracking state, JWT/OAuth2 authentication, and a React frontend behind an API Gateway. It demonstrates practical backend system design: service ownership, asynchronous workflows, cloud deployment, fault-tolerant fallbacks, role-based access, and real-world debugging across multiple services.
