package com.ryabov.sentinelai.ingestion.service

import com.ryabov.sentinelai.ingestion.model.AcceptedSecurityEvent

/**
 * Публикует accepted security event в Kafka до возврата `202 Accepted`.
 */
fun interface AcceptedSecurityEventPublisher {

    /**
     * Публикует событие с Kafka key равным `subject.id`.
     *
     * @throws Exception если broker недоступен, publish превысил timeout или
     * retries исчерпаны. Caller обязан не возвращать клиенту success.
     */
    suspend fun publish(event: AcceptedSecurityEvent)
}
