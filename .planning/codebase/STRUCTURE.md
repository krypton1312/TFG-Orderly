# Project Structure

**Analysis Date:** 2026-04-12

---

## Repository Layout

```
TFG-Orderly/
├── backendOrderly/          # Spring Boot REST API + WebSocket server
│   ├── pom.xml
│   └── src/main/java/com/yebur/backendorderly/
│       ├── [domain packages — one per business concept]
│       └── BackendOrderlyApplication.java
├── frontend/
│   ├── orderly/             # JavaFX 21 desktop POS (Java 23)
│   │   ├── pom.xml
│   │   └── src/main/
│   │       ├── java/com/yebur/
│   │       └── resources/com/yebur/
│   ├── OrderlyPhone/        # Android Kotlin/Compose waiter app
│   │   ├── settings.gradle.kts
│   │   └── app/src/main/java/com/example/orderlyphone/
│   └── OrderlyTablet/       # Android Kotlin/Compose kitchen display
│       ├── settings.gradle.kts
│       └── app/src/main/java/com/example/orderlytablet/
└── .planning/               # GSD planning artifacts
    └── codebase/
```

---

## Backend Package Structure

```
backendOrderly/src/main/java/com/yebur/backendorderly/
├── BackendOrderlyApplication.java          — Spring Boot entry point (@SpringBootApplication)
│
├── auth/                                   — Authentication endpoints
│   ├── AuthController.java                 — POST /auth/login, POST /auth/register
│   └── dto/
│       ├── LoginRequest.java
│       ├── RegisterRequest.java
│       └── AuthResponse.java               — Returns JWT token
│
├── security/                               — Spring Security + JWT
│   ├── SecurityConfig.java                 — Filter chain config (STATELESS, currently permitAll)
│   ├── CustomUserDetailsService.java       — Loads Employee by email for Spring Security
│   └── jwt/
│       ├── JwtService.java                 — Token generation and validation (JJWT)
│       └── JwtAuthFilter.java              — OncePerRequestFilter; extracts Bearer token
│
├── config/
│   └── PasswordConfig.java                 — BCryptPasswordEncoder bean
│
├── websocket/                              — Plain WebSocket server
│   ├── WebSocketConfig.java                — Registers handler at /ws/overview/tablet
│   ├── OrdersTabletWebSocketHandler.java   — Manages connected sessions; broadcasts JSON
│   ├── WsNotifier.java                     — Service-layer facade to trigger broadcasts
│   ├── WsEvent.java                        — Event payload (type, orderId, overviewId, ts)
│   └── WsEventType.java                    — Enum of event type constants
│
├── overview/                               — Aggregated read-only query layer
│   ├── OverviewController.java             — GET /overview, /overview/tablet, /overview/products-with-supplements-by-category/id/{id}, /overview/phone/dashboard-start
│   ├── OverviewService.java                — Joins tables + orders + details for dashboards
│   ├── OverviewRepository.java             — Custom JPQL/native queries
│   ├── TableWithOrderResponse.java         — Table + active order summary (PC dashboard)
│   ├── OrderWithOrderDetailResponse.java   — Order + all line items (tablet display)
│   ├── ProductsWithSupplements.java        — Category products with their supplements
│   └── DashboardStartResponse.java         — Phone home screen aggregated data
│
├── order/                                  — Order lifecycle
│   ├── Order.java                          — @Entity: id, datetime, state, paymentMethod, total → Employee, RestTable, Client, [OrderDetail]
│   ├── OrderStatus.java                    — Enum: OPEN, PAID, CANCELLED, …
│   ├── OrderController.java                — GET/POST/PUT/DELETE /orders
│   ├── OrderServiceInterface.java
│   ├── OrderService.java
│   ├── OrderRepository.java
│   ├── OrderRequest.java
│   └── OrderResponse.java
│
├── orderdetail/                            — Order line items
│   ├── OrderDetail.java                    — @Entity: id, name, product, order, cashSession; JoinTable order_details_products
│   ├── OrderDetailStatus.java              — Enum: PENDING, SERVED, …
│   ├── OrderDetailController.java          — GET/POST/PUT/DELETE /orderdetails
│   ├── OrderDetailServiceInterface.java
│   ├── OrderDetailService.java
│   ├── OrderDetailRepository.java
│   ├── OrderDetailRequest.java
│   └── OrderDetailResponse.java
│
├── product/                                — Menu products
│   ├── Product.java                        — @Entity: linked to Category, ManyToMany Supplement
│   ├── ProductDestination.java             — Enum (e.g., KITCHEN, BAR)
│   ├── ProductController.java              — GET/POST/PUT/DELETE /products
│   ├── ProductServiceInterface.java
│   ├── ProductService.java
│   ├── ProductRepository.java
│   ├── ProductRequest.java
│   └── ProductResponse.java
│
├── category/                               — Product categories
│   ├── Category.java                       — @Entity
│   ├── CategoryController.java             — GET/POST/PUT/DELETE /categories
│   ├── CategoryServiceInterface.java
│   ├── CategoryService.java
│   ├── CategoryRepository.java
│   ├── CategoryRequest.java
│   └── CategoryResponse.java
│
├── supplements/                            — Product add-ons
│   ├── Supplement.java                     — @Entity
│   ├── SupplementController.java           — GET/POST/PUT/DELETE /supplements
│   ├── SupplementServiceInterface.java
│   ├── SupplementService.java
│   ├── SupplementRepository.java
│   ├── SupplementRequest.java
│   └── SupplementResponse.java
│
├── resttable/                              — Restaurant tables
│   ├── RestTable.java                      — @Entity: name, capacity, status, position
│   ├── TableStatus.java                    — Enum: FREE, OCCUPIED, RESERVED
│   ├── RestTablePosition.java              — Enum (floor plan zones)
│   ├── RestTableController.java            — GET/POST/PUT/DELETE /resttables
│   ├── RestTableServiceInterface.java
│   ├── RestTableService.java
│   ├── RestTableRepository.java
│   ├── RestTableRequest.java
│   └── RestTableResponse.java
│
├── employee/                               — Staff management
│   ├── Employee.java                       — @Entity: UserDetails impl; roles ManyToMany
│   ├── EmployeeStatus.java                 — Enum: ACTIVE, INACTIVE
│   ├── EmployeeController.java             — GET/POST/PUT/DELETE /employees
│   ├── EmployeeServiceInterface.java
│   ├── EmployeeService.java
│   ├── EmployeeRepository.java
│   ├── EmployeeRequest.java
│   └── EmployeeResponse.java
│
├── role/                                   — Employee roles
│   ├── Role.java                           — @Entity
│   ├── RoleController.java                 — GET/POST/PUT/DELETE /roles
│   ├── RoleServiceInterface.java
│   ├── RoleService.java
│   ├── RoleRepository.java
│   ├── RoleRequest.java
│   └── RoleResponse.java
│
├── cashsessions/                           — Shift/cash session tracking
│   ├── CashSession.java                    — @Entity: shiftNo, openedAt, closedAt, status
│   ├── CashSessionStatus.java              — Enum: OPEN, CLOSED
│   ├── CashSessionController.java          — GET/POST/PUT /cashsessions
│   ├── CashSessionServiceInterface.java
│   ├── CashSessionService.java
│   ├── CashSessionRepository.java
│   ├── CashSessionRequest.java
│   └── CashSessionResponse.java
│
├── cashoperations/                         — Cash in/out within a session
│   ├── CashOperation.java                  — @Entity
│   ├── CashOperationType.java              — Enum: IN, OUT
│   ├── CashOperationController.java
│   ├── CashOperationServiceInterface.java
│   ├── CashOperationService.java
│   ├── CashOperationRepository.java
│   ├── CashOperationRequest.java
│   └── CashOperationResponse.java
│
├── cashcount/                              — End-of-shift denomination count
│   ├── CashCount.java                      — @Entity
│   ├── CashCountController.java
│   ├── CashCountServiceInterface.java
│   ├── CashCountService.java
│   ├── CashCountRepository.java
│   ├── CashCountRequest.java
│   └── CashCountResponse.java
│
├── shiftrecord/                            — Per-employee shift records
│   ├── ShiftRecord.java                    — @Entity
│   ├── ShiftRecordController.java
│   ├── ShiftRecordServiceInterface.java
│   ├── ShiftRecordService.java
│   ├── ShiftRecordRepository.java
│   ├── ShiftRecordRequest.java
│   └── ShiftRecordResponse.java
│
├── shiftrecordstory/                       — Historical shift snapshots
│   ├── ShiftRecordStory.java               — @Entity
│   ├── ShiftRecordStoryController.java
│   ├── ShiftRecordStoryServiceInterface.java
│   ├── ShiftRecordStoryService.java
│   ├── ShiftRecordStoryRepository.java
│   ├── ShiftRecordStoryRequest.java
│   └── ShiftRecordStoryResponse.java
│
└── client/
    └── Client.java                         — @Entity (no controller; referenced by Order)
```

