package com.ryabov.sentinelai.ingestion.controller

import com.ryabov.sentinelai.ingestion.model.SecurityEventAcceptedResponse
import com.ryabov.sentinelai.ingestion.model.SecurityEventRequest
import com.ryabov.sentinelai.ingestion.service.SecurityEventAcceptanceService
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.http.hateoas.JsonError
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid

/**
 * HTTP-контроллер первого входного boundary платформы.
 *
 * Принимает security events от внешних applications, агентов или будущего
 * `security-agent`, запускает validation через Micronaut и возвращает
 * `202 Accepted` только после успешной публикации в Kafka.
 */
@Controller("/api/v1/events")
@Tag(name = "Security Events", description = "Прием security events для дальнейшей asynchronous обработки")
class SecurityEventController(
    private val acceptanceService: SecurityEventAcceptanceService,
) {
    /**
     * Принимает один security event.
     *
     * Handler — `suspend`, чтобы HTTP-слой совпадал с coroutine service layer
     * и не блокировал event loop.
     */
    @Post
    @Operation(
        summary = "Принять security event",
        description = "Валидирует event envelope, публикует accepted event в Kafka и возвращает acceptance id.",
    )
    @ApiResponse(
        responseCode = "202",
        description = "Security event принят",
        content = [Content(schema = Schema(implementation = SecurityEventAcceptedResponse::class))],
    )
    @ApiResponse(
        responseCode = "400",
        description = "Ошибка validation request body",
        content = [Content(schema = Schema(implementation = JsonError::class))],
    )
    @ApiResponse(
        responseCode = "503",
        description = "Kafka publish не удался после timeout или retries",
        content = [Content(schema = Schema(implementation = JsonError::class))],
    )
    suspend fun accept(
        @Body @Valid request: SecurityEventRequest,
    ): HttpResponse<SecurityEventAcceptedResponse> =
        HttpResponse
            .status<SecurityEventAcceptedResponse>(HttpStatus.ACCEPTED)
            .body(acceptanceService.accept(request))
}
