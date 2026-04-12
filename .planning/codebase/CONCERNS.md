# Codebase Concerns & Risks

**Analysis Date:** 2026-04-12

---

## 🔴 Critical Issues

### 1. All Backend Endpoints Are Publicly Accessible — Auth Disabled
- **Location:** `backendOrderly/src/main/java/com/yebur/backendorderly/security/SecurityConfig.java` (lines 34–39)
- **Description:** The block that enforces JWT authentication is commented out. The active rule is `.anyRequest().permitAll()`, meaning every endpoint — orders, employees, cash sessions, products — is open to anyone without a token. The JWT infrastructure (JwtService, JwtAuthFilter) is wired but completely bypassed in authorization decisions.
- **Risk:** Any attacker with network access can read all orders, create/delete orders, view all employee data, and manipulate cash sessions. The entire security model is non-functional.
- **Recommendation:** Uncomment (or replace) the `.authorizeHttpRequests` block; at minimum: `requestMatchers("/auth/**").permitAll()` and `.anyRequest().authenticated()`. Then add RBAC with `@PreAuthorize` for admin-only endpoints (employee management, cash operations).

### 2. Hardcoded Database Password in Version-Controlled Config
- **Location:** `backendOrderly/src/main/resources/application.yml` (line 5)
- **Description:** `password: krypton` is stored in plain text in the repository. The file is committed to git and visible to anyone with repo access.
- **Risk:** Credential exposure. If the repo is ever made public or shared, the DB is compromised.
- **Recommendation:** Replace with `${DB_PASSWORD}` environment variable. Use `.env` files or a secrets manager for local dev; CI/CD secrets for deployment.

### 3. Placeholder JWT Secret in Production Config
- **Location:** `backendOrderly/src/main/resources/application.properties` (line 2)
- **Description:** `app.jwt.secret=CHANGE_ME_TO_LONG_RANDOM_SECRET_32_CHARS_MIN` — the secret is a literal placeholder string. Any JWT signed with this "secret" is trivially forgeable.
- **Risk:** An attacker can craft valid JWTs for any user, bypassing authentication entirely — even if auth enforcement is re-enabled.
- **Recommendation:** Replace with an environment variable `${JWT_SECRET}` and generate a secure random 256-bit key before any deployment.

### 4. Hardcoded IP Addresses Across All Client Applications
- **Location:**
  - `frontend/OrderlyTablet/app/src/main/java/com/example/orderlytablet/services/RetrofitClient.kt` (line 13): `http://192.168.1.136:8080/`
  - `frontend/OrderlyTablet/app/src/main/java/com/example/orderlytablet/ui/screens/OrdersViewModel.kt` (line 38): `ws://192.168.1.136:8080/ws/overview/tablet`
  - `frontend/OrderlyPhone/app/src/main/java/com/example/orderlyphone/di/AppModule.kt` (line 33): `http://10.0.2.2:8080/` (Android emulator loopback — **never works on a real device**)
  - `frontend/orderly/src/main/java/com/yebur/service/ApiClient.java` (line 13): `http://localhost:8080` (only works on the same machine as the backend)
- **Description:** Every client has a developer-specific URL baked as a compile-time constant. Deploying to any real environment requires a rebuild.
- **Risk:** Operational — all clients break when the backend moves or the dev machine changes. The phone app running on a real device will always fail because `10.0.2.2` is the emulator's loopback.
- **Recommendation:** Externalize via `BuildConfig` fields (Android) or config files read at startup (desktop). At minimum, inject via environment/build variant.

### 5. WebSocket Endpoint Has No Authentication and Accepts Any Origin
- **Location:** `backendOrderly/src/main/java/com/yebur/backendorderly/websocket/WebSocketConfig.java` (line 20): `.setAllowedOrigins("*")`
- **Description:** The `/ws/overview/tablet` WebSocket endpoint allows connections from any origin with no authentication. Combined with the open REST API, any device on the network can stream live order updates.
- **Risk:** Real-time order data (table numbers, food items, order status) is exposed to anyone on the local network. In a restaurant setting this is the kitchen display data.
- **Recommendation:** Restrict allowed origins to known client origins. Add token-based handshake authentication (pass JWT in query param or subprotocol header on connect).

