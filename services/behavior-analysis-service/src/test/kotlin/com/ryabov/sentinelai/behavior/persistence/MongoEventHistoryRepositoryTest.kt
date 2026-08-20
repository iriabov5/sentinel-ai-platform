package com.ryabov.sentinelai.behavior.persistence

import com.mongodb.MongoWriteException
import com.mongodb.WriteError
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.ryabov.sentinelai.behavior.configuration.BehaviorMongoProperties
import com.ryabov.sentinelai.behavior.model.EventHistoryDocument
import com.ryabov.sentinelai.behavior.model.SecurityEventSource
import com.ryabov.sentinelai.behavior.model.SecurityEventSubject
import com.ryabov.sentinelai.behavior.model.SecurityEventType
import com.ryabov.sentinelai.behavior.model.SubjectType
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.bson.BsonDocument
import org.bson.Document
import org.bson.conversions.Bson
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant

@DisplayName("MongoDB event history repository")
class MongoEventHistoryRepositoryTest {

    @Test
    @DisplayName("Вставляет history document")
    fun `inserts history document`() = runBlocking {
        val collection = mockCollection()
        every { collection.insertOne(any()) } returns mockk()
        val repository = MongoEventHistoryRepository(mongoClient(collection), testProperties(), Dispatchers.Unconfined)

        repository.insertIgnoringDuplicateEventId(historyDocument())

        verify { collection.insertOne(any<Document>()) }
    }

    @Test
    @DisplayName("Игнорирует duplicate key по eventId")
    fun `ignores duplicate event id`() {
        val collection = mockCollection()
        every { collection.insertOne(any()) } throws duplicateKeyException()
        val repository = MongoEventHistoryRepository(mongoClient(collection), testProperties(), Dispatchers.Unconfined)

        assertDoesNotThrow {
            runBlocking {
                repository.insertIgnoringDuplicateEventId(historyDocument())
            }
        }
    }

    @Test
    @DisplayName("Пробрасывает unexpected Mongo write error")
    fun `rethrows unexpected write error`() {
        val collection = mockCollection()
        every { collection.insertOne(any()) } throws otherWriteException()
        val repository = MongoEventHistoryRepository(mongoClient(collection), testProperties(), Dispatchers.Unconfined)

        assertThrows(MongoWriteException::class.java) {
            runBlocking {
                repository.insertIgnoringDuplicateEventId(historyDocument())
            }
        }
    }

    private fun mongoClient(collection: MongoCollection<Document>): MongoClient {
        val database = mockk<MongoDatabase>()
        val client = mockk<MongoClient>()
        every { client.getDatabase("behavior_analysis") } returns database
        every { database.getCollection("event_history") } returns collection
        return client
    }

    private fun mockCollection(): MongoCollection<Document> {
        val collection = mockk<MongoCollection<Document>>()
        every { collection.createIndex(any<Bson>(), any()) } returns "idx"
        every { collection.createIndex(any<Bson>()) } returns "idx"
        return collection
    }

    private fun duplicateKeyException(): MongoWriteException =
        MongoWriteException(
            WriteError(11000, "duplicate", BsonDocument()),
            mockk(relaxed = true)
        )

    private fun otherWriteException(): MongoWriteException =
        MongoWriteException(
            WriteError(50, "timeout", BsonDocument()),
            mockk(relaxed = true)
        )

    private fun historyDocument(): EventHistoryDocument =
        EventHistoryDocument(
            eventId = "event-1",
            receivedAt = Instant.parse("2026-08-20T10:15:00Z"),
            eventType = SecurityEventType.LOGIN_FAILED,
            subject = SecurityEventSubject(type = SubjectType.USER, id = "user-123"),
            occurredAt = Instant.parse("2026-08-20T10:14:00Z"),
            source = SecurityEventSource(application = "billing-api"),
            metadata = mapOf("reason" to "INVALID_PASSWORD"),
            storedAt = Instant.parse("2026-08-20T10:16:00Z")
        )

    private fun testProperties(): BehaviorMongoProperties =
        object : BehaviorMongoProperties {
            override val database: String = "behavior_analysis"
            override val collection: String = "event_history"
        }
}
