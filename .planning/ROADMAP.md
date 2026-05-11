# Orderly — Roadmap

**Milestone:** v1.0 — TFG Submission
**Deadline:** End of May 2026 (~6 weeks)
**Granularity:** Fine
**Status:** Planning

---

## Milestone 1: TFG-Ready Bar Management System

**Goal:** A fully connected bar management system that works end-to-end on real devices: waiters take orders on Android phones, the kitchen sees them on the tablet, and the PC closes bills accurately. No paper. No hardcoded credentials. JWT enforced.

**Requirements:** SEC-01, SEC-02, SEC-03, SEC-04, SEC-05, INF-01, INF-02, INF-03, PHN-01, PHN-02, PHN-03, PHN-04, PHN-05, PHN-06, PHN-07, PC-01, PC-02, PC-03, PC-04, PC-05, PC-06, PC-07, TAB-01, TAB-02, RT-01, RT-02

---

### Phase 1: Security Foundation

**Goal:** Enable JWT authentication across the system — activate Spring Security filter, secure all endpoints, keep the tablet public. No more `anyRequest().permitAll()`.

**Requirements:** SEC-01, SEC-02, SEC-03, SEC-04, SEC-05, SEC-06

**Plans:** 5 plans
- [x] 01-01-PLAN.md — Backend JWT activation + env config
- [x] 01-02-PLAN.md — Dual-token JWT (access + refresh endpoints)
- [x] 01-03-PLAN.md — PC login screen + session auto-restore
- [x] 01-04-PLAN.md — End-to-end verification checkpoint
- [x] 01-05-PLAN.md — Tablet login screen + auth wiring (Retrofit + WebSocket)

**Depends on:** —

---

### Phase 2: Infrastructure & Real-Device Compatibility

**Goal:** Fix all hardcoded configuration so the system runs on real Android devices in a local bar network. Move secrets to environment variables. Fix incorrect HTTP status codes.

**Requirements:** INF-01, INF-02, INF-03

**Plans:** 2 plans
- [x] 02-01-PLAN.md — Phone configurable BASE_URL via BuildConfig
- [x] 02-02-PLAN.md — Network security config (debug-only cleartext) + INF-03 verify

**Depends on:** Phase 1

---

### Phase 3: Phone App — Core Waiter Flow

**Goal:** Complete the waiter's primary workflow on the phone: log in, select a table, create/open an order, browse menu by category, configure each item before adding it to the order (quantity, comment, supplements), review draft vs sent items, and send to kitchen. Works on a real Android device.

**Requirements:** PHN-01, PHN-02, PHN-03, PHN-04, PHN-05

**Plans:** 4 plans

Plans:
- [x] 03-01-PLAN.md — Contract alignment for table/open-order flow
- [x] 03-02-PLAN.md — Shared table picker screen + Home/Orders navigation wiring
- [x] 03-03-PLAN.md — Category catalog + full-screen product configurator
- [x] 03-04-PLAN.md — Draft review, fire-to-kitchen, and real-device verification

**Depends on:** Phase 1, Phase 2

---

### Phase 4: Phone Real-Time & Polish

**Goal:** Waiter sees live order status updates on phone (SENT → IN_PROGRESS → SERVED). Implement or remove stub screens. Android apps show meaningful error states on network failure. Tablet reconnects automatically on WebSocket disconnect.

**Requirements:** PHN-06, PHN-07, RT-01, RT-02, TAB-01, TAB-02

**Plans:** 4 plans

