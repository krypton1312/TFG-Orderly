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

**Goal:** Complete the waiter's primary workflow on the phone: log in, select a table, create/open an order, browse menu by category, add products with comments, send to kitchen. Works on a real Android device.

**Requirements:** PHN-01, PHN-02, PHN-03, PHN-04, PHN-05

**Plans:** TBD

**Depends on:** Phase 1, Phase 2

---

### Phase 4: Phone Real-Time & Polish

**Goal:** Waiter sees live order status updates on phone (SENT → IN_PROGRESS → SERVED). Implement or remove stub screens. Android apps show meaningful error states on network failure. Tablet reconnects automatically on WebSocket disconnect.

**Requirements:** PHN-06, PHN-07, RT-01, RT-02, TAB-01, TAB-02

**Plans:** TBD

**Depends on:** Phase 3

---

### Phase 5: Employee Management (PC)

**Goal:** Managers can create, archive, and change PIN/password for employees from the PC admin screen.

**Requirements:** PC-04

**Plans:** TBD

**Depends on:** Phase 1

---

### Phase 6: Cash Session Management (PC)

**Goal:** Implement the cash session open/close flow completely — including the `CashSessionService.create()` stub. Produce shift reports (total sales by payment method).

**Requirements:** PC-05, PC-06

**Plans:** TBD

**Depends on:** Phase 1, Phase 5

---

### Phase 7: Full Order Management from PC

**Goal:** PC can open a table, add products (same flow as phone), and manage existing orders. Table overview shows real-time open/closed status.

**Requirements:** PC-01, PC-02

**Plans:** TBD

**Depends on:** Phase 3, Phase 6

---

### Phase 8: Partial Payment Polish (PC)

**Goal:** Improve PartialPaymentController so split-by-product-line correctly persists to the database. Every split generates the right DB records and status transitions.

**Requirements:** PC-03

**Plans:** TBD

**Depends on:** Phase 7

---

### Phase 9: History & Statistics (PC)

**Goal:** PC history/statistics screen shows orders per session, daily revenue totals, and breakdown by payment method.

**Requirements:** PC-07

**Plans:** TBD

**Depends on:** Phase 6, Phase 7

---

### Phase 10: Integration Polish & TFG Demo Prep

**Goal:** End-to-end smoke test on real devices. Fix any remaining integration gaps. Ensure the demo scenario (waiter → phone → tablet → PC bill) works flawlessly. Final cleanup.

**Requirements:** SEC-01, PHN-04, TAB-01, RT-01

**Plans:** TBD

**Depends on:** Phase 4, Phase 8, Phase 9

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
*Last updated: April 12, 2026 after initialization*