---

## Backend Layers (Class Roster)

**Controllers (REST endpoints):**
`AuthController`, `CashCountController`, `CashOperationController`, `CashSessionController`, `CategoryController`, `EmployeeController`, `OrderController`, `OrderDetailController`, `OverviewController`, `ProductController`, `RestTableController`, `RoleController`, `ShiftRecordController`, `ShiftRecordStoryController`, `SupplementController`

**Services (business logic — interface + impl):**
`CashCountService`, `CashOperationService`, `CashSessionService`, `CategoryService`, `EmployeeService`, `OrderService`, `OrderDetailService`, `OverviewService` (no interface), `ProductService`, `RestTableService`, `RoleService`, `ShiftRecordService`, `ShiftRecordStoryService`, `SupplementService`

**Repositories (Spring Data JPA):**
`CashCountRepository`, `CashOperationRepository`, `CashSessionRepository`, `CategoryRepository`, `EmployeeRepository`, `OrderRepository`, `OrderDetailRepository`, `OverviewRepository`, `ProductRepository`, `RestTableRepository`, `RoleRepository`, `ShiftRecordRepository`, `ShiftRecordStoryRepository`, `SupplementRepository`

**Entities (JPA models):**
`CashCount`, `CashOperation`, `CashSession`, `Category`, `Client`, `Employee`, `Order`, `OrderDetail`, `Product`, `RestTable`, `Role`, `ShiftRecord`, `ShiftRecordStory`, `Supplement`

