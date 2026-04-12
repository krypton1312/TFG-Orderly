# System Architecture

**Analysis Date:** 2026-04-12

## Overview

Orderly is a bar/restaurant management system composed of four independent clients communicating with a single Spring Boot REST API. Waiters take orders on an Android phone app, the kitchen/bar receives live order updates on an Android tablet via WebSocket, and managers operate a full-featured JavaFX desktop POS. The backend persists all data to PostgreSQL and coordinates real-time events through a plain WebSocket server.

## Architecture Pattern

- **Backend:** Domain-driven layered architecture — Controller → Service (with interface) → Repository → Entity — with one vertical slice per business domain (order, employee, product, etc.).
- **Android apps (Phone & Tablet):** MVVM pattern with Jetpack Compose UI. The Phone app uses Hilt for DI; the Tablet app uses manual ViewModel instantiation.
- **PC Frontend:** JavaFX MVC — FXML views driven by `*Controller` Java classes; a thin service layer wraps raw `HttpURLConnection` calls.

---

## System Components & Roles

### Backend (`backendOrderly/`)

- **Role:** Central REST API on port 8080 + plain WebSocket server. Single source of truth for all data.
- **Layers:**
  1. **Controller** — annotated `@RestController`; maps HTTP verbs to service calls; returns `ResponseEntity<DTO>`.
  2. **Service** — business logic; each domain has a `*ServiceInterface` + `*Service` implementation.
  3. **Repository** — Spring Data JPA `JpaRepository` extensions; one per entity.
  4. **Entity** — Lombok-annotated JPA classes mapped to PostgreSQL tables.
- **Domain modules:**
  | Package | Purpose |
  |---------|---------|
  | `auth` | Login (`/auth/login`) and employee registration (`/auth/register`) |
  | `employee` | Employee CRUD, statuses (ACTIVE / INACTIVE) |
  | `role` | Employee roles (USER, ADMIN, etc.) |
  | `order` | Order lifecycle management (OPEN, PAID, CANCELLED, etc.) |
  | `orderdetail` | Line items on an order; tracks per-item status |
  | `product` | Menu products with category and supplement associations |
  | `category` | Product categories |
  | `supplements` | Optional add-ons attached to products |
  | `resttable` | Restaurant tables with status and floor-plan position |
  | `cashsessions` | Shift/cash session tracking (OPEN/CLOSED) |
  | `cashoperations` | Cash in/out operations within a session |
  | `cashcount` | End-of-session denomination cash count |
  | `shiftrecord` | Shift records per employee |
  | `shiftrecordstory` | Historical shift snapshots |
  | `client` | Client entity (currently referenced by orders) |
  | `overview` | Aggregated read-only queries for dashboards and tablets |
  | `websocket` | WS server endpoint `/ws/overview/tablet` |
  | `security` | JWT filter chain; `CustomUserDetailsService`; `JwtService` |
  | `config` | `PasswordConfig` (BCrypt bean) |

- **Auth:** JWT Bearer (JJWT 0.11.5). `JwtAuthFilter` runs before `UsernamePasswordAuthenticationFilter`. **Note:** `SecurityConfig` currently sets `.anyRequest().permitAll()` — the JWT filter is wired but all endpoints are publicly accessible (likely toggled during development).
- **WebSocket:** `WebSocketConfig` registers `OrdersTabletWebSocketHandler` at `/ws/overview/tablet`. On order mutations, service layer calls `WsNotifier.send(WsEvent)` which broadcasts JSON to all connected tablet sessions.

### Tablet App (`frontend/OrderlyTablet/`)

- **Role:** Kitchen/bar real-time order display. Shows incoming orders grouped by table with per-item status.
- **Screens:**
  - `OrdersScreen` — main composable listing all active orders
  - `OrderCard` — composable card for a single order with its details
  - `OrderCardPreview` — preview composable for Android Studio previews
