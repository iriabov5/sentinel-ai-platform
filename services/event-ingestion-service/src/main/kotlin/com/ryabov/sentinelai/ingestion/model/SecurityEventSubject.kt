package com.ryabov.sentinelai.ingestion.model

import io.micronaut.serde.annotation.Serdeable
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

@Serdeable
data class SecurityEventSubject(
    @field:NotNull
    val type: SubjectType?,

    @field:NotBlank
    @field:Size(max = 128)
    val id: String?
)
