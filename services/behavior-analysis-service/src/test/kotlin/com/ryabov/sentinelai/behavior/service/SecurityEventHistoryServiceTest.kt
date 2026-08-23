package com.ryabov.sentinelai.behavior.service

import com.ryabov.sentinelai.behavior.kafka.RecordingDeadLetterPublisher
import com.ryabov.sentinelai.behavior.model.AcceptedSecurityEvent
import com.ryabov.sentinelai.behavior.model.SecurityEventSource
import com.ryabov.sentinelai.behavior.model.SecurityEventSubject
import com.ryabov.sentinelai.behavior.model.SecurityEventType
import com.ryabov.sentinelai.behavior.model.SubjectType
import com.ryabov.sentinelai.behavior.persistence.InMemoryEventHistoryRepository
import io.micronaut.json.JsonMapper
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant

@MicronautTest
@DisplayName("Обработка Kafka events в event history")
class SecurityEventHistoryServiceTest {

    @Inject
    lateinit var service: SecurityEventHistoryService

    @Inject
    lateinit var repository: InMemoryEventHistoryRepository

    @Inject
    lateinit var deadLetterPublisher: RecordingDeadLetterPublisher

    @Inject
    lateinit var jsonMapper: JsonMapper

    @BeforeEach
    fun resetState() {
        repository.reset()
        deadLetterPublisher.reset()
    }

    @Test
    @DisplayName("Сохраняет valid event в history")
    fun `persists valid event`() = runBlocking {
        service.handleRaw("user-123", payload())

        assertEquals(1, repository.documents.size)
        assertEquals("event-1", repository.documents.keys.first())
        assertTrue(deadLetterPublisher.published.isEmpty())
    }

    @Test
    @DisplayName("Идемпотентно обрабатывает duplicate eventId")
    fun `duplicate event id is stored once`() = runBlocking {
        val raw = payload()
        service.handleRaw("user-123", raw)
        service.handleRaw("user-123", raw)

        assertEquals(1, repository.documents.size)
        assertTrue(deadLetterPublisher.published.isEmpty())
    }

    @Test
    @DisplayName("Отправляет poison payload в DLQ после retries")
    fun `poison payload goes to dlq`() = runBlocking {
        service.handleRaw("user-123", "{not-json")

        assertTrue(repository.documents.isEmpty())
        assertEquals(1, deadLetterPublisher.published.size)
        assertEquals("user-123", deadLetterPublisher.published.first().first)
        assertTrue(deadLetterPublisher.published.first().second.contains("not-json"))
    }

    @Test
    @DisplayName("Отправляет record в DLQ, если persist исчерпал retries")
    fun `persist failures go to dlq`() = runBlocking {
        repository.failNext(3)

        service.handleRaw("user-123", payload())

        assertTrue(repository.documents.isEmpty())
        assertEquals(1, deadLetterPublisher.published.size)
    }

    @Test
    @DisplayName("Пробрасывает ошибку, если persist и DLQ не удались")
    fun `rethrows when persist and dlq fail`() {
        repository.failNext(3)
        deadLetterPublisher.shouldFail = true

        val exception = assertThrows(IllegalStateException::class.java) {
            runBlocking {
                service.handleRaw("user-123", payload())
            }
        }

        assertEquals("MongoDB unavailable", exception.message)
    }

    private fun payload(): String {
        val event = AcceptedSecurityEvent(
            eventId = "event-1",
            receivedAt = Instant.parse("2026-08-20T10:15:00Z"),
            eventType = SecurityEventType.LOGIN_FAILED,
            subject = SecurityEventSubject(type = SubjectType.USER, id = "user-123"),
            occurredAt = Instant.parse("2026-08-20T10:14:00Z"),
            source = SecurityEventSource(application = "billing-api"),
            metadata = mapOf("reason" to "INVALID_PASSWORD")
        )
        return jsonMapper.writeValueAsBytes(event).toString(Charsets.UTF_8)
    }
}
