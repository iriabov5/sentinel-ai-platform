package com.ryabov.sentinelai.ingestion.controller

import com.ryabov.sentinelai.ingestion.kafka.RecordingAcceptedSecurityEventPublisher
import com.ryabov.sentinelai.ingestion.model.SecurityEventAcceptedResponse
import com.ryabov.sentinelai.ingestion.model.SecurityEventRequest
import com.ryabov.sentinelai.ingestion.model.SecurityEventSource
import com.ryabov.sentinelai.ingestion.model.SecurityEventSubject
import com.ryabov.sentinelai.ingestion.model.SecurityEventType
import com.ryabov.sentinelai.ingestion.model.SubjectType
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

@MicronautTest
@DisplayName("HTTP API приема security events")
class SecurityEventControllerIntegrationTest {

    @Inject
    @field:Client("/")
    lateinit var client: HttpClient

    @Inject
    lateinit var publisher: RecordingAcceptedSecurityEventPublisher

    @BeforeEach
    fun resetPublisher() {
        publisher.reset()
    }

    @Test
    @DisplayName("Возвращает 202 Accepted для valid security event")
    fun `valid event returns accepted response`() {
        val response = client.toBlocking().exchange(
            HttpRequest.POST("/api/v1/events", validRequest()),
            SecurityEventAcceptedResponse::class.java
        )

        assertEquals(HttpStatus.ACCEPTED, response.status)
        val body = response.body()
        assertNotNull(body)
        UUID.fromString(body.eventId)
        assertEquals("ACCEPTED", body.status.name)
        assertNotNull(body.receivedAt)
        assertEquals(1, publisher.published.size)
        val published = publisher.published.first()
        assertEquals(body.eventId, published.eventId)
        assertEquals("user-123", published.subject.id)
    }

    @Test
    @DisplayName("Возвращает 400 Bad Request без required nested fields")
    fun `missing nested fields returns bad request`() {
        val invalidRequest = validRequest().copy(
            subject = SecurityEventSubject(type = SubjectType.USER, id = "")
        )

        val exception = assertThrows(HttpClientResponseException::class.java) {
            client.toBlocking().exchange(
                HttpRequest.POST("/api/v1/events", invalidRequest),
                SecurityEventAcceptedResponse::class.java
            )
        }

        assertEquals(HttpStatus.BAD_REQUEST, exception.status)
        assertTrue(publisher.published.isEmpty())
    }

    @Test
    @DisplayName("Возвращает 400 Bad Request при превышении metadata limits")
    fun `metadata limits return bad request`() {
        val invalidRequest = validRequest().copy(
            metadata = mapOf("x".repeat(65) to "value")
        )

        val exception = assertThrows(HttpClientResponseException::class.java) {
            client.toBlocking().exchange(
                HttpRequest.POST("/api/v1/events", invalidRequest),
                SecurityEventAcceptedResponse::class.java
            )
        }

        assertEquals(HttpStatus.BAD_REQUEST, exception.status)
        assertFalse(exception.message.isNullOrBlank())
        assertTrue(publisher.published.isEmpty())
    }

    @Test
    @DisplayName("Возвращает 503 Service Unavailable, если Kafka publish не удался")
    fun `kafka publish failure returns service unavailable`() {
        publisher.shouldFail = true

        val exception = assertThrows(HttpClientResponseException::class.java) {
            client.toBlocking().exchange(
                HttpRequest.POST("/api/v1/events", validRequest()),
                SecurityEventAcceptedResponse::class.java
            )
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
                application = "billing-api",
                ip = "203.0.113.42",
                deviceId = "device-abc"
            ),
            metadata = mapOf("reason" to "INVALID_PASSWORD")
        )
}
