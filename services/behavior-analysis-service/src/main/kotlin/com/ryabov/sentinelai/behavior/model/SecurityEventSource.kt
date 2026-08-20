package com.ryabov.sentinelai.behavior.model

import io.micronaut.serde.annotation.Serdeable

@Serdeable
data class SecurityEventSource(
    val application: String,
    val ip: String? = null,
    val deviceId: String? = null,
    val endpoint: String? = null,
    val region: String? = null
)
