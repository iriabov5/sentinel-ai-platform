package com.ryabov.sentinelai.behavior.model

import io.micronaut.serde.annotation.Serdeable

@Serdeable
data class SecurityEventSubject(
    val type: SubjectType,
    val id: String
)