### 6. Cleartext HTTP Allowed on Both Android Apps
- **Location:** `frontend/OrderlyPhone/app/src/main/AndroidManifest.xml` (line 8): `android:usesCleartextTraffic="true"` ; `frontend/OrderlyTablet/app/src/main/AndroidManifest.xml` (line 11): same.
- **Description:** Both apps permit unencrypted HTTP connections. Combined with hardcoded `http://` URLs, all API traffic (including JWTs and order data) is sent in plaintext.
- **Risk:** Anyone on the same WiFi can intercept credentials and order data via passive sniffing.
- **Recommendation:** Enable HTTPS on the backend (or use a reverse proxy like Nginx with TLS). Remove `usesCleartextTraffic="true"`. Use `https://` and `wss://` URLs.

---

## 🟡 Important Warnings

### 1. `@NotBlank` Applied to a `LocalDateTime` Field — Validator Is a No-Op
- **Location:** `backendOrderly/src/main/java/com/yebur/backendorderly/order/OrderRequest.java` (line 15)
- **Description:** `@NotBlank private LocalDateTime datetime` — `@NotBlank` only applies to `CharSequence`. On a `LocalDateTime`, the constraint is silently skipped by the Bean Validation framework, so a null `datetime` will pass validation and cause a DB constraint violation deeper in the stack.
- **Impact:** Misleading code; runtime `NOT NULL` constraint violation instead of a clean 400 validation error.

### 2. `ddl-auto: update` and SQL Logging Enabled — Unsuitable for Production
- **Location:** `backendOrderly/src/main/resources/application.yml` (lines 9–14)
- **Description:** `hibernate.ddl-auto: update` will attempt to auto-migrate the schema on every server restart, risking data loss or constraint conflicts. `show-sql: true` and `format_sql: true` print every SQL query to stdout, adding significant noise and performance overhead.
- **Impact:** Schema corruption risk on updates; performance degradation in production; sensitive query data in logs.

### 3. WebSocket Has No Reconnection Logic — Kitchen Display Goes Stale Silently
- **Location:** `frontend/OrderlyTablet/app/src/main/java/com/example/orderlytablet/services/OrderWebSocketClient.kt` (lines 40–46)
- **Description:** `onFailure` and `onClosed` callbacks only log errors. No reconnection attempt is made. If the connection drops during service, the tablet continues showing the last known state with no indication to kitchen staff that it is out of date.
- **Impact:** Orders placed after a disconnect will never appear on the kitchen display until manual app restart. In a restaurant context this can directly cause missed orders.

### 4. No Global Exception Handler — All RuntimeExceptions Become Opaque 500s
- **Location:** Entire `backendOrderly/src/main/java/` package — no `@ControllerAdvice` class exists.
- **Description:** Services throw `RuntimeException`, `IllegalStateException`, and `IllegalArgumentException` with no central handler. Spring Boot will respond with a default 500 and a full stack trace in the response body (in development mode), or an empty body. Clients cannot distinguish "Product not found" from a DB connection failure.
- **Impact:** Poor API ergonomics for all three frontends; leaked internal details in error messages.

### 5. Sensitive Financial Data Logged to stdout
- **Location:** `backendOrderly/src/main/java/com/yebur/backendorderly/cashoperations/CashOperationService.java` (line 47)
- **Description:** `System.out.println(paidCash + " " + paidCard)` logs cash and card totals on every call to `findCashOperationDTOBySessionId`. This is production code, not debug code.
- **Impact:** Financial figures in plain logs; if logs are shipped to a log aggregator, unauthorized personnel may see revenue data.