- **ViewModel:** `OrdersViewModel` — holds `StateFlow<OrdersUiState>` (Loading / Success / Error). On init, opens a WebSocket and calls `loadOrders()` via `RetrofitClient` → `ApiService`.
- **Real-time:** `OrderWebSocketClient` (OkHttp) connects to `ws://<host>:8080/ws/overview/tablet`. Handles events: `ORDER_DETAIL_CREATED`, `ORDER_DETAIL_UPDATED`, `ORDER_DETAIL_DELETED`, `ORDER_DETAIL_STATUS_CHANGED`, `ORDER_TOTAL_CHANGED`, `ORDER_CREATED`, `ORDER_DELETED`. Triggers debounced reload or single-order update.
- **Auth:** None — tablet endpoint is unauthenticated. No token storage or interceptor.

### Phone App (`frontend/OrderlyPhone/`)

- **Role:** Waiter-facing order-taking interface. Login, browse tables, create/view orders, add products.
- **Screens (routes):**
  - `login` → `LoginScreen` / `LoginViewModel` / `LoginState`
  - `home` → `HomeScreen` / `HomeViewModel` / `HomeState`
  - `orders` → `ActiveOrdersScreen` / `OrdersViewModel` / `OrdersState`
  - `order_details/{orderId}/{tableId}` → `OrderDetailScreen` / `OrderDetailViewModel` / `OrderDetailState`
  - `products/{orderId}/{tableId}` → `ProductsScreen` / `ProductsViewModel` / `ProductsState`
- **Navigation:** Compose NavHost with string routes in `AppNav.kt`. Hilt ViewModels injected via `hiltViewModel()`.
- **Data layer:**
  - Remote APIs (Retrofit): `AuthApi`, `CategoryApi`, `EmployeeApi`, `OrderDetailApi`, `OverviewApi`, `ProductsApi`
  - Interceptor: `AuthInterceptor` reads JWT from `TokenStore` (DataStore) and adds `Authorization: Bearer <token>` header to every request
  - Local persistence: `TokenStore` (JWT), `CashSessionStore` (active shift) — both backed by Android DataStore
  - Repository: `AuthRepository` (login/logout abstraction)
- **DI:** Hilt `AppModule` (`SingletonComponent`) provides `TokenStore`, `OkHttpClient` (with `AuthInterceptor`), `Gson` (custom LocalDate/LocalDateTime adapters), `Retrofit`, and all API interfaces.

### PC Frontend (`frontend/orderly/`)

- **Role:** Full manager dashboard — POS terminal, shift management, product/category/employee/supplement data management, cash counting, receipt generation.
- **Entry point:** `App.java` (JavaFX `Application`); loads `portal.fxml` as the root scene.
- **Screens / FXML:**
  | FXML | Controller | Purpose |
  |------|-----------|---------|
  | `portal/portal.fxml` | `PortalController` | Main shell with sidebar nav; lazy-loads sub-views into center pane |
  | `portal/views/start.fxml` | `StartController` | Landing screen; open/resume cash session |
  | `portal/views/data.fxml` | `DataController` | Master data management (products, categories, tables, employees, supplements) |
  | `portal/views/dataOperation.fxml` | `DataOperationController` | CRUD operations for master data |
  | `portal/views/shiftOperations.fxml` | `ShiftOperationsController` | Cash in/out operations during a shift |
  | `portal/views/shiftOperationClose.fxml` | `ShiftOperationCloseController` | Close shift workflow |
  | `portal/views/cashCountModel.fxml` | `CashCountModelController` | Denomination-by-denomination cash count |
  | `pos/pos.fxml` | `PosController` | Main POS — category/product tiles, order editing, table selection |
  | `pos/operations.fxml` | `OperationsController` | POS sub-operations (discount, transfer, split) |
  | `pos/journalEntry.fxml` | `JournalEntryController` | Journal/log view inside POS |
  | `payment/payment.fxml` | `PartialPaymentController` | Partial payment screen |
  | `receipt/receipt-view.fxml` | `ReceiptController` | Receipt preview before PDF generation |
- **HTTP:** Raw `HttpURLConnection` via `ApiClient` (static helper). Hard-coded `BASE_URL = "http://localhost:8080"`. **No JWT header is added** in `ApiClient` — PC frontend runs unauthenticated against the currently-open backend policy.
- **PDF:** `ReceiptPdfService` and `ReceiptFxToPdfService` (Apache PDFBox) generate receipt PDFs.

