# Requirements: Orderly

**Defined:** April 12, 2026
**Core Value:** Waiter logs in on phone, takes order, tablet shows it, PC closes the bill — full cycle works on real devices with no paper.

## v1 Requirements (TFG — end of May 2026)

### Security & Authentication

- [x] **SEC-01**: JWT authentication is enforced on all backend endpoints (Spring Security filter active)
- [x] **SEC-02**: Employee can log in on phone with username/password, JWT stored in DataStore
- [x] **SEC-03**: Employee can log in on PC with bar credentials
- [x] **SEC-04**: Tablet requires login with kitchen/bar credentials; JWT sent in Authorization header during WebSocket handshake and Retrofit calls
- [x] **SEC-05**: JWT secret and DB password sourced from environment variables (not hardcoded)
- [x] **SEC-06**: Dual-token JWT — short-lived access token (60 min) + long-lived refresh token (30 days); PC auto-restores session on restart without re-login

### Infrastructure

- [x] **INF-01**: Phone app connects to real Android devices (base URL configurable, not hardcoded to emulator IP)
- [x] **INF-02**: HTTPS or documented cleartext exception for local network deployment
- [x] **INF-03**: `PUT /cashSession` returns `200 OK` (not `201 Created`)

### Phone App — Waiter Flow

- [ ] **PHN-01**: Waiter selects a table from the floor plan / table list
- [ ] **PHN-02**: Waiter creates a new order for a table or opens an existing one
- [ ] **PHN-03**: Waiter browses menu by category and adds products with optional comment
- [ ] **PHN-04**: Waiter sends the order to the kitchen (orderDetails POSTed with status=SENT, WebSocket fires to tablet)
- [ ] **PHN-05**: Waiter can view existing order details for a table (item list with status badges)
- [ ] **PHN-06**: Waiter sees real-time status updates on their order (SENT → IN_PROGRESS → SERVED)
- [ ] **PHN-07**: Stub screens (onNewOrder, onShiftToggle, onSettings) are implemented or removed

### PC — TPV & Management

- [ ] **PC-01**: PC shows table overview (open/closed status) with real-time updates
- [ ] **PC-02**: Employee can open a table and add products from PC (same as phone flow)
- [ ] **PC-03**: Partial payment by product line works correctly (PartialPaymentController persists split)
- [ ] **PC-04**: Employee CRUD — create, archive, change PIN/password
- [ ] **PC-05**: Cash session open/close implemented (CashSessionService.create() stub completed)
- [ ] **PC-06**: Cash session shift report (totals by payment method)
- [ ] **PC-07**: Order history / statistics screen (orders by session, daily totals)

### Tablet

- [ ] **TAB-01**: WebSocket reconnects automatically on disconnect (no manual refresh needed)
- [ ] **TAB-02**: Network errors shown in UI (not silent crash)

### Real-time & Reliability

- [ ] **RT-01**: Phone receives real-time order status updates (WebSocket or SSE)
- [ ] **RT-02**: Android apps show meaningful error states on network failure

## v2 Requirements (Post-TFG)

### Receipts

- **REC-01**: QR code receipt for customer (scan to view digital bill)
- **REC-02**: Email receipt via PDF attachment
- **REC-03**: Direct print to thermal printer via USB/network

### Supplier Management

- **SUP-01**: Albarán / supplier invoice generation
- **SUP-02**: Low-stock products flagged, auto-generate supplier order

### Inventory

- **INV-01**: Stock quantity decremented when order detail is PAID
- **INV-02**: Low-stock alerts on PC dashboard

### Multi-tenancy

- **MT-01**: Single server handles multiple bars (bar-level data isolation)
- **MT-02**: Bar login screen selects which bar, separate JWT context

### Platform

- **PLT-01**: iOS version of phone app (requires KMM or separate project)
- **PLT-02**: iOS version of tablet app

## Out of Scope (TFG)

| Feature | Reason |
|---------|--------|
| iOS apps | Android Kotlin only for TFG; iOS is post-graduation |
| Multi-language UI | Interface in Spanish only, no i18n needed |
| Online payment / TPV hardware | Cash + card tracking is sufficient |
| RBAC (role-based endpoint restrictions) | Authentication only for TFG; RBAC is v2 |
| Stock/inventory decrement | Field exists, feature deferred post-TFG |
| Multi-tenancy | Single-bar deployment for TFG demo |
| Albaranes / supplier invoices | Major future feature, not related to TFG demo |

## Traceability

| Requirement | Phase | Status |
|-------------|-------|--------|
| SEC-01..06 | Phase 1 | Complete |
| INF-01..03 | Phase 2 | Complete |
| PHN-01..05 | Phase 3 | Pending |
| PHN-06, RT-01 | Phase 4 | Pending |
| PHN-07, RT-02, TAB-01..02 | Phase 4 | Pending |
| PC-04 | Phase 5 | Pending |
| PC-05..06 | Phase 6 | Pending |
| PC-01..02 | Phase 7 | Pending |
| PC-03 | Phase 8 | Pending |
| PC-07 | Phase 9 | Pending |

**Coverage:**
- v1 requirements: 23 total
- Mapped to phases: 23
- Unmapped: 0

---
*Requirements defined: April 12, 2026*
*Last updated: April 12, 2026 — Phase 1 and Phase 2 marked complete; SEC-06 added (dual-token JWT for PC session persistence); SEC-04 updated (tablet also requires login)*
