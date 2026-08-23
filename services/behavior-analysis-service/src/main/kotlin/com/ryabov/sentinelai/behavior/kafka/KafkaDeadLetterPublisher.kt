package com.ryabov.sentinelai.behavior.kafka

import com.ryabov.sentinelai.behavior.service.DeadLetterPublisher
import io.micronaut.context.annotation.Requires
import jakarta.inject.Named
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

@Singleton
@Requires(property = "kafka.enabled", value = "true", defaultValue = "true")
open class KafkaDeadLetterPublisher(
    private val kafkaClient: DeadLetterKafkaClient,
    @param:Named("io") private val ioDispatcher: CoroutineDispatcher
) : DeadLetterPublisher {

    override suspend fun publish(key: String, payload: String) {
        withContext(ioDispatcher) {
            kafkaClient.send(key, payload).get(2, TimeUnit.SECONDS)
        }
    }
}