---

## Data Flow — Order Lifecycle

1. **Waiter (Phone)** authenticates via `POST /auth/login`; JWT stored in `TokenStore` (DataStore).
2. Waiter opens an order: `POST /orders` — backend creates `Order` entity linked to `RestTable` and `Employee`.
3. Waiter adds items: `POST /orderdetails` — backend creates `OrderDetail` records, updates `Order.total`, and calls `WsNotifier.send(WsEvent{type="ORDER_DETAIL_CREATED", ...})`.
4. **Backend WS** broadcasts `JSON WsEvent` to all active sessions on `/ws/overview/tablet`.
5. **Tablet** (`OrderWebSocketClient`) receives event; `OrdersViewModel` calls `GET /overview/tablet` to refresh the `OrderWithOrderDetailResponse` list.
6. **Tablet** displays updated order cards to kitchen/bar staff who update item statuses.
7. **Manager (PC)** polls `GET /overview` via `OverviewService`; sees `TableWithOrderResponse` aggregation showing per-table order state.
8. Manager closes order: `PUT /orders/id/{id}` (sets state to PAID) → `POST /cashsessions` to record in cash session → `ReceiptController` renders receipt → `ReceiptFxToPdfService` generates PDF.

---

## Domain Model

Key JPA entities and their relationships:

| Entity | Table | Key Relations |
|--------|-------|---------------|
| `Employee` | `employees` | ManyToMany → `Role`; linked to `Order`, `ShiftRecord` |
| `Role` | `roles` | ManyToMany ← `Employee` |
| `RestTable` | `rest_tables` | Has `TableStatus`, `RestTablePosition`; linked to `Order` |
| `Order` | `orders` | ManyToOne → `Employee`, `RestTable`, `Client`; OneToMany → `OrderDetail`; has `OrderStatus` |
| `OrderDetail` | `order_details` | ManyToOne → `Order`, `Product`, `CashSession`; has `OrderDetailStatus`; JoinTable → `order_details_products` |
| `Product` | `products` | ManyToOne → `Category`; has `ProductDestination`; ManyToMany → `Supplement` |
| `Category` | `categories` | OneToMany → `Product` |
| `Supplement` | `supplements` | ManyToMany ← `Product` |
| `Client` | `clients` | Referenced by `Order` |
| `CashSession` | `cash_sessions` | Has `CashSessionStatus`; linked to `OrderDetail`, `CashOperation`, `CashCount` |
| `CashOperation` | `cash_operations` | ManyToOne → `CashSession`; has `CashOperationType` |
| `CashCount` | `cash_counts` | ManyToOne → `CashSession` |
| `ShiftRecord` | `shift_records` | ManyToOne → `Employee` |
| `ShiftRecordStory` | `shift_record_stories` | Historical snapshot of `ShiftRecord` |

---

## Key Design Patterns

- **Repository Pattern:** Spring Data JPA `JpaRepository` per entity — `OrderRepository`, `EmployeeRepository`, etc.
- **Service Interface Pattern:** Every domain service is hidden behind a `*ServiceInterface` (e.g., `OrderServiceInterface` + `OrderService`) enabling future swapping or mocking.
- **DTO Pattern:** Strict separation between API-facing `*Request`/`*Response` classes and JPA entities; entities never returned directly.
- **Builder / Lombok:** All entities and DTOs use Lombok annotations (`@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder` where needed).
- **MVVM (Android):** `StateFlow`-driven ViewModels with sealed `*UiState` classes; Compose collects state via `collectAsState()`.
- **Observer / WebSocket push:** Backend `WsNotifier` → `OrdersTabletWebSocketHandler.broadcast()` → Tablet `OrderWebSocketClient.onMessage()` → ViewModel state refresh.
- **Facade (PC ApiClient):** Static `ApiClient` hides all `HttpURLConnection` boilerplate; service classes call `ApiClient.get/post/put/delete`.
- **Lazy View Loading (PC PortalController):** Views loaded from FXML on first navigation and cached in a `Map<String, Node>` for reuse.
