package com.ryabov.sentinelai.ingestion.model

import io.micronaut.serde.annotation.Serdeable
import java.time.Instant

@Serdeable
data class SecurityEventAcceptedResponse(
    val eventId: String,
    val status: SecurityEventAcceptanceStatus,
    val receivedAt: Instant
)
