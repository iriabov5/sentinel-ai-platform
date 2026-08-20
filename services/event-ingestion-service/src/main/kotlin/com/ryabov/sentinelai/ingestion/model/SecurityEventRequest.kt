package com.ryabov.sentinelai.ingestion.model

import io.micronaut.serde.annotation.Serdeable
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.Instant

@Serdeable
data class SecurityEventRequest(
    @field:NotNull
    val eventType: SecurityEventType?,

    @field:Valid
    @field:NotNull
    val subject: SecurityEventSubject?,

    @field:NotNull
    val occurredAt: Instant?,

    @field:Valid
    @field:NotNull
    val source: SecurityEventSource?,

    @field:Size(max = 25)
    val metadata: Map<String, String> = emptyMap()
)
