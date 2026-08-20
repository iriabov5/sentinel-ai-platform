package com.ryabov.sentinelai.behavior.kafka

import io.micronaut.configuration.kafka.annotation.KafkaClient
import io.micronaut.configuration.kafka.annotation.KafkaKey
import io.micronaut.configuration.kafka.annotation.Topic
import io.micronaut.context.annotation.Requires
import org.apache.kafka.clients.producer.RecordMetadata
import java.util.concurrent.CompletableFuture

@Requires(notEnv = ["test"])
@KafkaClient(id = "security-events-dlq")
fun interface DeadLetterKafkaClient {

    @Topic("\${sentinel.kafka.topics.dlq}")
    fun send(
        @KafkaKey key: String,
        payload: String
    ): CompletableFuture<RecordMetadata>
}