**Request DTOs:**
`LoginRequest`, `RegisterRequest`, `CashCountRequest`, `CashOperationRequest`, `CashSessionRequest`, `CategoryRequest`, `EmployeeRequest`, `OrderRequest`, `OrderDetailRequest`, `ProductRequest`, `RestTableRequest`, `RoleRequest`, `ShiftRecordRequest`, `ShiftRecordStoryRequest`, `SupplementRequest`

**Response DTOs:**
`AuthResponse`, `CashCountResponse`, `CashOperationResponse`, `CashSessionResponse`, `CategoryResponse`, `EmployeeResponse`, `OrderResponse`, `OrderDetailResponse`, `ProductResponse`, `RestTableResponse`, `RoleResponse`, `ShiftRecordResponse`, `ShiftRecordStoryResponse`, `SupplementResponse`, `TableWithOrderResponse`, `OrderWithOrderDetailResponse`, `ProductsWithSupplements`, `DashboardStartResponse`

**Enums:**
`CashOperationType`, `CashSessionStatus`, `EmployeeStatus`, `OrderDetailStatus`, `OrderStatus`, `ProductDestination`, `RestTablePosition`, `TableStatus`, `WsEventType`

**Config/Security/WS:**
`PasswordConfig`, `SecurityConfig`, `CustomUserDetailsService`, `JwtService`, `JwtAuthFilter`, `WebSocketConfig`, `OrdersTabletWebSocketHandler`, `WsNotifier`, `WsEvent`

---

## Tablet App Structure

