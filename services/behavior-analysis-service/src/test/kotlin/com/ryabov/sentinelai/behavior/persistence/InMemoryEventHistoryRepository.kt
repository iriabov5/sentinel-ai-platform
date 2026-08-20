package com.ryabov.sentinelai.behavior.persistence

import com.ryabov.sentinelai.behavior.model.EventHistoryDocument
import com.ryabov.sentinelai.behavior.service.EventHistoryRepository
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@Singleton
@Requires(env = ["test"])
open class InMemoryEventHistoryRepository : EventHistoryRepository {

    val documents: ConcurrentHashMap<String, EventHistoryDocument> = ConcurrentHashMap()
    private val remainingFailures = AtomicInteger(0)

    override suspend fun insertIgnoringDuplicateEventId(document: EventHistoryDocument) {
        if (remainingFailures.getAndUpdate { current -> if (current > 0) current - 1 else 0 } > 0) {
            throw IllegalStateException("MongoDB unavailable")
        }
        documents.putIfAbsent(document.eventId, document)
    }

    fun failNext(times: Int) {
        remainingFailures.set(times)
    }

    fun reset() {
        documents.clear()
        remainingFailures.set(0)
    }
}
