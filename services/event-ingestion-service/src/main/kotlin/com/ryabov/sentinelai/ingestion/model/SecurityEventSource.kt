package com.ryabov.sentinelai.ingestion.model

import io.micronaut.serde.annotation.Serdeable
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Serdeable
data class SecurityEventSource(
    @field:NotBlank
    @field:Size(max = 128)
    val application: String?,

    @field:Size(max = 64)
    val ip: String? = null,

    @field:Size(max = 128)
    val deviceId: String? = null,

    @field:Size(max = 256)
    val endpoint: String? = null,

    @field:Size(max = 64)
    val region: String? = null
)
