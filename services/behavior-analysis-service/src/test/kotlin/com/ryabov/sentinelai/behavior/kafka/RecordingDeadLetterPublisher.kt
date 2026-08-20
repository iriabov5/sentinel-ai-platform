package com.ryabov.sentinelai.behavior.kafka

import com.ryabov.sentinelai.behavior.service.DeadLetterPublisher
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton
import java.util.concurrent.ConcurrentLinkedQueue

@Singleton
@Requires(property = "kafka.enabled", value = "false")
open class RecordingDeadLetterPublisher : DeadLetterPublisher {

    val published: ConcurrentLinkedQueue<Pair<String, String>> = ConcurrentLinkedQueue()

    @Volatile
    var shouldFail: Boolean = false

    override suspend fun publish(key: String, payload: String) {
        if (shouldFail) {
            throw IllegalStateException("DLQ unavailable")
        }
        published.add(key to payload)
    }

    fun reset() {
        published.clear()
        shouldFail = false
    }
}
