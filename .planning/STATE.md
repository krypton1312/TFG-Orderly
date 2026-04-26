---
gsd_state_version: 1.0
milestone: v1.0
milestone_name: milestone
current_phase: 07
status: Phase 06 complete — ready for Phase 07
stopped_at: Phase 6 complete — all 3 plans executed and committed
last_updated: "2026-04-26T14:00:00.000Z"
progress:
  total_phases: 10
  completed_phases: 5
  total_plans: 21
  completed_plans: 21
  percent: 100
---

# Project State

**Project:** Orderly — Bar Management System
**Milestone:** v1.0 — TFG Submission
**Current Phase:** 06
**Last Updated:** April 19, 2026
**Last session:** 2026-04-19
**Last Date:** 2026-04-19
**Stopped At:** Phase 5 complete — all 3 plans executed + post-fix commits
**Resume File:** .planning/phases/06-/

## Position

- **Current phase:** 6 — Cash Session Management (PC)
- **Phases complete:** 5 of 10
- **Milestone progress:** ~50%
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

## Out-of-Phase Changes

| Date | Description | Files |
|------|-------------|-------|
| 2026-04-13 | **Login by username (first-letter-name + lastname)** — added `username` column to `Employee`, dual-lookup in `CustomUserDetailsService` (email if `@`, else username), `LoginRequest.identifier` replaces `email`, auto-generated username on register. All clients (PC, Phone, Tablet) updated. | `Employee.java`, `EmployeeRepository.java`, `CustomUserDetailsService.java`, `LoginRequest.java` (backend), `AuthController.java`, `EmployeeResponse.java`, `AuthService.java` (PC), `LoginRequest.kt`, `AuthRepository.kt`, `LoginViewModel.kt`, `LoginScreen.kt` (Phone + Tablet) |
| 2026-04-13 | **Temporary password + mustChangePassword flow** — Employee creation auto-generates `TmpXXXXXX` temp password (`mustChangePassword=true`). "Cambiar contraseña" resets to new temp via `POST /employees/{id}/reset-password`. Login response includes `mustChangePassword`; if true, Phone/Tablet redirect to a forced change-password screen before accessing the app. `POST /auth/change-password` clears the flag. Soft delete (INACTIVE) replaces hard delete. | Backend: `Employee.java`, `AuthResponse.java`, `AuthController.java`, `ChangePasswordRequest.java` (new), `EmployeeService.java`, `EmployeeServiceInterface.java`, `EmployeeController.java`, `EmployeeResponse.java`. Phone: `AuthResponse.kt`, `LoginState.kt`, `AuthRepository.kt`, `AuthApi.kt`, `ChangePasswordRequest.kt` (new), `LoginViewModel.kt`, `LoginScreen.kt`, `AppNav.kt`, `ChangePasswordViewModel.kt` (new), `ChangePasswordScreen.kt` (new). Tablet: `AuthApi.kt`, `LoginViewModel.kt`, `MainActivity.kt`, `ChangePasswordScreen.kt` (new). |

## Pending Todos

- (none yet — added as work progresses)

## Key Files

- `.planning/PROJECT.md` — Project context and requirements
- `.planning/ROADMAP.md` — Phased execution plan
- `.planning/REQUIREMENTS.md` — Checkable requirements
- `.planning/codebase/` — Codebase map (7 documents)

---
*State initialized: April 12, 2026*
