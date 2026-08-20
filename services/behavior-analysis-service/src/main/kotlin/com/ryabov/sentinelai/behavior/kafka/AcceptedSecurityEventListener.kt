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
 */
@Requires(notEnv = ["test"])
@KafkaListener(groupId = "behavior-analysis-service")
open class AcceptedSecurityEventListener(
    private val historyService: SecurityEventHistoryService
) {

    @Topic("\${sentinel.kafka.topics.raw}")
    open fun receive(@KafkaKey key: String?, value: String) {
        runBlocking {
            historyService.handleRaw(key, value)
        }
    }
}
