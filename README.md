# SwiftShip - Real Time Order Fulfillment and Delivery Tracking

SwiftShip is a Spring Boot microservices project that simulates a real-time order fulfillment and delivery tracking backend inspired by Swiggy, Blinkit, Blinkit-style quick commerce, and ecommerce logistics systems.

The project demonstrates distributed backend patterns including service discovery, centralized configuration, API gateway routing, JWT authentication, OpenFeign communication, Kafka event-driven workflows, PostgreSQL/PostGIS spatial computation, OpenRouteService route calculation, and Gemini/Spring AI based route selection.

This is a learning and portfolio project built service-by-service so each distributed pattern can be understood before the next layer is added.

## Current Product Flow

```text
Customer places an order
  -> Order-Service validates customer/address and checks stock/prices with Inventory-Service
  -> Order-Service stores the order as PLACED and publishes order-placed
  -> Inventory-Service consumes order-placed, reduces stock, and publishes order-confirmed
  -> Order-Service consumes order-confirmed and marks the order CONFIRMED
  -> Warehouse-Service consumes order-confirmed and selects the nearest active warehouse using PostGIS
  -> Warehouse-Service publishes warehouse-assigned
  -> Routing-Service consumes warehouse-assigned
  -> Routing-Service calls OpenRouteService for route alternatives
  -> Routing-Service uses Gemini/Spring AI to choose the optimal route
  -> Routing-Service stores the selected route and publishes route-calculated
  -> Tracking-Service and Notification-Service are planned next
```

## Services

| Service | Port | Current Role |
| --- | ---: | --- |
| `Discovery-Service` | `8761` | Eureka service registry |
| `Config-Server` | `9080` | Centralized Spring Cloud Config server backed by Git |
| `API-Gateway` | `9090` | Gateway routes, JWT validation, protected downstream access |
| `Auth-Service` | configured service port | Signup, login, JWT access/refresh token generation |
| `Order-Service` | configured service port | Customers, addresses, order creation, order status updates, `order-placed` producer |
| `Inventory-Service` | configured service port | Product CRUD, stock/price validation, stock reduction, `order-confirmed` producer |
| `Warehouse-Service` | configured service port | Warehouse CRUD, PostGIS nearest warehouse selection, `warehouse-assigned` producer |
| `Routing-Service` | configured service port | OpenRouteService alternatives, Gemini route choice, selected route persistence, `route-calculated` producer |

Tracking-Service and Notification-Service are not built yet.

