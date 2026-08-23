package com.ryabov.sentinelai.behavior.configuration

import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.core.bind.annotation.Bindable
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive

/**
 * Runtime-настройки Kafka consumer и dead-letter topic.
 */
@ConfigurationProperties("sentinel.kafka")
interface BehaviorKafkaProperties {

    val topics: Topics

    /**
     * Сколько раз consumer пытается обработать record до публикации в DLQ.
     */
    @get:Bindable(defaultValue = "3")
    @get:Positive
    val processingRetries: Int

    @ConfigurationProperties("topics")
    interface Topics {

        @get:Bindable(defaultValue = "security.events.raw")
        @get:NotBlank
        val raw: String

        @get:Bindable(defaultValue = "security.events.raw.dlq")
        @get:NotBlank
        val dlq: String
    }
}