### 6. `updateOrder` PUT Endpoint Returns 201 Created Instead of 200 OK
- **Location:** `backendOrderly/src/main/java/com/yebur/backendorderly/order/OrderController.java` (line 86)
- **Description:** `return ResponseEntity.status(HttpStatus.CREATED).body(orderRequest)` is returned from a PUT (update) operation. The response body also echoes back the request object, not the updated entity.
- **Impact:** Clients relying on HTTP semantics will misinterpret updates as creations. The PC frontend's `OrderService.updateOrder` discards the response anyway, hiding this bug.

### 7. `OrdersViewModel` (Tablet) Double-Loads Orders on Startup
- **Location:** `frontend/OrderlyTablet/app/src/main/java/com/example/orderlytablet/ui/screens/OrdersViewModel.kt` (line 30) + `frontend/OrderlyTablet/app/src/main/java/com/example/orderlytablet/ui/OrdersScreen.kt`
- **Description:** `init { connectWebSocket(); loadOrders() }` triggers one fetch, then `LaunchedEffect(Unit) { viewModel.loadOrders() }` in `OrdersScreen` triggers a second. Results in two full order-list API calls on every screen entry.
- **Impact:** Double network load on launch; race conditions if the two responses arrive out of order.

### 8. `updateSingleOrder` Fetches ALL Orders to Update ONE
- **Location:** `frontend/OrderlyTablet/app/src/main/java/com/example/orderlytablet/ui/screens/OrdersViewModel.kt` (lines 91–108)
- **Description:** When a single-order WebSocket event arrives, `updateSingleOrder()` calls `getOrdersWithDetails()` — the full list — then filters to find the changed order. There is no per-order endpoint used here.
- **Impact:** Unnecessary bandwidth and backend load. Scales poorly as the active order count grows during a busy service.

### 9. `findAllOrderDTOByStatus` Filters In-Memory, Not in DB
- **Location:** `backendOrderly/src/main/java/com/yebur/backendorderly/order/OrderService.java` (lines 38–42)
- **Description:** `findAllOrderDTOByStatus` calls `orderRepository.findAllOrderDTO()` (all orders) then streams over the result with a `.filter()` in Java. No SQL `WHERE` clause is used.
- **Impact:** Full table scan on every status-filtered request. Significant performance issue as historical orders accumulate.

### 10. Error Handling Swallowed Silently in Multiple Places
- **Location:**
  - `frontend/orderly/src/main/java/com/yebur/controller/StartController.java` (line 107): `} catch (Exception ignored) {}`
  - `frontend/orderly/src/main/java/com/yebur/controller/ReceiptController.java` (line 93): `} catch (Exception ignored) {`
  - `frontend/orderly/src/main/java/com/yebur/controller/PosController.java` (lines 89–90, 202–203): `e.printStackTrace()` with no user notification
  - `frontend/orderly/src/main/java/com/yebur/controller/ShiftOperationsController.java` (lines 55–56): `e.printStackTrace()`
- **Description:** Multiple exception catch blocks either discard the error silently or only print to stderr, with no user-visible feedback. Failures during order creation, receipt printing, and shift operations are invisible to cashiers.
- **Impact:** Staff may believe an operation succeeded when it failed; data inconsistency.

### 11. OkHttp BODY-Level Logging Active in Production Build (Tablet)
- **Location:** `frontend/OrderlyTablet/app/src/main/java/com/example/orderlytablet/services/RetrofitClient.kt` (lines 14–16)
- **Description:** `HttpLoggingInterceptor.Level.BODY` logs full request and response bodies to Logcat at all times. This is not guarded by a `BuildConfig.DEBUG` check.
- **Impact:** Order contents, product data, and API responses are readable in Logcat on production devices.

---

## 🔵 Technical Debt

### 1. Critical Navigation Actions Are TODO Stubs in OrderlyPhone
- **Location:** `frontend/OrderlyPhone/app/src/main/java/com/example/orderlyphone/ui/navigation/AppNav.kt` (lines 49–51, 67)
- **Description:** `onNewOrder = { /* TODO */ }`, `onShiftToggle = { /* TODO */ }`, `onSettings = { /* TODO */ }` are wired to buttons in the HomeScreen and ActiveOrdersScreen. Tapping them does nothing.
- **Effort to fix:** High (requires implementing the flows end-to-end)

