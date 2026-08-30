package com.ryabov.sentinelai.behavior.persistence

import com.ryabov.sentinelai.behavior.model.EventHistoryDocument
import com.ryabov.sentinelai.behavior.service.EventHistoryQueryRepository
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton
import java.time.Instant
import java.util.concurrent.atomic.AtomicInteger

/**
 * In-memory double для unit/HTTP тестов: фильтрует зарегистрированные documents
 * по subject и временному диапазону, умеет эмулировать сбой чтения.
 */
@Singleton
@Requires(property = "sentinel.persistence", value = "memory")
class InMemoryEventHistoryQueryRepository : EventHistoryQueryRepository {
    val documents: MutableList<EventHistoryDocument> = mutableListOf()
    private val remainingFailures = AtomicInteger(0)

    override suspend fun findForSubject(
        subjectId: String,
        from: Instant,
        to: Instant,
    ): List<EventHistoryDocument> {
        if (remainingFailures.getAndUpdate { current -> if (current > 0) current - 1 else 0 } > 0) {
            error("MongoDB unavailable")
        }
        return documents
            .filter { it.subject.id == subjectId && it.occurredAt >= from && it.occurredAt < to }
            .sortedBy { it.occurredAt }
    }

    fun failNext(times: Int) {
        remainingFailures.set(times)
    }

    fun reset() {
        documents.clear()
        remainingFailures.set(0)
    }
}
