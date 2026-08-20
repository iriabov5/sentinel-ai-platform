package com.ryabov.sentinelai.ingestion.configuration

import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.core.bind.annotation.Bindable
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import java.time.Duration

/**
 * Runtime-настройки Kafka producer для публикации accepted security events.
 *
 * Safe local defaults рассчитаны на Docker Compose broker. Значения, которые
 * отличаются между окружениями, должны приходить через environment variables
 * или другой внешний configuration source.
 */
@ConfigurationProperties("sentinel.kafka")
interface IngestionKafkaProperties {

    /**
     * Kafka topic для accepted security events.
     */
    @get:Bindable(defaultValue = "security.events.raw")
    @get:NotBlank
    val topic: String

    /**
     * Максимальное время ожидания Kafka publish, после которого request
     * завершается ошибкой `503`.
     */
    @get:Bindable(defaultValue = "2s")
    val publishTimeout: Duration

    /**
     * Количество повторных попыток Kafka producer.
     */
    @get:Bindable(defaultValue = "3")
    @get:Positive
    val retries: Int
}
