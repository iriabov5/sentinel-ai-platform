package com.ryabov.sentinelai.ingestion.kafka

import com.ryabov.sentinelai.ingestion.model.AcceptedSecurityEvent
import io.micronaut.configuration.kafka.annotation.KafkaClient
import io.micronaut.configuration.kafka.annotation.KafkaKey
import io.micronaut.configuration.kafka.annotation.Topic
import io.micronaut.context.annotation.Requires
import org.apache.kafka.clients.producer.RecordMetadata
import java.util.concurrent.CompletableFuture

/**
 * Micronaut Kafka producer client для topic accepted security events.
 */
@Requires(property = "kafka.enabled", value = "true", defaultValue = "true")
@KafkaClient(id = "accepted-security-events")
fun interface AcceptedSecurityEventKafkaClient {

    @Topic("\${sentinel.kafka.topic}")
    fun send(
        @KafkaKey key: String,
        event: AcceptedSecurityEvent
    ): CompletableFuture<RecordMetadata>
}
