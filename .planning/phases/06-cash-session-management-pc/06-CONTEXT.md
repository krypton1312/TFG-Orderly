# Phase 6: Cash Session Management (PC) — Context

**Gathered:** April 20, 2026
**Status:** Ready for planning

<domain>
## Phase Boundary

Complete the cash session open/close lifecycle on the PC:
- Fix `CashSessionService.create()` stub (backend, PC-05)
- Add missing `POST /cashSession/{id}/close` backend endpoint
- Wire the "Cerrar turno" submit button in `ShiftOperationCloseController`
- Show a statistics modal after closing a shift (PC-06)

The "open" flow (StartController → openCashSession) already works and is NOT in scope.
The "last shifts history with sales by product" section is Phase 7 (PC-07) — NOT in scope.
PDF export (both A4 and receipt) is deferred to v2 — NOT in scope.

</domain>

<decisions>
## Implementation Decisions

### D-01: CashSessionService.create() stub
Fix the stub to properly persist a CashSession entity from the given CashSessionRequest.
The `open()` method handles the auto-open flow; `create()` is a generic create-from-request
used by the interface contract. Implement it analogously to other create() methods in the
codebase (map request → entity → save → return DTO).

### D-02: Backend close endpoint
Add `POST /cashSession/{id}/close` to `CashSessionController`.
The existing `CashSessionService.close(Long id, CashCount cashCount)` should be called.
The request body carries the cashCount data so the controller builds a CashCount object
and delegates to the service.

### D-03: ShiftOperationCloseController — submit button
Add `onCerrarTurno()` handler (already has `@FXML` handler in FXML or needs button added).
Flow:
1. Validate that cashSession is not null (show error label if no open session)
2. Build the close request body: cashAmount (efectivo), cardAmount (tarjeta) from existing
   controller fields (these are already captured via single-click + modal)
3. Also pass `cashCountController` data if the modal was opened (denominations)
4. POST to `/cashSession/{id}/close`
5. On success: open the statistics modal

### D-04: Statistics screen after close — format
Shown as a **modal / floating window** (not inline panel replacement).
Pattern: same as `openCashCountModal()` in `ShiftOperationCloseController` — new Stage,
`initModality(APPLICATION_MODAL)`, `StageStyle.UNDECORATED`.

### D-05: Statistics screen content
A new FXML/controller: `shiftCloseReport.fxml` + `ShiftCloseReportController`.
Fields to display:
- Fecha (businessDate)
- Turno n°
- Apertura (openedAt), Cierre (closedAt)
- Fondo inicial (cashStart)
- Ventas efectivo (totalSalesCash)
- Ventas tarjeta (totalSalesCard)
- Diferencia (difference)
- Lista de operaciones IN/OUT del turno (from `GET /cashOperation/cashSession/id/{id}`)

### D-06: CashCountRequest for close endpoint
The `CashSessionService.close()` takes a `CashCount` entity. The controller endpoint
should accept a `CashCountRequest`-like body (or a dedicated `CashSessionCloseRequest`)
containing: cashEndActual (total counted cash), optional denomination map.
Agent's discretion on whether to reuse `CashCountRequest` directly or create a thin wrapper.

### D-07: PDF export
Deferred to v2. Not in Phase 6 scope.

### D-08: "Últimos turnos / estadísticas históricas"
Deferred to Phase 7 (PC-07 — Order history / statistics screen).

### Agent's Discretion
- Exact HTTP status codes for close endpoint (201 or 200)
- Whether CashCount entity is created in DB during close or just used for calculations
  (check what `CashSessionService.close()` currently does with the CashCount param)
- Styling of `shiftCloseReport.fxml` — follow existing `cashCountModel.css` card style
- Whether to add a "Cerrar" button in `shiftOperations.fxml` directly or load via
  `ShiftOperationsController.showShiftOperationView()`

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Backend — Cash Session
- `backendOrderly/src/main/java/com/yebur/backendorderly/cashsessions/CashSessionService.java` — `open()`, `close()`, `create()` stub
- `backendOrderly/src/main/java/com/yebur/backendorderly/cashsessions/CashSessionController.java` — existing endpoints; add `/close` here
- `backendOrderly/src/main/java/com/yebur/backendorderly/cashsessions/CashSessionServiceInterface.java` — interface contract
- `backendOrderly/src/main/java/com/yebur/backendorderly/cashcount/CashCountRequest.java` — input model for cash count
- `backendOrderly/src/main/java/com/yebur/backendorderly/cashcount/CashCount.java` — entity passed to close()

