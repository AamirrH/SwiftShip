# SwiftShip Deployment Accounts

Track which Render account owns each deployed SwiftShip service.

| Gmail Account | Service | Platform | URL | Keepalive / Health URL | Status | Notes |
| --- | --- | --- | --- | --- | --- | --- |
| cavaliercoding@gmail.com | Discovery-Service | Render | https://swiftship-u7gw.onrender.com | https://swiftship-u7gw.onrender.com/ | Deployed | Eureka server; root page is the configured HTTP health target |
| hypezar11@gmail.com | API-Gateway | Render | https://swiftship-api-gateway.onrender.com | https://swiftship-api-gateway.onrender.com/keepalive.html | Deployed | Public backend entrypoint; aggregate health: `/platform/health` |
| aamirdev17.2@gmail.com | Auth-Service | Render | https://swiftship-authentication-service.onrender.com | https://swiftship-authentication-service.onrender.com/keepalive.html | Deployed | OAuth and password auth |
| aamirdev17.1@gmail.com | Order-Service | Render | https://swiftship-order-service.onrender.com | https://swiftship-order-service.onrender.com/keepalive.html | Deployed | Orders and customer addresses |
| aamirdev17.3@gmail.com | Inventory-Service | Render | https://swiftship-inventory-service.onrender.com | https://swiftship-inventory-service.onrender.com/keepalive.html | Deployed | Product catalog and stock reservation |
| aamirdev17.4@gmail.com | Warehouse-Service | Render | https://swiftship-warehouse-service.onrender.com | https://swiftship-warehouse-service.onrender.com/keepalive.html | Deployed | Warehouse selection |
| 24hussaina@rbunagpur.in | Routing-Service | Render | https://swiftship-routing-service.onrender.com | https://swiftship-routing-service.onrender.com/keepalive.html | Deployed | Route calculation |
| aamirdev17.5@gmail.com | Tracking-Service | Render | https://swiftship-tracking-service.onrender.com | https://swiftship-tracking-service.onrender.com/keepalive.html | Deployed | Delivery tracking and live updates |
| aamirdev17.6@gmail.com | Notification-Service | Render | https://swiftship-notification-service.onrender.com | https://swiftship-notification-service.onrender.com/keepalive.html | Deployed | In-app and email notifications |
| aamirr.1704@gmail.com | Frontend-Service | Vercel | https://swift-ship-nu.vercel.app/ | https://swift-ship-nu.vercel.app/ | Deployed | Customer/admin UI; root page can be used for an HTTP availability check |
| aamirr.1704@gmail.com | Aiven Kafka | Aiven | Aiven Console | N/A - TCP check on broker host and port | Active | Shared Kafka broker for service events |
| aamirr.1704@gmail.com | Redis Test DB | Redis Cloud | enormous-enormous-fish-69531.db.redis.io:10407 | N/A - TCP check on `enormous-enormous-fish-69531.db.redis.io:10407` | Active | Tracking live state cache |

