package com.ryabov.sentinelai.behavior.controller

import com.ryabov.sentinelai.behavior.service.FeaturesUnavailableException
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Produces
import io.micronaut.http.hateoas.JsonError
import io.micronaut.http.server.exceptions.ExceptionHandler
import jakarta.inject.Singleton

/**
 * Транслирует сбой чтения event history в `503 Service Unavailable`.
 */
@Singleton
@Produces
open class FeaturesUnavailableExceptionHandler :
    ExceptionHandler<FeaturesUnavailableException, HttpResponse<JsonError>> {

    override fun handle(
        request: HttpRequest<*>,
        exception: FeaturesUnavailableException
    ): HttpResponse<JsonError> {
        val error = JsonError("Feature computation unavailable: ${exception.message}")
        return HttpResponse.status<JsonError>(HttpStatus.SERVICE_UNAVAILABLE).body(error)
    }
}
