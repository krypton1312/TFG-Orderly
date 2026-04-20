# Phase 6: Cash Session Management (PC) — Discussion Log

> **Audit trail only.** Not consumed by planning/execution agents.
> Decisions are captured in 06-CONTEXT.md.

**Date:** 2026-04-20
**Phase:** 06 — Cash Session Management (PC)
**Areas discussed:** Shift report display, Report data, PDF export

---

## Shift report display

| Option | Description | Selected |
|--------|-------------|----------|
| Inline in ShiftOperationCloseController | Panel replaces calculator with stats | |
| Separate "Ver informe" card | User can view last report without closing | |
| **Modal / floating window** | New Stage opened on close success | ✓ |

**User's choice:** Modal / floating window after clicking "Cerrar turno"
**Notes:** User also wants a separate history section (last shifts, sales by product) — deferred to Phase 7 (PC-07)

---

## Report data

| Option | Description | Selected |
|--------|-------------|----------|
| Totals only | Fecha, turno, sales cash/card, difference | |
| **Totals + IN/OUT operations** | All totals + list of operations for the shift | ✓ |

**User's choice:** Totals + operations list
**Notes:** No additional order-level stats — those go to Phase 7

---

## PDF export

| Option | Description | Selected |
|--------|-------------|----------|
| A4 formal report | Document-style PDF | |
| Ticket 80mm | Thermal printer receipt | |
| **Both, config-driven (post-TFG)** | Configurable paper size + destination | ✓ |

**User's choice:** Deferred to v2 — both formats with configuration (email or print)
**Notes:** PDFBox already in project; full implementation requires email + print config

---

## Agent's Discretion

- Exact HTTP status code for close endpoint
- Whether to reuse `CashCountRequest` or create a `CashSessionCloseRequest` wrapper
- Styling of the statistics modal (follow `cashCountModel.css` card style)
- Denomination data inclusion in close request

## Deferred Ideas

- PDF export (A4 + thermal, config-driven) — v2
- Email PDF report — v2
- Últimos turnos / estadísticas históricas (ventas por producto) — Phase 7
- Configurable print/email settings — v2
