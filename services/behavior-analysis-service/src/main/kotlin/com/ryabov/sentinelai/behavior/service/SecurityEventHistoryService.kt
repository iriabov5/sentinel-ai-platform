package com.ryabov.sentinelai.behavior.service

import com.ryabov.sentinelai.behavior.configuration.BehaviorKafkaProperties
import com.ryabov.sentinelai.behavior.model.AcceptedSecurityEvent
import com.ryabov.sentinelai.behavior.model.DeadLetterEvent
import com.ryabov.sentinelai.behavior.model.EventHistoryDocument
import io.micronaut.json.JsonMapper
import jakarta.inject.Singleton
import java.time.Instant

/**
 * Обрабатывает raw Kafka record: parse, persist event history, dead-letter при
 * устойчивом сбое. Feature extraction в этот service не входит.
 */
@Singleton
open class SecurityEventHistoryService(
    private val repository: EventHistoryRepository,
    private val deadLetterPublisher: DeadLetterPublisher,
    private val kafkaProperties: BehaviorKafkaProperties,
    private val jsonMapper: JsonMapper
) {

    suspend fun handleRaw(key: String?, payload: String) {
        var lastError: Exception? = null
        repeat(kafkaProperties.processingRetries) {
            try {
                val event = parseEvent(payload)
                repository.insertIgnoringDuplicateEventId(event.toHistoryDocument())
                return
            } catch (ex: Exception) {
                lastError = ex
            }
        }
        publishDeadLetter(key, payload, lastError)
    }

    private fun parseEvent(payload: String): AcceptedSecurityEvent {
        val event = jsonMapper.readValue(
            payload.toByteArray(Charsets.UTF_8),
            AcceptedSecurityEvent::class.java
        )
        require(event.eventId.isNotBlank()) { "eventId must not be blank" }
        require(event.subject.id.isNotBlank()) { "subject.id must not be blank" }
        require(event.source.application.isNotBlank()) { "source.application must not be blank" }
        return event
    }

    private suspend fun publishDeadLetter(key: String?, payload: String, error: Exception?) {
        val dlqKey = key?.takeIf { it.isNotBlank() } ?: "unknown"
        val dlqPayload = jsonMapper.writeValueAsBytes(
            DeadLetterEvent(
                originalTopic = kafkaProperties.topics.raw,
                originalKey = key,
                payload = payload,
                reason = error?.message ?: "unknown processing error",
                failedAt = Instant.now()
            )
        ).toString(Charsets.UTF_8)
        try {
            deadLetterPublisher.publish(dlqKey, dlqPayload)
        } catch (dlqError: Exception) {
            throw error ?: dlqError
        }
    }

    private fun AcceptedSecurityEvent.toHistoryDocument(): EventHistoryDocument =
        EventHistoryDocument(
            eventId = eventId,
            receivedAt = receivedAt,
            eventType = eventType,
            subject = subject,
            occurredAt = occurredAt,
            source = source,
            metadata = metadata,
            storedAt = Instant.now()
        )
}
