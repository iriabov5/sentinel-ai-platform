package com.ryabov.sentinelai.behavior.controller

import com.ryabov.sentinelai.behavior.model.SubjectFeatures
import com.ryabov.sentinelai.behavior.service.FeatureComputationService
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.NotBlank

/**
 * On-demand behavioral features для одного subject.
 *
 * Endpoint считается по owned event history в момент запроса; consume path
 * фичи не вычисляет. Susept-функция выполняется в coroutine без blocking IO.
 */
@Controller("/api/v1/subjects")
@Tag(name = "Subject Features", description = "On-demand behavioral features для subjects")
open class SubjectFeaturesController(
    private val featureComputationService: FeatureComputationService
) {

    /**
     * Возвращает behavioral features subject: `new_ip_device`, `unusual_time`,
     * `request_rate`, `download_volume`.
     */
    @Get("/{subjectId}/features")
    @Operation(
        summary = "Получить behavioral features subject",
        description = "Считает explainable behavioral features по owned event history на момент запроса."
    )
    @ApiResponse(
        responseCode = "200",
        description = "Features вычислены",
        content = [Content(schema = Schema(implementation = SubjectFeatures::class))]
    )
    @ApiResponse(responseCode = "400", description = "Некорректный subject id")
    @ApiResponse(responseCode = "503", description = "Event history недоступна для чтения")
    open suspend fun features(
        @PathVariable @NotBlank subjectId: String
    ): SubjectFeatures = featureComputationService.computeFeatures(subjectId)
}
