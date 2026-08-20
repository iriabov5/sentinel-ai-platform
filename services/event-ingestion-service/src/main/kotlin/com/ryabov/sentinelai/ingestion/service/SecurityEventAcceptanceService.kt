package com.ryabov.sentinelai.ingestion.service

import com.ryabov.sentinelai.ingestion.model.SecurityEventAcceptedResponse
import com.ryabov.sentinelai.ingestion.model.SecurityEventAcceptanceStatus
import com.ryabov.sentinelai.ingestion.model.SecurityEventRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import jakarta.inject.Singleton
import java.util.UUID

/**
 * Application service для acceptance-этапа приема security events.
 *
 * Сервис проверяет bounded metadata rules и создает acceptance response с
 * техническим `eventId`. На этом этапе он сознательно не публикует событие в
 * Kafka и не пишет данные в storage: эти responsibilities будут добавлены
 * отдельными OpenSpec changes.
 */
@Singleton
open class SecurityEventAcceptanceService {

    /**
     * Принимает уже провалидированный Micronaut request и возвращает результат
     * первичного приема события.
     *
     * Метод остается `suspend`, чтобы service layer был готов к будущим
     * неблокирующим операциям, например Kafka producer с timeout/failure rules.
     */
    suspend fun accept(request: SecurityEventRequest): SecurityEventAcceptedResponse {
        validateMetadata(request)

        return SecurityEventAcceptedResponse(
            eventId = UUID.randomUUID().toString(),
            status = SecurityEventAcceptanceStatus.ACCEPTED,
            receivedAt = java.time.Instant.now()
        )
    }

    private fun validateMetadata(request: SecurityEventRequest) {
        request.metadata.forEach { (key, value) ->
            if (key.isBlank()) {
                throw HttpStatusException(HttpStatus.BAD_REQUEST, "metadata keys must not be blank")
            }
            if (key.length > MAX_METADATA_KEY_LENGTH) {
                throw HttpStatusException(HttpStatus.BAD_REQUEST, "metadata key length must be <= $MAX_METADATA_KEY_LENGTH")
            }
            if (value.length > MAX_METADATA_VALUE_LENGTH) {
                throw HttpStatusException(HttpStatus.BAD_REQUEST, "metadata value length must be <= $MAX_METADATA_VALUE_LENGTH")
            }
        }
    }

    companion object {
        const val MAX_METADATA_KEY_LENGTH = 64
        const val MAX_METADATA_VALUE_LENGTH = 512
    }
}
