package com.example.orderlytablet.services

/**
 * Phase 10 — D-01: DTO mirroring the backend CashSessionResponse shape.
 * Only `id` and `status` are strictly required for tablet session-gate logic.
 * Remaining fields are kept nullable for forward compatibility (backend JSON is a superset).
 * Date/time fields stored as String — the tablet does not need to parse them in Phase 10.
 */
data class CashSessionResponse(
    val id: Long,
    val status: String,
    val businessDate: String? = null,
    val shiftNo: Int? = null,
    val openedAt: String? = null,
    val closedAt: String? = null
)
