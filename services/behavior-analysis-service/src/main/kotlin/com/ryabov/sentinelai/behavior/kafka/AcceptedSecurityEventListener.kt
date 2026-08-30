package com.ryabov.sentinelai.behavior.kafka

import com.ryabov.sentinelai.behavior.service.SecurityEventHistoryService
import io.micronaut.configuration.kafka.annotation.KafkaKey
import io.micronaut.configuration.kafka.annotation.KafkaListener
import io.micronaut.configuration.kafka.annotation.Topic
import io.micronaut.context.annotation.Requires
import kotlinx.coroutines.runBlocking

/**
 * Kafka consumer adapter. Метод listener дожидается persist/DLQ, чтобы offset
 * коммитился только после обработки record.
 *
 * Micronaut Kafka callback не `suspend`, поэтому здесь остаётся тонкий
 * `runBlocking` только на границе listener → coroutine service.
 */
@Requires(property = "kafka.enabled", value = "true", defaultValue = "true")
@KafkaListener(groupId = "behavior-analysis-service")
class AcceptedSecurityEventListener(
    private val historyService: SecurityEventHistoryService,
) {
    @Topic("\${sentinel.kafka.topics.raw}")
    fun receive(
        @KafkaKey key: String?,
        value: String,
    ) {
        runBlocking {
            historyService.handleRaw(key, value)
        }
    }
}
