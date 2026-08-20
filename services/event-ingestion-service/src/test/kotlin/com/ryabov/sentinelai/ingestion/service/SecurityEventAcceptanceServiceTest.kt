package com.ryabov.sentinelai.ingestion.service

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
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

@DisplayName("Acceptance service security events")
class SecurityEventAcceptanceServiceTest {

    private val service = SecurityEventAcceptanceService()

    @Test
    @DisplayName("Создает acceptance response для valid event")
    fun `valid event creates acceptance response`() = runBlocking {
        val response = service.accept(validRequest())

        UUID.fromString(response.eventId)
        assertEquals(SecurityEventAcceptanceStatus.ACCEPTED, response.status)
        assertNotNull(response.receivedAt)
    }

    @Test
    @DisplayName("Отклоняет metadata с пустым ключом")
    fun `blank metadata key is rejected`() {
        val exception = assertThrows(HttpStatusException::class.java) {
            runBlocking {
                service.accept(validRequest().copy(metadata = mapOf(" " to "value")))
            }
        }

        assertEquals(HttpStatus.BAD_REQUEST, exception.status)
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
}
