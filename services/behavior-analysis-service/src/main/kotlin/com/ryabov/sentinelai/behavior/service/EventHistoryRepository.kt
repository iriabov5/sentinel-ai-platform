package com.ryabov.sentinelai.behavior.service

import com.ryabov.sentinelai.behavior.model.EventHistoryDocument

/**
 * Persistence boundary для owned event history.
 */
fun interface EventHistoryRepository {

    /**
     * Сохраняет document. Повторная вставка того же `eventId` считается успехом.
     */
    suspend fun insertIgnoringDuplicateEventId(document: EventHistoryDocument)
}
