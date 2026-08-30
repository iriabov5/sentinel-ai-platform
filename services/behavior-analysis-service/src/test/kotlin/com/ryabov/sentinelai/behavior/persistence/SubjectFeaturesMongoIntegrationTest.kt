package com.ryabov.sentinelai.behavior.persistence

import com.ryabov.sentinelai.behavior.model.EventHistoryDocument
import com.ryabov.sentinelai.behavior.model.SecurityEventSource
import com.ryabov.sentinelai.behavior.model.SecurityEventSubject
import com.ryabov.sentinelai.behavior.model.SecurityEventType
import com.ryabov.sentinelai.behavior.model.SubjectFeatures
import com.ryabov.sentinelai.behavior.model.SubjectType
import com.ryabov.sentinelai.behavior.service.EventHistoryRepository
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import jakarta.inject.Inject
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.condition.EnabledIf
import org.testcontainers.DockerClientFactory
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.utility.DockerImageName
import java.time.Duration
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnabledIf("dockerAvailable")
@DisplayName("Subject features integration: MongoDB history")
class SubjectFeaturesMongoIntegrationTest : TestPropertyProvider {
    @Inject
    @field:Client("/")
    lateinit var httpClient: HttpClient

    @Inject
    lateinit var repository: EventHistoryRepository

    override fun getProperties(): MutableMap<String, String> {
        if (!mongo.isRunning) {
            mongo.start()
        }
        return mutableMapOf(
            "mongodb.uri" to mongo.connectionString,
            "sentinel.persistence" to "mongo",
            "sentinel.mongodb.database" to "behavior_analysis",
            "sentinel.mongodb.collection" to COLLECTION,
        )
    }

    @Test
    @DisplayName("Считает фичи по сохранённой MongoDB history")
    fun `computes features from stored history`() {
        val now = Instant.now()
        runTest {
            repository.insertIgnoringDuplicateEventId(
                event("b1", now.minus(Duration.ofDays(2)), ip = "10.0.0.1", deviceId = "dev-1"),
            )
            repository.insertIgnoringDuplicateEventId(
                event("w1", now.minus(Duration.ofHours(1)), ip = "10.0.0.1", deviceId = "dev-1"),
            )
            repository.insertIgnoringDuplicateEventId(
                event(
                    "w2",
                    now.minus(Duration.ofHours(2)),
                    ip = "203.0.113.7",
                    deviceId = "dev-2",
                    type = SecurityEventType.FILE_DOWNLOAD,
                    metadata = mapOf("bytes" to "100"),
                ),
            )
        }

        val response =
            httpClient.toBlocking().exchange(
                HttpRequest.GET<Any>("/api/v1/subjects/user-123/features"),
                SubjectFeatures::class.java,
            )

        assertEquals(HttpStatus.OK, response.status)
        val features = assertNotNull(response.body()).features.associateBy { it.name }
        assertEquals(2.0, requireNotNull(features["new_ip_device"]).value)
        assertEquals(100.0, requireNotNull(features["download_volume"]).value)
        assertTrue(requireNotNull(features["request_rate"]).value > 0.0)
    }

    private fun event(
        eventId: String,
        occurredAt: Instant,
        type: SecurityEventType = SecurityEventType.LOGIN_FAILED,
        ip: String? = null,
        deviceId: String? = null,
        metadata: Map<String, String> = emptyMap(),
    ): EventHistoryDocument =
        EventHistoryDocument(
            eventId = eventId,
            receivedAt = occurredAt,
            eventType = type,
            subject = SecurityEventSubject(SubjectType.USER, "user-123"),
            occurredAt = occurredAt,
            source = SecurityEventSource(application = "billing-api", ip = ip, deviceId = deviceId),
            metadata = metadata,
            storedAt = occurredAt,
        )

    companion object {
        private const val COLLECTION = "event_history"
        private val mongo = MongoDBContainer(DockerImageName.parse("mongo:7.0"))

        @JvmStatic
        fun dockerAvailable(): Boolean = DockerClientFactory.instance().isDockerAvailable
    }
}