```
OrderlyTablet/app/src/main/
├── AndroidManifest.xml
├── java/com/example/orderlytablet/
│   ├── MainActivity.kt                     — Single activity; sets content to OrderlyTabletApp()
│   ├── ui/
│   │   ├── OrdersScreen.kt                 — Main Compose screen listing all active orders
│   │   ├── OrderCard.kt                    — Composable card for one order + its details
│   │   └── OrderCardPreview.kt             — Android Studio preview composable
│   │   └── screens/
│   │       └── OrdersViewModel.kt          — StateFlow<OrdersUiState>; WebSocket + Retrofit
│   ├── services/
│   │   ├── RetrofitClient.kt               — Retrofit singleton (no auth)
│   │   ├── ApiService.kt                   — GET /overview/tablet endpoint interface
│   │   └── OrderWebSocketClient.kt         — OkHttp WS; connects to /ws/overview/tablet
│   └── response/
│       ├── OrderWithOrderDetailResponse.kt
│       ├── OrderResponse.kt
│       ├── OrderDetailResponse.kt
│       └── RestTableResponse.kt
└── res/
    └── [standard Android resource dirs]
```

**Key screens:** `OrdersScreen` (single-screen app — no navigation graph)

**WebSocket events handled:**
`ORDER_DETAIL_CREATED`, `ORDER_DETAIL_UPDATED`, `ORDER_DETAIL_DELETED`, `ORDER_DETAIL_STATUS_CHANGED` → single-order refresh via `overviewId`
`ORDER_TOTAL_CHANGED`, `ORDER_CREATED`, `ORDER_DELETED` → full list reload (debounced 700ms)

---

## Phone App Structure

```
OrderlyPhone/app/src/main/
├── AndroidManifest.xml
├── java/com/example/orderlyphone/
│   ├── app/
│   │   ├── MainActivity.kt                 — @AndroidEntryPoint; sets content to AppNav()
│   │   └── OrderlyApp.kt                   — @HiltAndroidApp Application class
│   ├── di/
│   │   └── AppModule.kt                    — @Module @InstallIn(SingletonComponent); wires Hilt graph
│   ├── data/
│   │   ├── local/
│   │   │   ├── TokenStore.kt               — DataStore<Preferences>; stores JWT token
│   │   │   └── CashSessionStore.kt         — DataStore<Preferences>; stores active session id
│   │   ├── remote/
│   │   │   ├── AuthApi.kt                  — POST /auth/login
│   │   │   ├── CategoryApi.kt              — GET /categories
│   │   │   ├── EmployeeApi.kt              — GET /employees
│   │   │   ├── OrderDetailApi.kt           — POST/PUT/DELETE /orderdetails
│   │   │   ├── OverviewApi.kt              — GET /overview/phone/dashboard-start
│   │   │   ├── ProductsApi.kt              — GET /products, /overview/products-with-supplements-by-category/id/{id}
│   │   │   ├── interceptor/
│   │   │   │   └── AuthInterceptor.kt      — Reads JWT from TokenStore; adds Authorization header
│   │   │   └── adapter/
│   │   │       ├── LocalDateAdapter.kt     — Gson TypeAdapter for java.time.LocalDate
│   │   │       └── LocalDateTimeAdapter.kt — Gson TypeAdapter for java.time.LocalDateTime
│   │   └── repository/
│   │       └── AuthRepository.kt           — Login / logout; persists token to TokenStore
│   ├── domain/
│   │   └── model/
│   │       ├── DraftOrderDetailUi.kt       — UI model for an order detail being drafted
│   │       ├── request/
│   │       │   ├── LoginRequest.kt
│   │       │   └── OrderDetailRequest.kt
│   │       └── response/
│   │           ├── AuthResponse.kt
│   │           ├── CategoryResponse.kt
│   │           ├── DashboardStartResponse.kt
│   │           ├── EmployeeResponse.kt
│   │           ├── OrderDetailsResponse.kt
│   │           ├── OrderWithTableResponse.kt
│   │           ├── ProductResponse.kt
│   │           ├── RoleResponse.kt
│   │           └── ShiftRecordResponse.kt
│   └── ui/
│       ├── navigation/
│       │   └── AppNav.kt                   — NavHost; defines routes: login, home, orders, order_details/{orderId}/{tableId}, products/{orderId}/{tableId}
│       ├── screen/
│       │   ├── login/
│       │   │   ├── LoginScreen.kt
│       │   │   ├── LoginViewModel.kt
│       │   │   └── LoginState.kt
│       │   ├── home/
│       │   │   ├── HomeScreen.kt
│       │   │   ├── HomeViewModel.kt
│       │   │   └── HomeState.kt
│       │   ├── orders/
│       │   │   ├── OrdersScreen.kt         — ActiveOrdersScreen composable
│       │   │   ├── OrdersViewModel.kt
│       │   │   └── OrdersState.kt
│       │   ├── orderDetails/
│       │   │   ├── OrderDetailScreen.kt
│       │   │   ├── OrderDetailViewModel.kt
│       │   │   └── OrderDetailState.kt
│       │   └── products/
│       │       ├── ProductsScreen.kt
│       │       ├── ProductsViewModel.kt
│       │       └── ProductsState.kt
│       └── theme/
│           ├── Color.kt
│           ├── Theme.kt
│           └── Type.kt
└── res/
    └── [standard Android resource dirs]
```

