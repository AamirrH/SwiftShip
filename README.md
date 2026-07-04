# SwiftShip - Real Time Order Fulfillment and Delivery Tracking

SwiftShip is a Spring Boot microservices project that simulates a real-time order fulfillment and delivery tracking backend inspired by Swiggy, Blinkit, and ecommerce logistics systems.

The goal is to demonstrate practical distributed-system skills: service discovery, API gateway routing, JWT authentication, config server usage, OpenFeign calls, Kafka event-driven workflows, PostGIS warehouse selection, external route calculation, and eventually live tracking with Redis/WebSocket.

This is a learning and portfolio project. It is intentionally built service-by-service so each distributed pattern can be understood before the next layer is added.

## Product Flow

```text
Customer places an order
  -> Order-Service stores the order and publishes order-placed
  -> Inventory-Service consumes order-placed and reserves/reduces stock
  -> Inventory-Service will publish order-confirmed or order-rejected
  -> Warehouse-Service will assign the nearest active warehouse using PostGIS
  -> Routing-Service will calculate route alternatives using OpenRouteService
  -> Routing-Service will use Gemini/Spring AI to select the best route
  -> Tracking-Service will simulate driver movement and live ETA updates
```

## Current Services

| Service | Port | Current Role |
| --- | ---: | --- |
| `Discovery-Service` | `8761` | Eureka service registry |
| `Config-Server` | `9080` | Centralized Spring Cloud Config server |
| `API-Gateway` | `9090` | Gateway routes, JWT validation, protected service access |
| `Auth-Service` | configured service port | Signup, login, JWT access/refresh token generation |
| `Order-Service` | configured service port | Orders, customers, customer addresses, selected delivery address snapshots, `order-placed` Kafka producer |
| `Inventory-Service` | configured service port | Product CRUD, stock checks, async stock reduction from `order-placed` Kafka consumer |
| `Warehouse-Service` | configured service port | Warehouse CRUD and nearest active warehouse lookup using PostgreSQL/PostGIS |
| `Routing-Service` | configured service port | OpenRouteService route alternatives between warehouse/customer coordinates |

Tracking and notification services are planned next.

## Current Architecture

```text
Client
  |
  v
API-Gateway
  |
  |-- /auth/**              -> Auth-Service
  |-- /orders/**            -> Order-Service
  |-- /customers/**         -> Order-Service
  |-- /products/**          -> Inventory-Service
  |-- /admin/warehouses/**  -> Warehouse-Service
  |-- /routes/**            -> Routing-Service
  |
  v
Eureka Discovery-Service

Config-Server provides centralized service configuration.
Kafka is used for order lifecycle events.
```

## Implemented Features

- Spring Boot 4 based microservices.
- Spring Cloud Gateway API entry point.
- Eureka service discovery.
- Spring Cloud Config Server.
- Auth-Service with signup, login, password hashing, and JWT generation.
- API-Gateway JWT validation for protected routes.
- Gateway forwards authenticated user identity using `X-User-Id`.
- OpenFeign between services where synchronous calls are still used.
- Resilience4j retry, rate limiter, and circuit breaker experiments in Order-Service.
- Zipkin tracing configuration.
- Order-Service customer and address domain:
  - `Customer`
  - `CustomerAddress`
  - order stores `customer`, selected address reference, and delivery address snapshot.
- Order placement publishes an `order-placed` Kafka event.
- Inventory-Service consumes `order-placed` and reduces stock asynchronously.
- Inventory stock checking uses a single DB fetch and groups duplicate product IDs.
- Warehouse-Service uses PostGIS geography points for nearest active warehouse selection.
- Routing-Service calls OpenRouteService and returns route alternatives with distance/duration.

## Kafka Progress

Implemented:

```text
Order-Service -- order-placed --> Inventory-Service
```

`order-placed` currently carries:

```text
orderNumber
customerId
deliveryAddress
deliveryLat
deliveryLng
orderedItems: productId + quantity
```

Next Kafka events:

```text
Inventory-Service -- order-confirmed/order-rejected --> Order-Service
Inventory-Service -- order-confirmed --> Warehouse-Service
Warehouse-Service -- warehouse-assigned --> Routing-Service
Routing-Service -- route-calculated --> Tracking-Service
Tracking-Service -- eta-updated/order-delivered --> Notification-Service and Order-Service
```

## API Gateway Routes

Gateway runs on:

```text
http://localhost:9090
```

| Gateway Path | Routed Service | Auth Filter |
| --- | --- | --- |
| `/auth/**` | `Auth-Service` | No |
| `/orders/**` | `Order-Service` | Yes |
| `/products/**` | `Inventory-Service` | Yes |
| `/admin/warehouses/**` | `Warehouse-Service` | Currently routed |

Protected routes require:

```http
Authorization: Bearer <accessToken>
```

## Order-Service Routes

Base path:

```text
/orders
```

