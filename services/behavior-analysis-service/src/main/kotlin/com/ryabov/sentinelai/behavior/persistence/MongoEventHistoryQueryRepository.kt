package com.ryabov.sentinelai.behavior.persistence

import com.mongodb.client.MongoClient
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import com.ryabov.sentinelai.behavior.configuration.BehaviorMongoProperties
import com.ryabov.sentinelai.behavior.model.EventHistoryDocument
import com.ryabov.sentinelai.behavior.model.SecurityEventSource
import com.ryabov.sentinelai.behavior.model.SecurityEventSubject
import com.ryabov.sentinelai.behavior.model.SecurityEventType
import com.ryabov.sentinelai.behavior.model.SubjectType
import com.ryabov.sentinelai.behavior.service.EventHistoryQueryRepository
import io.micronaut.context.annotation.Requires
import jakarta.inject.Named
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.bson.Document
import java.time.Instant

/**
 * MongoDB adapter для чтения event history по subject и временному диапазону.
 *
 * Индексы создаёт write path (`MongoEventHistoryRepository`); здесь только
 * read-запросы по `subject.id + occurredAt`. Blocking driver вызывается на
 * injected IO dispatcher.
 */
@Singleton
@Requires(property = "sentinel.persistence", value = "mongo", defaultValue = "mongo")
open class MongoEventHistoryQueryRepository(
    private val mongoClient: MongoClient,
    private val mongoProperties: BehaviorMongoProperties,
    @param:Named("io") private val ioDispatcher: CoroutineDispatcher
) : EventHistoryQueryRepository {

    private val collection by lazy {
        mongoClient
            .getDatabase(mongoProperties.database)
            .getCollection(mongoProperties.collection)
    }

    override suspend fun findForSubject(
        subjectId: String,
        from: Instant,
        to: Instant
    ): List<EventHistoryDocument> = withContext(ioDispatcher) {
        collection.find(
            Filters.and(
                Filters.eq("subject.id", subjectId),
                Filters.gte("occurredAt", from),
                Filters.lt("occurredAt", to)
            )
        )
            .sort(Sorts.ascending("occurredAt"))
            .into(mutableListOf())
            .map { it.toEventHistoryDocument() }
    }

    private fun Document.toEventHistoryDocument(): EventHistoryDocument {
        val subjectDocument = get("subject", Document::class.java)
        val sourceDocument = get("source", Document::class.java)
        val metadataDocument = get("metadata", Document::class.java)
        return EventHistoryDocument(
            eventId = getString("eventId"),
            receivedAt = getDate("receivedAt").toInstant(),
            eventType = SecurityEventType.valueOf(getString("eventType")),
            subject = SecurityEventSubject(
                type = SubjectType.valueOf(subjectDocument.getString("type")),
                id = subjectDocument.getString("id")
            ),
            occurredAt = getDate("occurredAt").toInstant(),
            source = SecurityEventSource(
                application = sourceDocument.getString("application"),
                ip = sourceDocument.getString("ip"),
                deviceId = sourceDocument.getString("deviceId"),
                endpoint = sourceDocument.getString("endpoint"),
                region = sourceDocument.getString("region")
            ),
            metadata = metadataDocument
                ?.entries
                ?.associate { it.key to it.value.toString() }
                ?: emptyMap(),
            storedAt = getDate("storedAt").toInstant()
        )
    }
}
