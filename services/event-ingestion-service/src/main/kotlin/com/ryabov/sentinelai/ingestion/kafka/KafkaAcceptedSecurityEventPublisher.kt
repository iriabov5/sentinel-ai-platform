package com.ryabov.sentinelai.ingestion.kafka

import com.ryabov.sentinelai.ingestion.configuration.IngestionKafkaProperties
import com.ryabov.sentinelai.ingestion.model.AcceptedSecurityEvent
import com.ryabov.sentinelai.ingestion.service.AcceptedSecurityEventPublisher
import io.micronaut.context.annotation.Requires
import jakarta.inject.Named
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Kafka adapter, который публикует accepted event и ограничивает ожидание
 * configured timeout. Blocking producer IO выполняется на injected IO dispatcher.
 */
@Singleton
@Requires(notEnv = ["test"])
open class KafkaAcceptedSecurityEventPublisher(
    private val kafkaClient: AcceptedSecurityEventKafkaClient,
    private val kafkaProperties: IngestionKafkaProperties,
    @param:Named("io") private val ioDispatcher: CoroutineDispatcher
) : AcceptedSecurityEventPublisher {

    override suspend fun publish(event: AcceptedSecurityEvent) {
        val key = requireNotNull(event.subject.id) { "subject.id is required for Kafka partition key" }
        withContext(ioDispatcher) {
            kafkaClient.send(key, event)
                .get(kafkaProperties.publishTimeout.toMillis(), TimeUnit.MILLISECONDS)
        }
    }
}