**Navigation flow:** `login` → `home` → `orders` → `order_details/{orderId}/{tableId}` → `products/{orderId}/{tableId}`

**Key screens:** LoginScreen, HomeScreen, ActiveOrdersScreen (orders list), OrderDetailScreen (view/edit line items), ProductsScreen (add products to order)

---

## PC Frontend Structure

```
orderly/src/main/
├── java/
│   ├── module-info.java                    — Java module descriptor (requires javafx.*)
│   └── com/yebur/
│       ├── app/
│       │   └── App.java                    — JavaFX Application; loads portal.fxml; entry point
│       ├── controller/                     — JavaFX FXML controllers (MVC)
│       │   ├── PortalController.java       — Main shell; sidebar nav; lazy-loads sub-views
│       │   ├── StartController.java        — Landing; open/resume cash session
│       │   ├── DataController.java         — Master data view (products, tables, employees…)
│       │   ├── DataOperationController.java — CRUD dialog for master data entities
│       │   ├── PosController.java          — POS terminal; category/product tiles; order editing
│       │   ├── OperationsController.java   — POS sub-ops (discount, transfer, table split)
│       │   ├── JournalEntryController.java — Journal log inside POS
│       │   ├── ShiftOperationsController.java — Cash in/out during an open shift
│       │   ├── ShiftOperationCloseController.java — Close shift workflow
│       │   ├── CashCountModelController.java — Denomination cash count at shift close
│       │   ├── PartialPaymentController.java — Partial payment screen
│       │   └── ReceiptController.java      — Receipt preview + PDF trigger
│       ├── service/                        — HTTP service layer (wraps ApiClient)
│       │   ├── ApiClient.java              — Static HttpURLConnection wrapper (GET/POST/PUT/DELETE)
│       │   ├── CashCountService.java
│       │   ├── CashOperationService.java
│       │   ├── CashSessionService.java
│       │   ├── CategoryService.java
│       │   ├── OrderDetailService.java
│       │   ├── OrderService.java
│       │   ├── OverviewService.java
│       │   ├── ProductService.java
│       │   ├── ReceiptFxToPdfService.java  — Renders JavaFX scene to PDF (PDFBox)
│       │   ├── ReceiptPdfService.java      — Generates PDF receipt from order data (PDFBox)
│       │   ├── RestTableService.java
│       │   └── SupplementService.java
│       ├── model/
│       │   ├── request/
│       │   │   ├── CashCountRequest.java
│       │   │   ├── CashOperationRequest.java
│       │   │   ├── CashSessionRequest.java
│       │   │   ├── CategoryRequest.java
│       │   │   ├── OrderDetailRequest.java
│       │   │   ├── OrderRequest.java
│       │   │   ├── ProductRequest.java
│       │   │   ├── RestTableRequest.java
│       │   │   └── SupplementRequest.java
│       │   └── response/
│       │       ├── ApiException.java
│       │       ├── CashCountResponse.java
│       │       ├── CashOperationResponse.java
│       │       ├── CashSessionResponse.java
│       │       ├── CategoryResponse.java
│       │       ├── CategoryResponseSummary.java
│       │       ├── ErrorResponse.java
│       │       ├── OrderDetailResponse.java
│       │       ├── OrderResponse.java
│       │       ├── OrderSummary.java
│       │       ├── ProductResponse.java
│       │       ├── ProductResponseSummary.java
│       │       ├── ProductsWithSupplements.java
│       │       ├── RestTableResponse.java
│       │       ├── SupplementResponse.java
│       │       └── TableWithOrderResponse.java
│       └── ui/
│           └── CustomDialog.java           — Reusable modal dialog helper
└── resources/com/yebur/
    ├── portal/
    │   ├── portal.fxml                     — Main shell layout (sidebar + center pane)
    │   ├── portal.css
    │   └── views/
    │       ├── start.fxml                  — Landing / session open screen
    │       ├── data.fxml                   — Master data management
    │       ├── dataOperation.fxml          — CRUD operation dialogs
    │       ├── shiftOperations.fxml        — Cash operations view
    │       ├── shiftOperationClose.fxml    — Close shift screen
    │       └── cashCountModel.fxml         — Cash denomination count
    ├── pos/
    │   ├── pos.fxml                        — POS terminal layout
    │   ├── operations.fxml                 — POS operations panel
    │   └── journalEntry.fxml               — Journal/log panel
    ├── payment/
    │   └── payment.fxml                    — Partial payment screen
    ├── receipt/
    │   └── receipt-view.fxml               — Receipt preview layout
    └── icons/
        ├── icon.png                        — App window icon
        └── logo.png                        — Sidebar logo
```

