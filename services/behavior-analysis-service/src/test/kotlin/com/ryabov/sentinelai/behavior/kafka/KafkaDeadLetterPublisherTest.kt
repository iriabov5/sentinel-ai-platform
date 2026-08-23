package com.ryabov.sentinelai.behavior.kafka

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.apache.kafka.clients.producer.RecordMetadata
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.concurrent.CompletableFuture

@DisplayName("Kafka DLQ publisher")
class KafkaDeadLetterPublisherTest {

    @Test
    @DisplayName("Публикует poison record в DLQ topic")
    fun `publishes payload to dlq`() = runBlocking {
        val kafkaClient = mockk<DeadLetterKafkaClient>()
        every { kafkaClient.send(any(), any()) } returns CompletableFuture.completedFuture(mockk<RecordMetadata>())

        KafkaDeadLetterPublisher(kafkaClient, Dispatchers.Unconfined)
            .publish("user-123", "{\"payload\":\"x\"}")

        verify { kafkaClient.send("user-123", "{\"payload\":\"x\"}") }
    }
}
