package com.ryabov.sentinelai.behavior.model

import io.micronaut.serde.annotation.Serdeable
import java.time.Instant

@Serdeable
data class AcceptedSecurityEvent(
    val eventId: String,
    val receivedAt: Instant,
    val eventType: SecurityEventType,
    val subject: SecurityEventSubject,
    val occurredAt: Instant,
    val source: SecurityEventSource,
    val metadata: Map<String, String> = emptyMap()
)
