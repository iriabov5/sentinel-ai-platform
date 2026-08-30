package com.ryabov.sentinelai.behavior.persistence

import com.mongodb.client.FindIterable
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.ryabov.sentinelai.behavior.configuration.BehaviorMongoProperties
import com.ryabov.sentinelai.behavior.model.SecurityEventType
import com.ryabov.sentinelai.behavior.model.SubjectType
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.bson.Document
import org.bson.conversions.Bson
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.Date
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@DisplayName("MongoDB event history query repository")
class MongoEventHistoryQueryRepositoryTest {
    @Test
    @DisplayName("Читает documents subject в заданном window")
    fun `maps mongo documents to event history`() =
        runTest {
            val occurredAt = Instant.parse("2026-08-20T10:14:00Z")
            val collection =
                mockCollection(
                    listOf(
                        historyBson(
                            eventId = "event-1",
                            occurredAt = occurredAt,
                            metadata = Document("bytes", 100),
                        ),
                    ),
                )
            val repository =
                MongoEventHistoryQueryRepository(
                    mongoClient(collection),
                    testProperties(),
                    Dispatchers.Unconfined,
                )

            val documents =
                repository.findForSubject(
                    "user-123",
                    Instant.parse("2026-08-19T00:00:00Z"),
                    Instant.parse("2026-08-21T00:00:00Z"),
                )

            val document = documents.single()
            assertEquals("event-1", document.eventId)
            assertEquals(SecurityEventType.LOGIN_FAILED, document.eventType)
            assertEquals(SubjectType.USER, document.subject.type)
            assertEquals("user-123", document.subject.id)
            assertEquals("billing-api", document.source.application)
            assertEquals("10.0.0.1", document.source.ip)
            assertEquals(mapOf("bytes" to "100"), document.metadata)
        }

    @Test
    @DisplayName("Пустой metadata в Mongo даёт emptyMap без ошибки")
    fun `missing metadata becomes empty map`() =
        runTest {
            val occurredAt = Instant.parse("2026-08-20T10:14:00Z")
            val collection =
                mockCollection(
                    listOf(historyBson(eventId = "event-2", occurredAt = occurredAt, metadata = null)),
                )
            val repository =
                MongoEventHistoryQueryRepository(
                    mongoClient(collection),
                    testProperties(),
                    Dispatchers.Unconfined,
                )

            val documents =
                repository.findForSubject(
                    "user-123",
                    Instant.parse("2026-08-19T00:00:00Z"),
                    Instant.parse("2026-08-21T00:00:00Z"),
                )

            val document = documents.single()
            assertTrue(document.metadata.isEmpty())
            assertNull(document.source.deviceId)
        }

    private fun mongoClient(collection: MongoCollection<Document>): MongoClient {
        val database = mockk<MongoDatabase>()
        val client = mockk<MongoClient>()
        every { client.getDatabase("behavior_analysis") } returns database
        every { database.getCollection("event_history") } returns collection
        return client
    }

    private fun mockCollection(documents: List<Document>): MongoCollection<Document> {
        val collection = mockk<MongoCollection<Document>>()
        val findIterable = mockk<FindIterable<Document>>()
        every { collection.find(any<Bson>()) } returns findIterable
        every { findIterable.sort(any()) } returns findIterable
        every { findIterable.into(any<MutableList<Document>>()) } answers {
            firstArg<MutableList<Document>>().apply { addAll(documents) }
        }
        return collection
    }

    private fun historyBson(
        eventId: String,
        occurredAt: Instant,
        metadata: Document?,
    ): Document =
        Document()
            .append("eventId", eventId)
            .append("receivedAt", Date.from(occurredAt))
            .append("eventType", "LOGIN_FAILED")
            .append(
                "subject",
                Document()
                    .append("type", "USER")
                    .append("id", "user-123"),
            ).append("occurredAt", Date.from(occurredAt))
            .append(
                "source",
                Document()
                    .append("application", "billing-api")
                    .append("ip", "10.0.0.1")
                    .append("deviceId", null)
                    .append("endpoint", null)
                    .append("region", null),
            ).append("metadata", metadata)
            .append("storedAt", Date.from(occurredAt))

    private fun testProperties(): BehaviorMongoProperties =
        object : BehaviorMongoProperties {
            override val database: String = "behavior_analysis"
            override val collection: String = "event_history"
        }
}
