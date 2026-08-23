package com.ryabov.sentinelai.ingestion.kafka

import com.ryabov.sentinelai.ingestion.configuration.IngestionKafkaProperties
import com.ryabov.sentinelai.ingestion.model.AcceptedSecurityEvent
import com.ryabov.sentinelai.ingestion.model.SecurityEventSource
import com.ryabov.sentinelai.ingestion.model.SecurityEventSubject
import com.ryabov.sentinelai.ingestion.model.SecurityEventType
import com.ryabov.sentinelai.ingestion.model.SubjectType
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.apache.kafka.clients.producer.RecordMetadata
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeoutException

@DisplayName("Kafka publisher accepted security events")
class KafkaAcceptedSecurityEventPublisherTest {

    @Test
    @DisplayName("Публикует event с key равным subject.id")
    fun `publishes event with subject id key`() = runBlocking {
        val kafkaClient = mockk<AcceptedSecurityEventKafkaClient>()
        val metadata = mockk<RecordMetadata>()
        every { kafkaClient.send(any(), any()) } returns CompletableFuture.completedFuture(metadata)

        val publisher = KafkaAcceptedSecurityEventPublisher(
            kafkaClient,
            testProperties(),
            Dispatchers.Unconfined
        )
        val event = acceptedEvent()

        publisher.publish(event)

        verify { kafkaClient.send("user-123", event) }
    }

    @Test
    @DisplayName("Пробрасывает ошибку, если Kafka publish не завершился вовремя")
    fun `throws when kafka publish times out`() {
        val kafkaClient = mockk<AcceptedSecurityEventKafkaClient>()
        every { kafkaClient.send(any(), any()) } returns CompletableFuture.failedFuture(TimeoutException("timeout"))

        val publisher = KafkaAcceptedSecurityEventPublisher(
            kafkaClient,
            testProperties(),
            Dispatchers.Unconfined
        )

        assertThrows(Exception::class.java) {
            runBlocking {
                publisher.publish(acceptedEvent())
            }
        }
    }

    private fun acceptedEvent(): AcceptedSecurityEvent =
        AcceptedSecurityEvent(
            eventId = "event-1",
            receivedAt = Instant.parse("2026-08-20T10:15:00Z"),
            eventType = SecurityEventType.LOGIN_FAILED,
            subject = SecurityEventSubject(type = SubjectType.USER, id = "user-123"),
            occurredAt = Instant.parse("2026-08-20T10:14:00Z"),
            source = SecurityEventSource(application = "billing-api"),
            metadata = mapOf("reason" to "INVALID_PASSWORD")
        )

    private fun testProperties(): IngestionKafkaProperties =
        object : IngestionKafkaProperties {
            override val topic: String = "security.events.raw"
            override val publishTimeout: Duration = Duration.ofSeconds(2)
            override val retries: Int = 3
        }
}