### 2. `CashSessionService.create()` Is an Empty Stub
- **Location:** `backendOrderly/src/main/java/com/yebur/backendorderly/cashsessions/CashSessionService.java` (lines 61–63)
- **Description:** The `create(CashSessionRequest)` method returns `new CashSessionResponse()` — an empty object. Any caller expecting a real response will receive zeros/nulls. The real logic lives only in `open()`.
- **Effort to fix:** Low (remove or implement properly)

### 3. PC Frontend ApiClient Has No Timeout Configuration
- **Location:** `frontend/orderly/src/main/java/com/yebur/service/ApiClient.java`
- **Description:** All HTTP calls use `HttpURLConnection` with no `setConnectTimeout` or `setReadTimeout`. A slow or unreachable backend will hang the JavaFX UI thread indefinitely.
- **Effort to fix:** Low

### 4. OrderlyTablet Has No Dependency Injection Framework
- **Location:** `frontend/OrderlyTablet/app/src/main/java/com/example/orderlytablet/services/RetrofitClient.kt`
- **Description:** Uses a Kotlin `object` (global singleton) instead of Hilt/Dagger used in OrderlyPhone. `OrdersViewModel` directly instantiates `OrderWebSocketClient`. This makes unit testing and configuration swapping impossible.
- **Effort to fix:** Medium

### 5. Mixed Languages in Source Comments — Russian, Spanish, and English
- **Location:** Across all modules — e.g., `RetrofitClient.kt` (Russian), `PosController.java` (Spanish and English mixed), `SecurityConfig.java` (Russian)
- **Description:** Code comments are written in at least three languages throughout the codebase, often mixed within the same file. Reduces maintainability and onboarding ease.
- **Effort to fix:** Low (cleanup pass, no logic changes)

### 6. `OrderDetailRequest.orderId` Is Nullable Without Null Guard in Service
- **Location:** `backendOrderly/src/main/java/com/yebur/backendorderly/orderdetail/OrderDetailRequest.java` and `OrderDetailService.java`
- **Description:** `orderId` has no `@NotNull` annotation. When null, the service calls `orderRepository.findById(null)` which throws a JPA exception rather than returning a clean 400 error.
- **Effort to fix:** Low (add `@NotNull` + update error handler)

### 7. Forgot Password and Biometric Auth Are Placeholders
- **Location:** `frontend/OrderlyPhone/app/src/main/java/com/example/orderlyphone/ui/screen/login/LoginScreen.kt` (lines 190, 222)
- **Description:** `/* TODO forgot password */` and `/* TODO face id */` — UI buttons are rendered but clicking them does nothing.
- **Effort to fix:** High (requires backend reset flow and Android BiometricPrompt integration)

### 8. Receipt Has Hardcoded Business Name and Address
- **Location:** `frontend/orderly/src/main/java/com/yebur/service/ReceiptPdfService.java` (lines 35–37)
- **Description:** `SHOP_NAME = "Orderly Bar"`, `SHOP_ADDRESS = "C/ Valencia 123, Valencia"`, `SHOP_PHONE = "+34 600 000 000"` are literal constants. Every establishment using this software would need a rebuild.
- **Effort to fix:** Low (read from config or DB settings table)

---

## 🟢 Missing Features (for bar management completeness)

