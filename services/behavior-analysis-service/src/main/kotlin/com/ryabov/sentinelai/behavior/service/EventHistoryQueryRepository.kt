package com.ryabov.sentinelai.behavior.service

import com.ryabov.sentinelai.behavior.model.EventHistoryDocument
import java.time.Instant

/**
 * Read boundary для owned event history: выборка событий subject по временному
 * диапазону. Использует индекс `subject.id + occurredAt`.
 */
fun interface EventHistoryQueryRepository {

    /**
     * Возвращает события subject с `occurredAt` в диапазоне `[from, to)`,
     * отсортированные по времени возникновения.
     */
    suspend fun findForSubject(
        subjectId: String,
        from: Instant,
        to: Instant
    ): List<EventHistoryDocument>
}
