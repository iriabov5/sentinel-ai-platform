package com.ryabov.sentinelai.ingestion.service

import com.ryabov.sentinelai.ingestion.configuration.IngestionMetadataProperties
import com.ryabov.sentinelai.ingestion.model.AcceptedSecurityEvent
import com.ryabov.sentinelai.ingestion.model.SecurityEventAcceptedResponse
import com.ryabov.sentinelai.ingestion.model.SecurityEventAcceptanceStatus
import com.ryabov.sentinelai.ingestion.model.SecurityEventRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import jakarta.inject.Singleton
import java.time.Instant
import java.util.UUID

/**
 * Application service для acceptance-этапа приема security events.
 *
 * Сервис проверяет bounded metadata rules, создает `eventId` и публикует
 * accepted event в Kafka. `202 Accepted` возвращается только после успешной
 * публикации.
 */
@Singleton
open class SecurityEventAcceptanceService(
    private val metadataProperties: IngestionMetadataProperties,
    private val eventPublisher: AcceptedSecurityEventPublisher
) {

    /**
     * Принимает уже провалидированный Micronaut request, публикует событие и
     * возвращает результат приема.
     *
     * Если Kafka publish не удался, метод бросает `503`, чтобы клиент не получил
     * success для unpublished event.
     */
    suspend fun accept(request: SecurityEventRequest): SecurityEventAcceptedResponse {
        validateMetadata(request)

        val eventId = UUID.randomUUID().toString()
        val receivedAt = Instant.now()
        val acceptedEvent = request.toAcceptedEvent(eventId, receivedAt)

        try {
            eventPublisher.publish(acceptedEvent)
        } catch (ex: HttpStatusException) {
            throw ex
        } catch (ex: Exception) {
            throw HttpStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Failed to publish security event to Kafka"
            )
        }

        return SecurityEventAcceptedResponse(
            eventId = eventId,
            status = SecurityEventAcceptanceStatus.ACCEPTED,
            receivedAt = receivedAt
        )
    }

    private fun validateMetadata(request: SecurityEventRequest) {
        if (request.metadata.size > metadataProperties.maxEntries) {
            throw HttpStatusException(
                HttpStatus.BAD_REQUEST,
                "metadata entries count must be <= ${metadataProperties.maxEntries}"
            )
        }

        request.metadata.forEach { (key, value) ->
            if (key.isBlank()) {
                throw HttpStatusException(HttpStatus.BAD_REQUEST, "metadata keys must not be blank")
            }
            if (key.length > metadataProperties.maxKeyLength) {
                throw HttpStatusException(
                    HttpStatus.BAD_REQUEST,
                    "metadata key length must be <= ${metadataProperties.maxKeyLength}"
                )
            }
            if (value.length > metadataProperties.maxValueLength) {
                throw HttpStatusException(
                    HttpStatus.BAD_REQUEST,
                    "metadata value length must be <= ${metadataProperties.maxValueLength}"
                )
            }
        }
    }

    private fun SecurityEventRequest.toAcceptedEvent(
        eventId: String,
        receivedAt: Instant
    ): AcceptedSecurityEvent =
        AcceptedSecurityEvent(
            eventId = eventId,
            receivedAt = receivedAt,
            eventType = requireNotNull(eventType),
            subject = requireNotNull(subject),
            occurredAt = requireNotNull(occurredAt),
            source = requireNotNull(source),
            metadata = metadata
        )
}