- [ ] **Role-based access control** — roles (`USER`, admin) exist in the DB schema but are never enforced; all endpoints are open regardless of role.
- [ ] **"New Order" flow in OrderlyPhone** — the waiter app has no way to create a new order (button is a TODO stub).
- [ ] **Shift start/end in OrderlyPhone** — shift toggle button is a TODO stub; waiters cannot clock in/out from the phone app.
- [ ] **Settings screen in OrderlyPhone** — no configuration UI exists.
- [ ] **Forgot password / password reset** — no backend endpoint or email flow; only a UI placeholder exists.
- [ ] **Real-time sync in OrderlyPhone** — the phone app has no WebSocket connection; order status changes are only visible on manual refresh via `loadOrdersData()`.
- [ ] **WebSocket reconnection with backoff** — the tablet display goes stale silently on connection drop.
- [ ] **Stock management** — `Product.stock` field exists in the entity and DB, but no endpoint decrements stock on order creation. It is never read or validated during ordering.
- [ ] **Environment-based server configuration** — all three clients require a rebuild to point to a different backend server.
- [ ] **Token refresh / expiry handling** — the phone app stores a JWT but has no logic to detect expiry or refresh it; users receive silent 403s after 60 minutes.
- [ ] **Pagination on order history** — no paginated endpoint exists for historical (closed/paid) orders; volume will grow unboundedly in the active query.
- [ ] **Audit trail for cash operations** — there is a `CashOperation` model but no immutable append-only log; records can be updated and deleted.

---

## Configuration Issues

| Item | File | Current Value | Problem |
|---|---|---|---|
| DB password | `application.yml:5` | `krypton` (plaintext) | Committed to repo |
| JWT secret | `application.properties:2` | Placeholder string | Cryptographically invalid |
| `ddl-auto` | `application.yml:9` | `update` | Risk of schema corruption on restart |
| `show-sql` | `application.yml:11` | `true` | Performance degradation + log noise |
| `format_sql` | `application.yml:13` | `true` | Performance degradation |
| Tablet HTTP base URL | `RetrofitClient.kt:13` | `192.168.1.136` | Developer's home network IP |
| Tablet WebSocket URL | `OrdersViewModel.kt:38` | `ws://192.168.1.136:8080` | Developer's home network IP |
| Phone base URL | `AppModule.kt:33` | `10.0.2.2` | Emulator loopback — fails on real devices |
| PC frontend base URL | `ApiClient.java:13` | `localhost:8080` | Fails on any machine other than the backend host |

---

## Security Notes

- **All REST endpoints are open** — `SecurityConfig.anyRequest().permitAll()` is the enforced rule; the JWT filter runs but its result is never acted upon for access control.
- **WebSocket has no auth** — `/ws/overview/tablet` accepts connections from any origin with no token check.
- **HTTPS is not enforced** — `usesCleartextTraffic="true"` in both Android manifests; all client URLs use `http://`.
- **DB credential committed to git** — `application.yml` password stored in plaintext.
- **JWT secret is a placeholder** — tokens are trivially forgeable if the secret is not rotated before deployment.
- **OkHttp BODY logging in production** — full request/response payloads logged to Logcat in the Tablet app (not guarded by `BuildConfig.DEBUG`).
- **PC frontend sends no `Authorization` header** — `ApiClient.java` makes all REST calls with no token; it would fail if auth enforcement were re-enabled.
- **No rate limiting** — the `/auth/login` endpoint has no brute-force protection.

---

## Scalability Notes

- **In-memory status filtering** — `OrderService.findAllOrderDTOByStatus()` fetches the entire orders table and filters in Java; grows O(n) with history.
- **WebSocket session set is process-local** — `OrdersTabletWebSocketHandler` stores sessions in a `HashSet`. Horizontal scaling (multiple server instances) would break real-time delivery unless a shared pub/sub broker (Redis, STOMP broker) is introduced.
- **`updateSingleOrder` fetches all orders** — every targeted WebSocket event causes a full `getOrdersWithDetails()` call on the backend; inefficient under concurrent updates.
- **No connection pool configuration** — `application.yml` does not configure Hikari pool size; default is 10 connections, which may be insufficient under concurrent tablet + phone + desktop load.
- **No caching** — product and category lists are fetched fresh on every POS screen load (PC frontend); these are read-heavy and effectively static during a shift.
- **HttpURLConnection in PC frontend** — uses Java's built-in `HttpURLConnection` with no connection pooling or keep-alive tuning; each request opens a new TCP connection.