### PC Frontend — Close flow
- `frontend/orderly/src/main/java/com/yebur/controller/ShiftOperationCloseController.java` — UI already built; add submit handler
- `frontend/orderly/src/main/java/com/yebur/controller/ShiftOperationsController.java` — parent panel with `closeShiftVBox` routing
- `frontend/orderly/src/main/java/com/yebur/controller/CashCountModelController.java` — modal that returns denomination total
- `frontend/orderly/src/main/java/com/yebur/service/CashSessionService.java` — frontend HTTP client; add `closeCashSession()`

### PC Frontend — Data layer
- `frontend/orderly/src/main/java/com/yebur/model/response/CashSessionResponse.java` — DTO with all session fields
- `frontend/orderly/src/main/java/com/yebur/service/CashOperationService.java` — `GET /cashOperation/cashSession/id/{id}` for operations list
- `frontend/orderly/src/main/java/com/yebur/model/response/CashOperationResponse.java` — list item for operations table

### PC Frontend — Patterns to follow
- `frontend/orderly/src/main/java/com/yebur/controller/CashCountModelController.java` — reference for modal opening pattern (Stage/Modality)
- `frontend/orderly/src/main/resources/com/yebur/portal/views/cashCountModel.fxml` — reference FXML style for modal
- `frontend/orderly/src/main/resources/com/yebur/portal/views/cashCountModel.css` — CSS to extend for report modal

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `CashCountModelController` — existing modal pattern; `ShiftCloseReportController` should follow the same Stage/Modality/StageStyle.UNDECORATED pattern
- `CashSessionService` (frontend) — already has `openCashSession()`, `existsByStatus()`, `findCashSessionByStatus()`, `updateCashSession()` — add `closeCashSession(Long id, ...)` alongside
- `CashOperationService` (frontend) — already has `getOperationsByCashSessionId()` or equivalent endpoint hit
- `ShiftOperationCloseController` — has `cashAmount`, `cardAmount` fields and `cashCountController` reference ready to pass to close

### Established Patterns
- Backend controller error handling: `try/catch → ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()))` — use same pattern in close endpoint
- Frontend service: static methods, `ApiClient.post()/put()/get()`, ObjectMapper with JavaTimeModule — match `EmployeeService` / `CashSessionService` style
- Modal stage: `new Stage(); stage.initStyle(UNDECORATED); stage.initModality(APPLICATION_MODAL); stage.initOwner(...)` — use this in ShiftOperationCloseController for statistics window

### Integration Points
- `ShiftOperationCloseController` → add `@FXML private void onCerrarTurno()` → builds request → calls `CashSessionService.closeCashSession(id, ...)` → on success opens report modal
- `CashSessionController` → add `@PostMapping("/{id}/close")` → delegates to `cashSessionService.close(id, cashCount)`
- `ShiftCloseReportController` → receives `CashSessionResponse` from caller, calls `CashOperationService.getOperationsByCashSessionId(id)` for operations list

</code_context>

<specifics>
## Specific Ideas

- Statistics screen is a **modal window** (not inline panel swap), same pattern as `cashCountModel.fxml`
- Report content: fecha, turno n°, apertura/cierre timestamps, fondo inicial, ventas efectivo, ventas tarjeta, diferencia + table of IN/OUT operations
- PDF export (both A4 and thermal receipt size, config-driven) → deferred to v2

</specifics>

<deferred>
## Deferred Ideas

- **PDF export** — both A4 and 80mm receipt format, configurable — v2 (REC-01, REC-03)
- **Email PDF report** — send shift PDF to email address — v2 (REC-02)
- **Últimos turnos / estadísticas históricas** (ventas por producto, turnos anteriores) — Phase 7 (PC-07)
- **Configurable report settings** — print destination, paper size, email address — v2

</deferred>

---

*Phase: 06-cash-session-management-pc*
*Context gathered: April 20, 2026*
