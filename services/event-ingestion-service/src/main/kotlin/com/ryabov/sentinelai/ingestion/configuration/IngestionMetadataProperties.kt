package com.ryabov.sentinelai.ingestion.configuration

import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.core.bind.annotation.Bindable
import jakarta.validation.constraints.Positive

/**
 * Runtime-настройки ограничений `metadata` для входящих security events.
 *
 * Значения имеют safe local defaults в `application.yml`, но могут
 * переопределяться через environment variables, system properties или внешний
 * Micronaut configuration source для dev/CI/deployment окружений.
 */
@ConfigurationProperties("sentinel.ingestion.metadata")
interface IngestionMetadataProperties {

    /**
     * Максимальное количество key/value entries в `metadata` одного event.
     */
    @get:Bindable(defaultValue = "25")
    @get:Positive
    val maxEntries: Int

    /**
     * Максимальная длина одного metadata key.
     */
    @get:Bindable(defaultValue = "64")
    @get:Positive
    val maxKeyLength: Int

    /**
     * Максимальная длина одного metadata value.
     */
    @get:Bindable(defaultValue = "512")
    @get:Positive
    val maxValueLength: Int
}
