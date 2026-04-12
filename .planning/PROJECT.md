# Orderly

## What This Is

Orderly is a full-stack bar management system that eliminates all paper-based workflows from bar operations. Waiters take orders on Android phones, kitchen/bar staff see and close orders on a tablet, and employees process payments and manage the business from a PC that acts as the main TPV (point-of-sale terminal). Every order lives in the system from creation to payment — nothing is written on paper.

## Core Value

A waiter takes an order on the phone, the kitchen/bar sees it instantly on the tablet, and the PC closes the bill accurately — the full cycle works end-to-end on real devices with no paper.

## Requirements

### Validated

- ✓ Backend REST API (Spring Boot 3.5.5 / Java 17 / PostgreSQL) — existing
- ✓ JWT authentication infrastructure (JJWT, AuthInterceptor, TokenStore) — existing, not enforced yet
- ✓ Order domain model (Order, OrderDetail, status lifecycle: PENDING → SENT → IN_PROGRESS → SERVED → PAID) — existing
- ✓ OrderDetail partial payment logic (split by product/amount) — existing (PartialPaymentController, PC)
- ✓ Cash session management (open/close shift, CashCount denomination count) — existing
- ✓ CashOperation tracking (IN/OUT entries within session) — existing
- ✓ PDF receipt generation (58mm thermal format, PDFBox) — existing (ReceiptPdfService)
- ✓ Product & category CRUD — existing (backend + PC)
- ✓ Supplement/modifier support (applied to order details) — existing
- ✓ Table management (RestTable entity, open/close lifecycle) — existing
- ✓ WebSocket broadcast to tablet (order created/updated/detail changed) — existing
- ✓ Tablet display — shows active orders live, allows marking SERVED/IN_PROGRESS — existing, stable
- ✓ Phone: login, view orders by table, add products, adjust quantities — existing (partial, needs real-device fix)
- ✓ Overview read-model layer (aggregated projections for tablet + PC dashboard) — existing
- ✓ PC lazy-loaded SPA navigation (PortalController node cache) — existing

### Active

**Authentication & Security**
- [ ] JWT enforcement: enable Spring Security filter (remove `anyRequest().permitAll()`)
- [ ] Per-employee login on phone (JWT stored in DataStore, sent on all requests)
- [ ] Per-employee login on PC (bar-level login; role-aware menus for manager vs cashier)
- [ ] Tablet public access preserved (no token required for `/ws/overview/tablet`)

**Phone App — Waiter Flow (MVP)**
- [ ] Fix hardcoded IP — read base URL from config/BuildConfig so it works on real devices
- [ ] Login screen → token persisted in DataStore
- [ ] Table selection screen (open tables visible)
- [ ] Create order or open existing order for a table
- [ ] Browse menu by category → add products with optional comment
- [ ] Send order to kitchen (POST orderDetails, status=SENT, WebSocket fires to tablet)
- [ ] View existing order details for a table (list items, status badges)
- [ ] Real-time status updates on phone (WebSocket or polling) — waiter sees SERVED items
- [ ] Implement stub screens: onNewOrder, onShiftToggle, onSettings

**PC — TPV & Management**
- [ ] Employee CRUD (create, archive, change PIN/password)
- [ ] Cash session open/close with denomination count UI (CashSessionService.create() stub → implement)
- [ ] Full order management from PC: open table, add products, modify order (same flow as phone)
- [ ] Partial payment improvement: persist split correctly per product line
- [ ] History/statistics screen (orders by session, totals by payment method)

**Real-time & Reliability**
- [ ] WebSocket reconnection logic on tablet (currently no retry on disconnect)
- [ ] Error states on Android: network failure shown in UI (not silent crash)

**Infrastructure**
- [ ] Replace hardcoded DB password and JWT secret with environment variables / `.env`
- [ ] Disable `usesCleartextTraffic` — use HTTPS (or document dev-only exception)
- [ ] Fix `PUT /cashSession` returning `201 Created` → should return `200 OK`

### Out of Scope (TFG deadline — end of May 2026)

- iOS version — Android only for now; iOS is post-TFG
- Supplier invoices / albaranes — planned future feature, not required for TFG
- Stock/inventory decrement — field exists but not decremented; deferred post-TFG
- Multi-language UI — interface is Spanish only; no i18n needed
- Multi-tenancy / multi-bar server — architecture is noted for future; single-bar for TFG
- Online payment / TPV hardware integration — cash + card tracking only
- QR code receipt / email receipt — nice to have post-TFG (PDF print is enough for TFG)
- Role-based access control (RBAC) — authentication only; no per-role endpoint restrictions for TFG

## Context

- **Academic project (TFG)** — Final degree project. Deadline: end of May 2026 (~6 weeks). Must be demonstrable to a professor. External appearance matters.
- **4-component architecture** — Spring Boot backend, JavaFX PC app (TPV + management), Android phone app (waiter), Android tablet app (kitchen display). All communicate via REST + WebSocket.
- **Brownfield codebase** — PC and tablet are largely functional. Phone is minimal. Backend has most logic complete but security is disabled.
- **Real deployment target** — Post-TFG the author plans to deploy at the bar where he works, then potentially sell to other bars. Architecture decisions should not block this.
- **Language conventions** — Code in English, UI text in Spanish.
- **The PC is both TPV and management console** — Not just for managers. All staff use it for payments; managers use extra screens (employees, sessions, statistics).

## Constraints

- **Timeline**: End of May 2026 — ~6 weeks for everything remaining
- **Platform**: Android only (phone + tablet); JavaFX desktop (PC); Spring Boot backend
- **Database**: PostgreSQL — schema changes must be backward-compatible
- **Real devices**: Must run on physical Android device + physical tablet, not just emulator (fix hardcoded `10.0.2.2`)
- **TFG demo**: Must be demonstrable in a single Wi-Fi network (local deploy acceptable for submission)
- **No iOS**: Kotlin/Compose Android only. iOS is explicitly out of scope.

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| JWT auth enabled for TFG | Academic project should demonstrate proper security even if no RBAC yet | — Pending |
| Phone real-device support | TFG demo requires physical device; emulator IP breaks this | — Pending |
| Tablet stays public (no auth) | Tablet is a display terminal in the bar, not a user-facing device | — Pending |
| Local deployment for TFG | Single Wi-Fi network is sufficient for demo; cloud is post-TFG | — Pending |
| Spanish UI throughout | Bar staff in Spain; no need for i18n | ✓ Good |
| PDF receipt via PDFBox | Already implemented; sufficient for TFG demo | ✓ Good |
| Partial payment by product line | Critical for "large table splits" problem — the main pain point solved | ✓ Good |

## Evolution

This document evolves at phase transitions and milestone boundaries.

**After each phase transition** (via `/gsd-transition`):
1. Requirements invalidated? → Move to Out of Scope with reason
2. Requirements validated? → Move to Validated with phase reference
3. New requirements emerged? → Add to Active
4. Decisions to log? → Add to Key Decisions
5. "What This Is" still accurate? → Update if drifted

**After each milestone** (via `/gsd-complete-milestone`):
1. Full review of all sections
2. Core Value check — still the right priority?
3. Audit Out of Scope — reasons still valid?
4. Update Context with current state

---
*Last updated: April 12, 2026 after initialization*
