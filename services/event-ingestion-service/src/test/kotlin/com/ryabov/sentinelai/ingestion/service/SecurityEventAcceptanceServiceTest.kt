package com.ryabov.sentinelai.ingestion.service

import com.ryabov.sentinelai.ingestion.configuration.IngestionMetadataProperties
import com.ryabov.sentinelai.ingestion.model.AcceptedSecurityEvent
import com.ryabov.sentinelai.ingestion.model.SecurityEventAcceptanceStatus
import com.ryabov.sentinelai.ingestion.model.SecurityEventRequest
import com.ryabov.sentinelai.ingestion.model.SecurityEventSource
import com.ryabov.sentinelai.ingestion.model.SecurityEventSubject
import com.ryabov.sentinelai.ingestion.model.SecurityEventType
import com.ryabov.sentinelai.ingestion.model.SubjectType
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

@DisplayName("Acceptance service security events")
class SecurityEventAcceptanceServiceTest {

    private val publisher = RecordingPublisher()
    private val service = SecurityEventAcceptanceService(testMetadataProperties(), publisher)

    @BeforeEach
    fun resetPublisher() {
        publisher.reset()
    }

    @Test
    @DisplayName("Создает acceptance response и публикует valid event")
    fun `valid event creates acceptance response and publishes`() = runBlocking {
        val response = service.accept(validRequest())

        UUID.fromString(response.eventId)
        assertEquals(SecurityEventAcceptanceStatus.ACCEPTED, response.status)
        assertNotNull(response.receivedAt)
        assertEquals(1, publisher.published.size)
        val published = publisher.published.first()
        assertEquals(response.eventId, published.eventId)
        assertEquals(response.receivedAt, published.receivedAt)
        assertEquals("user-123", published.subject.id)
    }

    @Test
    @DisplayName("Не публикует event при ошибке metadata validation")
    fun `invalid metadata is not published`() {
        val exception = assertThrows(HttpStatusException::class.java) {
            runBlocking {
                service.accept(validRequest().copy(metadata = mapOf(" " to "value")))
            }
        }

        assertEquals(HttpStatus.BAD_REQUEST, exception.status)
        assertTrue(publisher.published.isEmpty())
    }

    @Test
    @DisplayName("Отклоняет слишком длинное значение metadata")
    fun `long metadata value is rejected`() {
        val exception = assertThrows(HttpStatusException::class.java) {
            runBlocking {
                service.accept(validRequest().copy(metadata = mapOf("reason" to "x".repeat(513))))
            }
        }

        assertEquals(HttpStatus.BAD_REQUEST, exception.status)
        assertTrue(publisher.published.isEmpty())
    }

    @Test
    @DisplayName("Отклоняет metadata с количеством entries выше configured limit")
    fun `too many metadata entries are rejected`() {
        val exception = assertThrows(HttpStatusException::class.java) {
            runBlocking {
                service.accept(
                    validRequest().copy(
                        metadata = mapOf(
                            "one" to "1",
                            "two" to "2",
                            "three" to "3",
                            "four" to "4"
                        )
                    )
                )
            }
        }

        assertEquals(HttpStatus.BAD_REQUEST, exception.status)
        assertTrue(publisher.published.isEmpty())
    }

    @Test
    @DisplayName("Возвращает 503, если Kafka publish завершился ошибкой")
    fun `kafka publish failure returns service unavailable`() {
        publisher.shouldFail = true

        val exception = assertThrows(HttpStatusException::class.java) {
            runBlocking {
                service.accept(validRequest())
            }
        }

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.status)
        assertTrue(publisher.published.isEmpty())
    }

    private fun validRequest(): SecurityEventRequest =
        SecurityEventRequest(
            eventType = SecurityEventType.LOGIN_FAILED,
            subject = SecurityEventSubject(
                type = SubjectType.USER,
                id = "user-123"
            ),
            occurredAt = Instant.parse("2026-08-20T10:15:00Z"),
            source = SecurityEventSource(
                application = "billing-api"
            ),
            metadata = mapOf("reason" to "INVALID_PASSWORD")
        )

    private fun testMetadataProperties(): IngestionMetadataProperties =
        object : IngestionMetadataProperties {
            override val maxEntries: Int = 3
            override val maxKeyLength: Int = 64
            override val maxValueLength: Int = 512
        }

    private class RecordingPublisher : AcceptedSecurityEventPublisher {
        val published = mutableListOf<AcceptedSecurityEvent>()
        var shouldFail: Boolean = false

        override suspend fun publish(event: AcceptedSecurityEvent) {
            if (shouldFail) {
                throw IllegalStateException("Kafka unavailable")
            }
            published += event
        }

        fun reset() {
            published.clear()
            shouldFail = false
        }
    }
}
