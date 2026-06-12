# SwiftShip - Real-time Order Fulfillment & Delivery Tracking Platform 🛒

This repository is a learning project for understanding microservices architecture with Spring Boot and Spring Cloud. It is intentionally built as a set of small services instead of one monolith so that common distributed-system patterns can be practiced in isolation: API gateway routing, service discovery, config server usage, service-to-service communication, JWT authentication, resilience patterns, and basic observability.

The project is not intended to be production-ready yet. Some implementation choices are deliberately simple while the focus is on learning how the pieces fit together.

## Architecture Overview 🧭

```text
Client
  |
  v
API-Gateway :9090
  |
  |-- /auth/**     -> Auth-Service
  |-- /orders/**   -> Order-Service
  |-- /products/** -> Inventory-Service
  |
  v
Discovery-Service :8761

Config-Server :9080 provides centralized configuration for services that import it.
```

## Services 🧩

| Service | Purpose | Current Role |
| --- | --- | --- |
| `API-Gateway` 🚪 | Single entry point for clients | Routes requests to services, applies route-specific JWT authentication filter for protected routes, forwards `X-User-Id` after token validation |
| `Auth-Service` 🔐 | Authentication and user management | Signup, login, password hashing, JWT access/refresh token generation, Spring Security configuration |
| `Discovery-Service` 🧭 | Eureka server | Service registry for microservices |
| `Config-Server` ⚙️ | Spring Cloud Config server | Centralized config loaded from a remote Git repository |
| `Inventory-Service` 🗃️ | Product/inventory domain | Product lookup, stock add/reduce, service discovery demo, Feign call demo |
| `Order-Service` 📦 | Order domain | Order CRUD-like operations, order cancellation, inventory communication, resilience config |

## Implemented Features ✅

- 🧱 Spring Boot based microservices split by responsibility.
- 🚪 Spring Cloud Gateway as the API entry point.
- 🧭 Eureka service discovery.
- ⚙️ Spring Cloud Config Server.
- 🔑 JWT-based login flow.
- 🛡️ Gateway route-specific authentication for `/orders/**` and `/products/**`.
- 🌐 Public auth routes under `/auth/**`.
- ✅ Gateway validates JWT locally using the configured signing secret.
- 👤 Gateway forwards authenticated user identity through `X-User-Id`.
- 🧑‍💻 Auth-Service stores users with unique username/email.
- 🔒 Password hashing with BCrypt.
- 🔗 Service-to-service communication using OpenFeign.
- 🧯 Resilience4j configuration in Order-Service for retry, rate limiter, and circuit breaker experiments.
- 📈 Zipkin tracing configuration in Order-Service.

## API Gateway Routes 🚪

Gateway runs on:

```text
http://localhost:9090
```

| Gateway Path | Routed Service | Auth Filter |
| --- | --- | --- |
| `/auth/**` | `Auth-Service` | No |
| `/orders/**` | `Order-Service` | Yes |
| `/products/**` | `Inventory-Service` | Yes |

Protected routes require:

```http
Authorization: Bearer <accessToken>
```

After successful JWT validation, the gateway adds:

```http
X-User-Id: <userId>
```

## Auth-Service Routes 🔐

Base path:

```text
/auth
```

| Method | Route | Description | Auth Required |
| --- | --- | --- | --- |
| `POST` | `/auth/signup` | Create a new user | No |
| `POST` | `/auth/login` | Authenticate user and return tokens | No |

Example signup payload:

```json
{
  "username": "testuser",
  "email": "testuser@example.com",
  "password": "password123"
}
```

Example login payload:

```json
{
  "username": "testuser",
  "password": "password123"
}
```

Example login response shape:

```json
{
  "username": "testuser",
  "accessToken": "eyJhbGciOi...",
  "refreshToken": "eyJhbGciOi..."
}
```

## Order-Service Routes 📦

Base path:

```text
/orders
```

When called through the gateway, these routes are protected by JWT authentication.

| Method | Route | Description |
| --- | --- | --- |
| `GET` | `/orders/testOrders` | Test endpoint, also reads config value |
| `GET` | `/orders` | Get all orders |
| `POST` | `/orders/{ID}` | Get order by ID |
| `POST` | `/orders/createOrder` | Create an order |
| `PUT` | `/orders/cancelOrder/{id}` | Cancel an order |

## Inventory-Service Routes 🗃️

Base path:

```text
/products
```

When called through the gateway, these routes are protected by JWT authentication.

| Method | Route | Description |
| --- | --- | --- |
| `GET` | `/products/fetchProducts` | Demo Feign call to Order-Service |
| `GET` | `/products/discovered-services` | Lists services discovered through Eureka |
| `GET` | `/products` | Get all products |
| `POST` | `/products/{ID}` | Get product by ID |
| `PUT` | `/products/reduceStock` | Reduce stock for products |
| `PUT` | `/products/addStock` | Add stock for products |

## Authentication Flow 🔑

```text
1. Client signs up using POST /auth/signup.
2. Client logs in using POST /auth/login.
3. Auth-Service verifies username/password.
4. Auth-Service returns accessToken and refreshToken.
5. Client sends accessToken on protected requests:
   Authorization: Bearer <accessToken>
6. API-Gateway validates the JWT.
7. API-Gateway forwards the request to the target service.
8. API-Gateway adds X-User-Id so downstream services can identify the user.
```

The gateway does not call Auth-Service for every request. JWT validation is done locally in the gateway using the same signing secret. This keeps protected requests independent of Auth-Service availability after login.

## Suggested Startup Order 🚀

1. 🧭 Start `Discovery-Service`.
2. ⚙️ Start `Config-Server`.
3. 🧩 Start domain services:
   - `Auth-Service`
   - `Inventory-Service`
   - `Order-Service`
4. 🚪 Start `API-Gateway`.

Then call APIs through:

```text
http://localhost:9090
```

## Local Configuration Notes ⚙️

This project currently uses local configuration files for database credentials, JWT secrets, and config-server settings. For a real deployment, these values should be moved to environment variables, a secret manager, or a secure external config source.

Important local dependencies include:

- PostgreSQL databases for services that persist data.
- Eureka running through `Discovery-Service`.
- Config server repository access for `Config-Server`.
- Optional Zipkin server if tracing is being tested.

## Learning Goals Covered 🎯

This repository is useful for practicing:

- 🚪 How an API Gateway sits in front of services.
- 🛡️ How route-specific gateway filters work.
- 🔑 How JWT authentication differs from authorization.
- 🔐 Why login/signup belong in Auth-Service, while token validation can happen in Gateway.
- 🧭 How Eureka enables service discovery.
- 🔗 How Feign clients simplify service-to-service calls.
- ⚙️ How central config changes the way services are configured.
- 🧯 How resilience patterns are configured with Resilience4j.
- 👤 How auth identity can be propagated using headers like `X-User-Id`.

## Current Limitations / Next Steps 🛠️

- 🧯 Add proper exception handling in Auth-Service for duplicate users and bad credentials.
- 🚫 Return clean `401` responses for expired or malformed JWTs in API-Gateway.
- 🔒 Move JWT secrets and database credentials out of committed config files.
- 👥 Add roles/authorities to `UserEntity` and JWT claims.
- 🛡️ Protect downstream services from direct external access so clients cannot bypass the gateway.
- 🧪 Add integration tests for auth flow through the gateway.
- 🐳 Add Docker Compose for PostgreSQL, Eureka, Config Server, Zipkin, and all services.
- 🧹 Standardize HTTP methods, for example use `GET /orders/{id}` instead of `POST /orders/{ID}` for lookups.

