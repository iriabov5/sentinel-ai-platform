package com.ryabov.sentinelai.ingestion.kafka

import com.ryabov.sentinelai.ingestion.model.AcceptedSecurityEvent
import com.ryabov.sentinelai.ingestion.service.AcceptedSecurityEventPublisher
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Test double, который запоминает published events и может эмулировать сбой Kafka.
 */
@Singleton
@Requires(property = "kafka.enabled", value = "false")
class RecordingAcceptedSecurityEventPublisher : AcceptedSecurityEventPublisher {
    val published: ConcurrentLinkedQueue<AcceptedSecurityEvent> = ConcurrentLinkedQueue()

    @Volatile
    var shouldFail: Boolean = false

    override suspend fun publish(event: AcceptedSecurityEvent) {
        if (shouldFail) {
            error("Kafka unavailable")
        }
        published.add(event)
    }

    fun reset() {
        published.clear()
        shouldFail = false
    }
}
