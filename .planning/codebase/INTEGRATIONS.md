# Integrations & Communication

**Analysis Date:** 2026-04-12

## Backend ↔ Tablet (OrderlyTablet)

- **Protocol 1 — REST:** Retrofit 2.11.0 + OkHttp 4.12.0 + Gson converter
  - Base URL: `http://192.168.1.136:8080/` (configured in `frontend/OrderlyTablet/app/src/main/java/com/example/orderlytablet/services/RetrofitClient.kt`)
  - Endpoints consumed:
    - `GET /orders` — list of all orders
    - `GET /overview/tablet` — orders with order detail aggregates
    - `PUT /orderDetails/change-status/{status}` — bulk status update for order details (body: list of IDs)
  - Auth: No JWT token attached — all Tablet REST calls are unauthenticated (security is `permitAll()` on backend)
- **Protocol 2 — WebSocket:** OkHttp `WebSocket` API used directly in `frontend/OrderlyTablet/app/src/main/java/com/example/orderlytablet/services/OrderWebSocketClient.kt`
  - Server endpoint: `ws://<host>:8080/ws/overview/tablet`
  - Direction: server → tablet (server broadcasts events; tablet is read-only listener)
  - Message format: JSON object with fields `type`, `orderId`, `overviewId`, `ts`
  - Keep-alive: OkHttp ping interval of 20 seconds
  - Auth: No token; backed by `setAllowedOrigins("*")` in `WebSocketConfig.java`

## Backend ↔ Phone (OrderlyPhone)

- **Protocol — REST:** Retrofit 2.11.0 + OkHttp 4.12.0 + Gson converter
  - Base URL: `http://10.0.2.2:8080/` in emulator; production IP to be configured per device (set in `frontend/OrderlyPhone/app/src/main/java/com/example/orderlyphone/di/AppModule.kt`)
  - API interfaces under `frontend/OrderlyPhone/app/src/main/java/com/example/orderlyphone/data/remote/`:
    - `AuthApi` — `POST /auth/login` (no auth header)
    - `CategoryApi` — category CRUD endpoints
    - `EmployeeApi` — employee management endpoints
    - `OrderDetailApi` — order detail endpoints
    - `OverviewApi` — overview/aggregation endpoints
    - `ProductsApi` — product management endpoints
  - Auth: JWT Bearer token injected by `AuthInterceptor` (`frontend/OrderlyPhone/app/src/main/java/com/example/orderlyphone/data/remote/interceptor/AuthInterceptor.kt`). Token skipped for `/auth/*` requests; otherwise reads token from `TokenStore` (DataStore Preferences) and adds `Authorization: Bearer <token>` header.
  - Token storage: `androidx.datastore:datastore-preferences:1.1.1` via `TokenStore` (local, persisted)
  - DI: Hilt (Dagger 2.52) wires Retrofit, OkHttpClient, Gson, and all API interfaces in `AppModule`
  - Date adapters: custom Gson adapters for `LocalDate` and `LocalDateTime` (`LocalDateAdapter.kt`, `LocalDateTimeAdapter.kt`)

## Backend ↔ PC Frontend (orderly)

- **Protocol — REST:** Plain `java.net.HttpURLConnection` (no third-party HTTP library)
  - Base URL: `http://localhost:8080` (hardcoded in `frontend/orderly/src/main/java/com/yebur/service/ApiClient.java`)
  - Verbs supported: GET, POST, PUT, DELETE (all implemented in `ApiClient.java`)
  - JSON parsing: Jackson ObjectMapper with `JavaTimeModule` for `LocalDate`/`LocalDateTime`
  - Services wrapping the client (all under `frontend/orderly/src/main/java/com/yebur/service/`):
    - `OrderService` — order CRUD
    - `OrderDetailService` — order detail CRUD
    - `ProductService` — product management
    - `CategoryService` — category management
    - `RestTableService` — table management
    - `OverviewService` — overview/dashboard data
    - `CashSessionService`, `CashCountService`, `CashOperationService` — cash management
    - `SupplementService` — supplements/modifiers
    - `ReceiptPdfService`, `ReceiptFxToPdfService` — PDF generation (uses `pdfbox`)
  - Auth: No JWT token attached in `ApiClient.java`; the backend currently has `anyRequest().permitAll()` so all PC requests proceed unauthenticated. JWT infrastructure exists but enforcement is commented out in `SecurityConfig.java`.

## External Services

- None detected. No third-party payment providers, cloud services, messaging platforms, analytics, error tracking, push notifications, or CDN integrations are present in any component.

## Real-time / Events

- **WebSocket (server → tablet broadcast):**
  - Server: Spring `@EnableWebSocket` handler at `/ws/overview/tablet` (`backendOrderly/src/main/java/com/yebur/backendorderly/websocket/WebSocketConfig.java`)
  - Handler: `OrdersTabletWebSocketHandler` maintains a synchronized `Set<WebSocketSession>` and broadcasts JSON to all connected sessions via `WsNotifier`
  - Event types (enum `WsEventType`): `ORDER_CREATED`, `ORDER_DELETED`, `ORDER_TOTAL_CHANGED`, `ORDER_DETAIL_CREATED`, `ORDER_DETAIL_UPDATED`, `ORDER_DETAIL_DELETED`, `ORDER_DETAIL_STATUS_CHANGED`
  - Event payload (`WsEvent`): `type`, `orderId`, `overviewId`, `detailIds` (list), `destinations` (set), `ts` (Instant)
  - Consumer: Tablet (`OrderWebSocketClient.kt`) listens and dispatches events to UI via callback
- **Polling / SSE:** Not used in any component.
- **STOMP:** Not used; backend uses raw Spring WebSocket (not STOMP broker relay).

---

*Integration audit: 2026-04-12*
