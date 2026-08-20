package com.ryabov.sentinelai.behavior.persistence

import com.mongodb.ErrorCategory
import com.mongodb.MongoWriteException
import com.mongodb.client.MongoClient
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import com.ryabov.sentinelai.behavior.configuration.BehaviorMongoProperties
import com.ryabov.sentinelai.behavior.model.EventHistoryDocument
import com.ryabov.sentinelai.behavior.service.EventHistoryRepository
import io.micronaut.context.annotation.Requires
import jakarta.inject.Named
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.bson.Document

/**
 * MongoDB adapter для owned `event_history` collection.
 *
 * Unique index по `eventId` обеспечивает идемпотентность at-least-once consumer.
 * Blocking driver вызывается на injected IO dispatcher.
 */
@Singleton
@Requires(property = "sentinel.persistence", value = "mongo", defaultValue = "mongo")
open class MongoEventHistoryRepository(
    private val mongoClient: MongoClient,
    private val mongoProperties: BehaviorMongoProperties,
    @param:Named("io") private val ioDispatcher: CoroutineDispatcher
) : EventHistoryRepository {

    private val collection by lazy {
        val mongoCollection = mongoClient
            .getDatabase(mongoProperties.database)
            .getCollection(mongoProperties.collection)
        mongoCollection.createIndex(Indexes.ascending("eventId"), IndexOptions().unique(true))
        mongoCollection.createIndex(
            Indexes.compoundIndex(
                Indexes.ascending("subject.id"),
                Indexes.ascending("occurredAt")
            )
        )
        mongoCollection
    }

    override suspend fun insertIgnoringDuplicateEventId(document: EventHistoryDocument) {
        try {
            withContext(ioDispatcher) {
                collection.insertOne(document.toBson())
            }
        } catch (ex: MongoWriteException) {
            if (ex.error.category == ErrorCategory.DUPLICATE_KEY) {
                return
            }
            throw ex
        }
    }

    private fun EventHistoryDocument.toBson(): Document {
        val subjectDocument = Document()
            .append("type", subject.type.name)
            .append("id", subject.id)
        val sourceDocument = Document()
            .append("application", source.application)
            .append("ip", source.ip)
            .append("deviceId", source.deviceId)
            .append("endpoint", source.endpoint)
            .append("region", source.region)
        return Document()
            .append("eventId", eventId)
            .append("receivedAt", receivedAt)
            .append("eventType", eventType.name)
            .append("subject", subjectDocument)
            .append("occurredAt", occurredAt)
            .append("source", sourceDocument)
            .append("metadata", Document(metadata))
            .append("storedAt", storedAt)
    }
}