Plans:
- [x] 04-01-PLAN.md — Backend: register /ws/orders/phone endpoint + Security permitAll /ws/**
- [x] 04-02-PLAN.md — Phone: error states UI with WifiOff icon + Reintentar button on all 3 screens
- [x] 04-03-PLAN.md — Tablet: WebSocket reconnect with exponential backoff + URL from BASE_URL
- [x] 04-04-PLAN.md — Phone: OrderWebSocketClient singleton (SharedFlow) + VM wiring + AppNav connect/disconnect

**Depends on:** Phase 3

---

### Phase 5: Employee Management (PC)

**Goal:** Managers can create, archive, and change PIN/password for employees from the PC admin screen.

**Requirements:** PC-04

**Plans:** 3 plans

Plans:
- [x] 05-01-PLAN.md — EmployeeResponse, RoleResponse, EmployeeRequest DTOs + EmployeeService + RoleService
- [x] 05-02-PLAN.md — Empleados card in data.fxml + DataController routing + EntityType.EMPLOYEE wiring
- [x] 05-03-PLAN.md — Employee ADD/EDIT/DELETE forms (DatePicker, role ToggleButtons, status ComboBox, soft delete, temp password dialog)

**Depends on:** Phase 1

---

### Phase 6: Cash Session Management (PC)

**Goal:** Implement the cash session open/close flow completely — close endpoint, CashCount persistence, and shift report modal. Produce shift reports (total sales by payment method).

**Requirements:** PC-05, PC-06

**Plans:** 3 plans

Plans:
- [x] 06-01-PLAN.md — Backend: CashSessionServiceCloseTest + close() CashCount persistence fix + POST /cashSession/{id}/close endpoint
- [x] 06-02-PLAN.md — Report modal: shiftCloseReport.css + shiftCloseReport.fxml + ShiftCloseReportController
- [x] 06-03-PLAN.md — Frontend wiring: getDenomCounts() getter + closeCashSession() service method + onCerrarTurno() handler + FXML button

**Depends on:** Phase 1, Phase 5

---

### Phase 7: Full Order Management from PC ✅

**Goal:** PC can open a table, add products (same flow as phone), and manage existing orders. Table overview shows real-time open/closed status.

**Requirements:** PC-01, PC-02

**Plans:** 07-01-PLAN.md (BUG-01/02/04/05, GAP-04), 07-02-PLAN.md (BUG-03, GAP-03), 07-03-PLAN.md (GAP-02 WebSocket)

**Depends on:** Phase 3, Phase 6

**Status:** COMPLETE — 3 atomic commits (73fda67, f1ae2b5, 37e29dd)

---

### Phase 8: Payment Integrity & PC Order Status Polish

**Goal:** Verify the entire payment flow end-to-end so nothing is lost. Fix partial payment split-by-product-line persistence. Improve PC ordered-item cards with clear status-based visual styling (paid, sent, in-progress, served, cancelled). Paid-but-not-yet-served products must remain visible in the PC overview with a distinct "PAGADO" indicator so staff never lose track of them.

**Requirements:** PC-03

**Plans:** 5 plans
- [ ] 08-01-PLAN.md — Backend models: existsByOrderIdAndStatus + OrderSummary.hasPaidItems
- [ ] 08-02-PLAN.md — Frontend CSS + model: border accents, status pills, parcial badge, OrderSummary.hasPaidItems
- [ ] 08-03-PLAN.md — OverviewService: inject OrderDetailRepository + compute hasPaidItems
- [ ] 08-04-PLAN.md — PC Controllers: PosController full refactor + PartialPaymentController integrity fix
- [ ] 08-05-PLAN.md — Smoke test: 5-scenario end-to-end payment verification

**Depends on:** Phase 7

---

### Phase 9: Analytics & Listados (PC)

**Goal:** PC Inicio screen shows a full analytics dashboard (revenue totals, covers, top-selling products, breakdown by payment method) and a quick partial-stats summary. A dedicated Listados section lets managers browse all orders with date/session/status filters and inspect order history in full.

**Requirements:** PC-07

**Plans:** 5 plans

Plans:
- [x] 09-01-PLAN.md — Backend analytics endpoint (GET /analytics/monthly-summary) + GET /clients + Wave 0 test stubs
- [x] 09-02-PLAN.md — Frontend analytics: start.fxml stats VBox + StartController month nav + analytics.fxml + AnalyticsController
- [x] 09-03-PLAN.md — Portal wiring: Analisis button + new Listados button in portal.fxml + PortalController handlers
- [x] 09-04-PLAN.md — Listados launcher (8 cards) + turnosList + mesasList + categoriasList + pagosList modals
- [x] 09-05-PLAN.md — Listados detail modals: pedidoDetail (shared) + pedidosList + clientesList/Detail + productosList/Detail + empleadosList/Detail

**Depends on:** Phase 6, Phase 7

---

### Phase 10: Shift Integrity & Open Session Checks

**Goal:** Enforce that no work can happen without an active cash session. Tablet shows a clear "no hay turno abierto" screen when no session exists. Phone blocks new order creation and displays an informative message. Add the ability to reopen a closed shift; prevent reopening if another session is already open. Link all employee fichajes to the active cash session they belong to.

**Plans:** TBD

**Depends on:** Phase 6

---

### Phase 11: Employee Fichajes Management (PC)

**Goal:** Managers can edit and correct employee fichajes (clock-in / clock-out records) from the PC. Every modification is recorded in the DB change-history table that already exists. Fichajes are displayed and filtered per cash session/shift.

**Plans:** TBD

**Depends on:** Phase 10

---

### Phase 12: App Configuration

**Goal:** PC gets a dedicated Settings panel where the admin can choose the theme (light/dark), configure the SMTP sender credentials (host, port, user, password) and a receiver e-mail address for shift reports, and set up the printer. Phone settings screen gains a colour/theme selector. All configuration is persisted in the database.

**Plans:** 6 plans

Plans:
- [ ] 12-01-PLAN.md — Backend entity layer + AES encryption + test stubs
- [ ] 12-02-PLAN.md — AppConfigService + AppConfigController + tests
- [ ] 12-03-PLAN.md — PC ConfigService + PortalController navigation wiring
- [ ] 12-04-PLAN.md — PC configuracion.fxml + ConfiguracionController + CSS
- [ ] 12-05-PLAN.md — PC dark theme (portal-dark.css)
- [ ] 12-06-PLAN.md — Phone ThemePreferenceStore + SettingsScreen + wiring

**Depends on:** Phase 5

---

### Phase 13: Shift Close Report — Print & Email

**Goal:** Complete the two stub buttons in ShiftCloseReportController. "IMPRIMIR" generates a PDF of the shift report and sends it to the configured printer via JavaFX PrinterJob. "ENVIAR" generates an A4 PDF and delivers it via SMTP to the receiver address stored in the app configuration (Phase 12). Apache PDFBox (already in stack) handles PDF generation.

**Plans:** TBD

**Depends on:** Phase 10, Phase 12

---

### Phase 14: Customer Management

**Goal:** Managers and waiters can add, edit, and look up customers — from both the PC and the phone. A waiter can link an open table/order to a customer. When a customer is linked, the app surfaces a short list of personalised product recommendations derived from their order history.

**Plans:** TBD

**Depends on:** Phase 3, Phase 7

---

### Phase 15: Receipt Printing

**Goal:** Staff can print an order receipt (ticket) from the PC for any closed/paid order. Uses the printer configured in Phase 12. Receipt layout follows a standard ticket format (products, quantities, total, payment method).

**Plans:** TBD

**Depends on:** Phase 8, Phase 12

---

### Phase 16: UI/UX Polish & Bug Fixes

**Goal:** A focused sweep of known visual and UX bugs across all three frontends.

Scope:
- **App icons** — set meaningful launcher icons for the phone and tablet Android apps.
- **Employee edit scroll** — add a ScrollPane to the main employee-edit layout to prevent overflow when screen space is tight.
- **Phone password-change colours** — fix black validation-error text inside input boxes; align error colours to the dark/orange palette.
- **Phone settings** — add colour/theme selector to the phone settings screen.
- **Photo fallback** — replace blank avatar with a proper default icon everywhere; add the option to upload/change a profile photo from the phone.
- **Tablet order status translations** — translate all order status labels to Spanish.
- **Tablet kitchen-role logic** — when the logged-in tablet user has the kitchen role, show only orders with status SENT (orders arriving from waiters); other roles retain the current view.
- **Order card redesign** — redesign order cards on both tablet and phone for better readability and hierarchy.

**Plans:** TBD

**Depends on:** Phase 3, Phase 4, Phase 5, Phase 10

---

### Phase 17: DB Cascade & Final Integration Audit

**Goal:** Audit every JPA entity for correct cascade types, orphanRemoval settings, fetch strategies, FK constraints, indexes, and nullable columns. Run a full end-to-end smoke test on real devices covering the complete demo scenario (waiter → phone → tablet → PC → bill → shift close). Fix any final gaps. TFG-ready.

**Plans:** TBD

**Depends on:** Phase 8, Phase 9, Phase 10, Phase 11, Phase 12, Phase 13, Phase 14, Phase 15, Phase 16

---

## Backlog (Post-TFG)

### 999.1 QR Code Receipt
- QR code customer receipt — post-graduation feature

### 999.2 Supplier Invoices (Albaranes)
- Albarán & supplier invoice generation — post-graduation feature

### 999.3 iOS Apps
- iOS versions of phone and tablet apps

### 999.4 Multi-tenancy
- Single server for multiple bars with data isolation

### 999.5 Stock Management
- Inventory decrement on payment, low-stock alerts

---
*Roadmap created: April 12, 2026*
*Last updated: May 10, 2026 — Phase 9 planned (5 plans, 4 waves)*
