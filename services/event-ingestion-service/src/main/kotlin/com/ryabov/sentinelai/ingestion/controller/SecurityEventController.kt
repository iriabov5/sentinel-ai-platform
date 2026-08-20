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
import kotlinx.coroutines.reactor.mono
import reactor.core.publisher.Mono

/**
 * Reactive HTTP-контроллер первого входного boundary платформы.
 *
 * Принимает security events от внешних applications, агентов или будущего
 * `security-agent`, запускает validation через Micronaut и возвращает
 * `202 Accepted`, не выполняя downstream processing синхронно в request path.
 */
@Controller("/api/v1/events")
@Tag(name = "Security Events", description = "Прием security events для дальнейшей asynchronous обработки")
open class SecurityEventController(
    private val acceptanceService: SecurityEventAcceptanceService
) {

    /**
     * Реактивно принимает один security event.
     *
     * Метод возвращает `Mono`, чтобы controller contract был явно reactive:
     * HTTP thread не блокируется, а coroutine-based service layer выполняется
     * внутри reactive publisher.
     */
    @Post
    @Operation(
        summary = "Принять security event",
        description = "Валидирует event envelope и возвращает acceptance id для downstream processing."
    )
    @ApiResponse(
        responseCode = "202",
        description = "Security event принят",
        content = [Content(schema = Schema(implementation = SecurityEventAcceptedResponse::class))]
    )
    @ApiResponse(
        responseCode = "400",
        description = "Ошибка validation request body",
        content = [Content(schema = Schema(implementation = JsonError::class))]
    )
    open fun accept(@Body @Valid request: SecurityEventRequest): Mono<HttpResponse<SecurityEventAcceptedResponse>> =
        mono {
            HttpResponse.status<SecurityEventAcceptedResponse>(HttpStatus.ACCEPTED)
                .body(acceptanceService.accept(request))
        }
}