## Architecture Snapshot

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
  |-- /routes/**            -> Routing-Service test/manual route APIs
  |
  v
Eureka Discovery-Service

Config-Server provides service configuration.
Kafka carries order lifecycle events.
PostGIS powers warehouse proximity lookup.
OpenRouteService calculates route alternatives.
Gemini/Spring AI selects the preferred route candidate.
```

## Implemented Features

- Spring Boot 4 based microservices.
- Spring Cloud Gateway API entry point.
- Eureka service discovery.
- Spring Cloud Config Server.
- Auth-Service with signup, login, password hashing, JWT access token and refresh token support.
- API-Gateway JWT validation for protected routes.
- Gateway forwards authenticated identity using `X-User-Id`.
- OpenFeign for service-to-service HTTP calls.
- Resilience4j retry, rate limiter, and circuit breaker experiments in Order-Service.
- Zipkin tracing configuration.
- Order-Service owns customers, customer addresses, orders, and order items.
- Orders store a delivery address snapshot so historical orders remain correct if a saved address changes.
- Order total price is calculated server-side using product prices returned by Inventory-Service.
- Inventory-Service checks stock and returns trusted product prices from its own database.
- Inventory-Service consumes `order-placed` and reduces stock asynchronously.
- Warehouse-Service stores warehouse locations as PostGIS points and selects the nearest active warehouse.
- Routing-Service consumes warehouse assignments, calls OpenRouteService, asks Gemini to choose the best route, stores the selected route, and publishes `route-calculated`.

## Kafka Event Flow

Implemented:

```text
Order-Service      -- order-placed      --> Inventory-Service
Inventory-Service  -- order-confirmed   --> Order-Service
Inventory-Service  -- order-confirmed   --> Warehouse-Service
Warehouse-Service  -- warehouse-assigned --> Routing-Service
Routing-Service    -- route-calculated   --> next planned consumers
```

Planned:

```text
Tracking-Service   -- eta-updated       --> Notification-Service
Tracking-Service   -- order-delivered   --> Order-Service + Notification-Service
Notification-Service consumes order/warehouse/route/tracking lifecycle events
```

### Current Event Responsibilities

`order-placed` carries order/customer/address coordinates and ordered product quantities.

`order-confirmed` confirms stock reduction and forwards delivery address coordinates.

`warehouse-assigned` carries customer destination plus assigned warehouse identity and coordinates.

`route-calculated` carries selected route id, distance, time, reasoning, warehouse/customer context, and route coordinates for downstream tracking.

Event classes are currently duplicated per service. A shared events module or Schema Registry can be added later after event payloads stabilize.

## Order-Service

Base path:

```text
/orders
```

| Method | Route | Description |
| --- | --- | --- |
| `GET` | `/orders/testOrders` | Test endpoint |
| `GET` | `/orders` | Get all orders |
| `POST` | `/orders/{ID}` | Get order by id, currently non-standard and should become `GET` later |
| `POST` | `/orders/createOrder` | Create order and publish `order-placed` |
| `PUT` | `/orders/cancelOrder/{id}` | Cancel order |

Order creation request no longer trusts client-sent price:

```json
{
  "customerId": 1,
  "customerAddressId": 1,
  "items": [
    { "productId": 1, "quantity": 2 }
  ]
}
```

Order-Service asks Inventory-Service for stock and product prices, calculates total price on the server, saves the order, and publishes `order-placed`.

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

Geocoding is still a planned improvement. Currently addresses store coordinates directly.

## Inventory-Service

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
| `POST` | `/products/checkStock` | Checks stock and returns product prices for server-side order pricing |
| `PUT` | `/products/reduceStock` | Manual/internal stock reduction |
| `PUT` | `/products/addStock` | Manual/internal stock restoration |

Kafka is now the main stock reduction path after order placement.

## Warehouse-Service

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

Warehouse locations are stored using PostgreSQL/PostGIS. The nearest warehouse query uses active warehouses only and is also reused by the Kafka `order-confirmed` consumer.

## Routing-Service

Routing-Service currently has both manual route APIs and Kafka-driven routing.

Manual route endpoint:

```text
POST /routes
```

The main workflow is event-driven:

```text
warehouse-assigned
  -> build OpenRouteService request using warehouse/customer coordinates
  -> request alternative routes with driving-hgv profile
  -> convert ORS route summaries into route candidates
  -> ask Gemini/Spring AI to select the optimal route
  -> save SelectedRoute in routeDB
  -> publish route-calculated
```

OpenRouteService coordinates are sent as:

```text
[lng, lat]
```

Selected route persistence stores:

```text
orderId
customerId
warehouseId
customerLng/customerLat
warehouseLng/warehouseLat
totalDistance
timeToReach
reasoning
```

Known remaining Routing-Service work:

- Strengthen Kafka listener error handling with retries and DLT.
- Add/update tests for route calculation and AI selection.
- Validate Gemini selected route id against candidate routes.
- Clean up manual test routes if the service becomes fully event-driven.

## API Gateway Routes

Gateway runs on:

```text
http://localhost:9090
```

| Gateway Path | Routed Service | Auth Filter |
| --- | --- | --- |
| `/auth/**` | `Auth-Service` | No |
| `/orders/**` | `Order-Service` | Yes |
| `/customers/**` | `Order-Service` | Yes |
| `/products/**` | `Inventory-Service` | Yes |
| `/admin/warehouses/**` | `Warehouse-Service` | Currently routed |
| `/routes/**` | `Routing-Service` | Currently routed/test use |

Protected routes require:

```http
Authorization: Bearer <accessToken>
```

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

1. Start PostgreSQL and Kafka.
2. Start `Discovery-Service`.
3. Start `Config-Server`.
4. Verify Config-Server endpoints, for example:

```text
http://localhost:9080/Order-Service/default
http://localhost:9080/Inventory-Service/default
http://localhost:9080/Warehouse-Service/default
http://localhost:9080/Routing-Service/default
```

5. Start domain services:
   - `Auth-Service`
   - `Inventory-Service`
   - `Order-Service`
   - `Warehouse-Service`
   - `Routing-Service`
6. Start `API-Gateway`.

Then call public APIs through:

```text
http://localhost:9090
```

## Local Dependencies

- PostgreSQL for persistent services.
- PostgreSQL/PostGIS for Warehouse-Service spatial queries.
- Kafka for order, warehouse, route, and future tracking events.
- Eureka Discovery-Service.
- Spring Cloud Config Server.
- Optional Zipkin server for tracing.
- OpenRouteService API key for Routing-Service.
- Gemini API key for Routing-Service AI route selection.
- Later: Redis for Tracking-Service live state.

## Current Limitations / Next Steps

- Create Tracking-Service with Kafka, Redis, simulated GPS movement, ETA calculation, and WebSocket live updates.
- Create Notification-Service for lifecycle notifications.
- Add geocoding for customer addresses instead of manually providing lat/lng.
- Add Kafka retry and dead-letter topic handling.
- Add shared event contracts or Schema Registry after payloads stabilize.
- Add Docker Compose for local infrastructure and demo startup.
- Move secrets and API keys to environment variables or secure config.
- Add more reliable integration tests that do not depend on live external APIs unless explicitly enabled.

## Interview Pitch

SwiftShip is a distributed order fulfillment and delivery tracking backend. It uses Kafka for order lifecycle orchestration, PostGIS for nearest warehouse selection, OpenRouteService for route alternatives, and Gemini/Spring AI for route choice. The next milestone is Tracking-Service, where Redis and WebSocket will power live delivery progress and ETA updates.
