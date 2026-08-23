package com.ryabov.sentinelai.behavior.model

import java.time.Instant

/**
 * Owned MongoDB document для event history.
 */
data class EventHistoryDocument(
    val eventId: String,
    val receivedAt: Instant,
    val eventType: SecurityEventType,
    val subject: SecurityEventSubject,
    val occurredAt: Instant,
    val source: SecurityEventSource,
    val metadata: Map<String, String>,
    val storedAt: Instant
)
