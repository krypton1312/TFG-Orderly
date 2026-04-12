---
gsd_state_version: 1.0
milestone: v1.0
milestone_name: milestone
current_phase: 03
status: Executing Phase 03
stopped_at: Phase 3 UI-SPEC approved
last_updated: "2026-04-12T17:52:21.184Z"
progress:
  total_phases: 10
  completed_phases: 2
  total_plans: 11
  completed_plans: 7
  percent: 64
---

# Project State

**Project:** Orderly — Bar Management System
**Milestone:** v1.0 — TFG Submission
**Current Phase:** 03
**Last Updated:** April 12, 2026
**Last session:** 2026-04-12T16:54:35.851Z
**Last Date:** 2026-04-12T16:54:35.851Z
**Stopped At:** Phase 3 UI-SPEC approved
**Resume File:** .planning/phases/03-phone-app-core-waiter-flow/03-UI-SPEC.md

## Position

- **Current phase:** 3 — Phone App - Core Waiter Flow
- **Phases complete:** 2 of 10
- **Milestone progress:** ~20%
- **Deadline:** End of May 2026

## Active Decisions

| Decision | Made | Rationale |
|----------|------|-----------|
| JWT enforced for TFG | April 12, 2026 | Academic project must demonstrate security |
| Android-only (no iOS) | April 12, 2026 | Kotlin/Compose; iOS is post-TFG |
| Tablet stays public (no auth) | April 12, 2026 | Kitchen display terminal, not user-facing |
| Local Wi-Fi deployment for TFG | April 12, 2026 | Single-bar demo, no cloud required |
| Spanish UI throughout | April 12, 2026 | Bar staff in Spain |
| Code in English | April 12, 2026 | Developer convention |
| Fine granularity (10 phases) | April 12, 2026 | 6-week deadline with 4 components |
| Interactive mode | April 12, 2026 | User wants to confirm each step |

## Tech Stack (Locked)

- **Backend:** Spring Boot 3.5.5 / Java 17 / PostgreSQL / Hibernate JPA / JJWT 0.11.5
- **Phone:** Kotlin 2.0.21 / Jetpack Compose BOM 2024.09.00 / Retrofit + Hilt + DataStore
- **Tablet:** Kotlin 2.0.21 / Jetpack Compose 1.7.4 / Retrofit + OkHttp WebSocket
- **PC:** Java 23 / JavaFX 21.0.2 / HttpURLConnection / PDFBox

## Known Issues (from codebase scan)

- ~~`anyRequest().permitAll()`~~ — fixed ✅
- ~~Hardcoded DB password~~ — moved to env ✅
- ~~Placeholder JWT secret~~ — moved to env ✅
- ~~Hardcoded IP in tablet~~ — fixed to `10.0.2.2` ✅
- ~~Phone client IP still hardcoded~~ — moved to BuildConfig/local.properties ✅
- ~~`usesCleartextTraffic="true"` on both Android apps~~ — scoped to debug manifests only ✅
- ~~`PUT /cashSession` returns `201 Created`~~ — verified false alarm; PUT already returns 200, POST open remains 201 ✅
- `CashSessionService.create()` is an empty stub
- `onNewOrder`, `onShiftToggle`, `onSettings` are TODO stubs in Phone
- No WebSocket reconnection on tablet disconnect

## Pending Todos

- (none yet — added as work progresses)

## Key Files

- `.planning/PROJECT.md` — Project context and requirements
- `.planning/ROADMAP.md` — Phased execution plan
- `.planning/REQUIREMENTS.md` — Checkable requirements
- `.planning/codebase/` — Codebase map (7 documents)

---
*State initialized: April 12, 2026*