**Key scenes:** Portal shell (always visible) → Start, Data, POS (opened as new Stage), Payment (modal), Receipt (preview Stage)

---

## Naming Conventions

**Backend (Java):**
- One package per domain, flat (no sub-packages except `auth/dto`, `security/jwt`)
- Classes: `{Domain}{Layer}.java` — `OrderController`, `OrderService`, `OrderRepository`, `Order` (entity has no suffix)
- Interface suffix: `{Domain}ServiceInterface`
- Enums: `{Domain}Status`, `{Domain}Type`, `{Domain}Position`

**Android (Kotlin):**
- Screen triad per feature: `{Feature}Screen.kt` + `{Feature}ViewModel.kt` + `{Feature}State.kt`
- Remote API interfaces: `{Domain}Api.kt`
- Response models: `{Domain}Response.kt`; request models: `{Domain}Request.kt`

**PC Frontend (Java):**
- Controllers named `{Scene}Controller.java` matching their FXML file
- Services named `{Domain}Service.java`, no interface
- Models segregated into `model/request/` and `model/response/`

---

## Where to Add New Code

**New backend domain (e.g., `reservation`):**
- Create package `backendorderly/reservation/`
- Add: `Reservation.java` (entity), `ReservationRepository.java`, `ReservationServiceInterface.java`, `ReservationService.java`, `ReservationController.java`, `ReservationRequest.java`, `ReservationResponse.java`

**New REST endpoint on existing domain:**
- Add method to existing `*Controller.java` and `*ServiceInterface.java` + `*Service.java`

**New Phone screen:**
- Add triad in `ui/screen/{feature}/`: `{Feature}Screen.kt`, `{Feature}ViewModel.kt`, `{Feature}State.kt`
- Add composable route in `ui/navigation/AppNav.kt`
- Add API interface in `data/remote/` if new backend endpoint needed
- Register API in `di/AppModule.kt`

**New PC view:**
- Create FXML in `resources/com/yebur/{area}/{name}.fxml`
- Create `{Name}Controller.java` in `controller/`
- Add service calls in `service/{Domain}Service.java` via `ApiClient`
- Wire navigation in `PortalController.java` (add `@FXML` button handler + `loadCenterContent(...)` call)

**New WebSocket event type:**
- Add constant to `WsEventType.java`
- Call `WsNotifier.send(new WsEvent(...))` from the relevant service
- Handle new event type in `OrdersViewModel.kt` (Tablet) `when` block
