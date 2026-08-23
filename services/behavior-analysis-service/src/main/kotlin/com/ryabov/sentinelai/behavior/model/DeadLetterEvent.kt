package com.ryabov.sentinelai.behavior.model

import io.micronaut.serde.annotation.Serdeable
import java.time.Instant

@Serdeable
data class DeadLetterEvent(
    val originalTopic: String,
    val originalKey: String?,
    val payload: String,
    val reason: String,
    val failedAt: Instant
)
