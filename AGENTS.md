# SwiftShip Project Memory

This project follows the SwiftShip plan: a real-time order fulfillment and delivery tracking platform inspired by Swiggy/Blinkit-style backend systems.

## Core Product Goal

SwiftShip simulates this flow:

Customer places an order, inventory reserves stock, the nearest warehouse is selected using PostGIS, a route is calculated and optimized with Spring AI, a simulated driver delivers it, and the customer can watch delivery progress live on a map.

## Services

- `config-server` on `8888`: central configuration.
- `api-gateway` on `8080`: single entry point, JWT auth, rate limiting, route security.
- `order-service` on `8081`: create and manage orders.
- `inventory-service` on `8082`: product and stock management, stock reservation.
- `warehouse-service` on `8083`: nearest warehouse selection using PostgreSQL/PostGIS.
- `routing-service` on `8084`: route calculation, Spring AI/Gemini route optimization.
- `tracking-service` on `8085`: Redis, Kafka Streams, GPS ingestion, ETA, WebSocket live updates.
- `notification-service` on `8086`: email/SMS/in-app notifications at key events.

## Main Build Order

1. Finish core REST/business behavior for existing services.
2. Build `warehouse-service`.
3. Build `routing-service`.
4. Add Spring AI route optimization in `routing-service`.
5. Build `tracking-service` with Kafka Streams, Redis, and WebSocket.
6. Build `notification-service`.
7. Add Kafka integration across services after core APIs stabilize.
8. Add API Gateway routes, auth roles, admin/customer access rules.
9. Add Docker/deployment/readme/demo polish.

## Kafka Event Flow

- `order.created`: produced by order-service; consumed by inventory-service and notification-service.
- `order.confirmed`: produced by inventory-service; consumed by warehouse-service and notification-service.
- `warehouse.assigned`: produced by warehouse-service; consumed by routing-service and notification-service.
- `route.calculated`: produced by routing-service; consumed by tracking-service and notification-service.
- `gps.ping`: produced by driver simulator; consumed by tracking-service.
- `eta.updated`: produced by tracking-service; consumed by notification-service.
- `order.delivered`: produced by tracking-service; consumed by order-service and notification-service.

## Service Responsibilities

### Order Service

- Owns orders and order items.
- Creates orders with customer, delivery address, lat/lng, and items.
- Publishes order lifecycle events later.
- Eventually supports CQRS/read model for dashboard queries.

### Inventory Service

- Owns products, stock, and reservations.
- Customer can view products.
- Admin can create/update/delete products.
- Internal APIs reserve/release stock.
- Later consumes `order.created` and publishes `order.confirmed` or failure event.

### Warehouse Service

- Owns warehouse location data.
- Uses PostgreSQL/PostGIS for nearest warehouse selection.
- Current project decision: use the actual codebase shape over the PDF when they differ.
- Current local table style may be `warehouse` with `warehouse_name`; keep entity, SQL, and native queries consistent.
- CRUD/admin operations exist; admin security is wired later through gateway/security.
- Nearest endpoint uses active warehouses only.
- Kafka integration can be added later using the same nearest-warehouse service method.

### Routing Service

- Consumes/accepts warehouse origin and delivery destination.
- Stores route records.
- V1 can use simple distance/ETA logic.
- Later uses Spring AI with Gemini for route optimization using weather and demand signals.

### Tracking Service

- Consumes GPS pings from a simulated driver.
- Uses Kafka Streams for rolling ETA calculations.
- Stores live driver/order mappings in Redis.
- Pushes live updates over WebSocket.

### Notification Service

- Stores and sends notifications for order, warehouse, route, ETA, and delivery events.
- V1 can store notification records and expose APIs.
- Email/SMS can be added later.

## Spring AI Placement

Spring AI belongs mainly in `routing-service`, not Warehouse-Service.

Use it for route optimization with inputs like:

- warehouse origin lat/lng
- customer destination lat/lng
- weather signal
- demand signal
- route options/mock data

It should produce a suggested route and explanation, stored in the route record as `suggested_route` and `ai_reasoning`.

Optional later AI features:

- customer support assistant
- admin analytics assistant
- low-stock/product insight assistant
- notification message generation

## Important Working Rules

- Do not implement code without explicit user approval when the user is asking to learn step by step.
- Explain each step before building it when requested.
- Prefer finishing REST/business basics before Kafka integration.
- Kafka integration should be done together later once service payloads stabilize.
- For customer/admin separation, add route structure first; role enforcement can be wired later.
- Keep scope disciplined for the two-month timeline.

## Interview Pitch

"I built SwiftShip, a distributed order fulfillment and real-time delivery tracking system. It uses Kafka Streams for stateful GPS event aggregation and live ETA computation, PostGIS for spatial warehouse selection, WebSocket for pushing live location updates to the client, and Spring AI with Gemini to optimize delivery routes factoring in weather and demand signals. The read-heavy dashboard is separated from the write-heavy order flow using CQRS with a dedicated read model database."