| Method | Route | Description |
| --- | --- | --- |
| `GET` | `/orders/testOrders` | Test endpoint |
| `GET` | `/orders` | Get all orders |
| `POST` | `/orders/{ID}` | Get order by id |
| `POST` | `/orders/createOrder` | Create an order and publish `order-placed` |
| `PUT` | `/orders/cancelOrder/{id}` | Cancel an order |

Order creation now expects customer/address context:

```json
{
  "customerId": 1,
  "customerAddressId": 1,
  "items": [
    { "productId": 1, "quantity": 2 }
  ],
  "totalPrice": 1299.00
}
```

The selected address is copied into the order as a delivery snapshot so old orders remain historically correct even if the customer edits their saved address later.

## Customer Routes

Customer/address management lives inside Order-Service for this project.

Base path:

```text
/customers
```

| Method | Route | Description |
| --- | --- | --- |
| `GET` | `/customers` | Get all customers |
| `GET` | `/customers/{customerId}` | Get customer by id |
| `POST` | `/customers` | Create customer |
| `PATCH` | `/customers/{customerId}` | Update customer |
| `DELETE` | `/customers/{customerId}` | Delete customer |
| `GET` | `/customers/{customerId}/addresses` | Get saved addresses |
| `GET` | `/customers/{customerId}/addresses/{addressId}` | Get saved address |
| `POST` | `/customers/{customerId}/addresses` | Add saved address |
| `PATCH` | `/customers/{customerId}/addresses/{addressId}` | Update saved address |
| `DELETE` | `/customers/{customerId}/addresses/{addressId}` | Delete saved address |

## Inventory-Service Routes

Base path:

```text
/products
```

| Method | Route | Description |
| --- | --- | --- |
| `GET` | `/products/fetchProducts` | Demo Feign call to Order-Service |
| `GET` | `/products/discovered-services` | List Eureka services |
| `GET` | `/products` | Get all products |
| `GET` | `/products/{ID}` | Get product by id |
| `POST` | `/products/admin` | Create product |
| `PUT` | `/products/admin/{ID}` | Update product |
| `PATCH` | `/products/admin/{ID}` | Patch product |
| `DELETE` | `/products/admin/{ID}` | Delete product |
| `POST` | `/products/checkStock` | Check stock availability |
| `PUT` | `/products/reduceStock` | Manual/internal stock reduction |
| `PUT` | `/products/addStock` | Manual/internal stock restoration |

Final order stock changes should happen through Kafka, not direct client calls.

## Warehouse-Service Routes

Base path:

```text
/admin/warehouses
```

| Method | Route | Description |
| --- | --- | --- |
| `GET` | `/admin/warehouses` | Get all warehouses |
| `GET` | `/admin/warehouses/{id}` | Get warehouse by UUID |
| `POST` | `/admin/warehouses` | Create warehouse |
| `PATCH` | `/admin/warehouses/{id}` | Update warehouse |
| `DELETE` | `/admin/warehouses/{id}` | Soft delete warehouse |
| `GET` | `/admin/warehouses/nearest?lon={lng}&lat={lat}` | Find nearest active warehouse |

Warehouse locations are stored as PostGIS `GEOGRAPHY(Point, 4326)` and queried using a native nearest-neighbor query.

## Routing-Service

Routing-Service currently calls OpenRouteService with warehouse/customer coordinate pairs and supports alternative routes.

OpenRouteService coordinates must be sent as:

```text
[lng, lat]
```

The service currently extracts route candidates with:

```text
distance
duration
```

Next step: add Spring AI/Gemini route selection over those candidates using business signals such as weather, demand, priority, and vehicle profile.

## Auth Flow

```text
1. Client signs up using Auth-Service.
2. Client logs in and receives JWT access/refresh tokens.
3. Client calls protected routes with Authorization: Bearer <token>.
4. API-Gateway validates the JWT locally.
5. Gateway forwards the request to downstream services.
6. Gateway adds X-User-Id for downstream identity context.
```

## Suggested Startup Order

1. Start PostgreSQL/Kafka dependencies.
2. Start `Discovery-Service`.
3. Start `Config-Server`.
4. Start domain services:
   - `Auth-Service`
   - `Order-Service`
   - `Inventory-Service`
   - `Warehouse-Service`
   - `Routing-Service`
5. Start `API-Gateway`.

Then call public APIs through:

```text
http://localhost:9090
```

## Local Dependencies

- PostgreSQL for persistent services.
- PostgreSQL/PostGIS for Warehouse-Service.
- Kafka for order lifecycle events.
- Eureka Discovery-Service.
- Config-Server.
- Optional Zipkin server for tracing.
- OpenRouteService API key for Routing-Service.
- Later: Redis for Tracking-Service live state.

## Current Limitations / Next Steps

- Create Tracking-Service with Kafka, Redis, and WebSocket live updates.
- Create Notification-Service for lifecycle notifications.
- Add Docker Compose for local infrastructure and demo startup.
- Move secrets and API keys to environment variables or secure config.
- Standardize routes where needed, for example `GET /orders/{id}` instead of `POST /orders/{ID}`.
