package com.ryabov.sentinelai.ingestion.kafka

import com.ryabov.sentinelai.ingestion.model.SecurityEventAcceptedResponse
import com.ryabov.sentinelai.ingestion.model.SecurityEventRequest
import com.ryabov.sentinelai.ingestion.model.SecurityEventSource
import com.ryabov.sentinelai.ingestion.model.SecurityEventSubject
import com.ryabov.sentinelai.ingestion.model.SecurityEventType
import com.ryabov.sentinelai.ingestion.model.SubjectType
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import jakarta.inject.Inject
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.condition.EnabledIf
import org.testcontainers.DockerClientFactory
import org.testcontainers.kafka.KafkaContainer
import org.testcontainers.utility.DockerImageName
import java.time.Duration
import java.time.Instant
import java.util.Properties
import java.util.UUID

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnabledIf("dockerAvailable")
@DisplayName("Kafka integration: REST publish в security.events.raw")
class SecurityEventKafkaIntegrationTest : TestPropertyProvider {

    @Inject
    @field:Client("/")
    lateinit var client: HttpClient

    override fun getProperties(): MutableMap<String, String> {
        if (!kafka.isRunning) {
            kafka.start()
        }
        return mutableMapOf(
            "kafka.enabled" to "true",
            "kafka.bootstrap.servers" to kafka.bootstrapServers,
            "sentinel.kafka.topic" to TOPIC
        )
    }

    @Test
    @DisplayName("Публикует accepted event в Kafka с тем же eventId и key=subject.id")
    fun `accepted event is published to kafka`() {
        val response = client.toBlocking().exchange(
            HttpRequest.POST("/api/v1/events", validRequest()),
            SecurityEventAcceptedResponse::class.java
        )

        assertEquals(HttpStatus.ACCEPTED, response.status)
        val body = response.body()
        val record = pollFirstRecord()

        assertEquals("user-123", record.key())
        assertTrue(record.value().contains("\"eventId\":\"${body.eventId}\""))
        assertTrue(record.value().contains("\"eventType\":\"LOGIN_FAILED\""))
    }

    private fun pollFirstRecord() =
        KafkaConsumer<String, String>(consumerProperties()).use { consumer ->
            consumer.subscribe(listOf(TOPIC))
            val deadline = System.currentTimeMillis() + Duration.ofSeconds(20).toMillis()
            while (System.currentTimeMillis() < deadline) {
                val records = consumer.poll(Duration.ofMillis(500))
                if (!records.isEmpty) {
                    return@use records.first()
                }
            }
            throw AssertionError("Kafka record was not published to $TOPIC")
        }

    private fun consumerProperties(): Properties =
        Properties().apply {
            put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.bootstrapServers)
            put(ConsumerConfig.GROUP_ID_CONFIG, "ingestion-it-${UUID.randomUUID()}")
            put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
            put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
            put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
            put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
        }

    private fun validRequest(): SecurityEventRequest =
        SecurityEventRequest(
            eventType = SecurityEventType.LOGIN_FAILED,
            subject = SecurityEventSubject(type = SubjectType.USER, id = "user-123"),
            occurredAt = Instant.parse("2026-08-20T10:15:00Z"),
            source = SecurityEventSource(
                application = "billing-api",
                ip = "203.0.113.42",
                deviceId = "device-abc"
            ),
            metadata = mapOf("reason" to "INVALID_PASSWORD")
        )

    companion object {
        private const val TOPIC = "security.events.raw"
        private val kafka = KafkaContainer(DockerImageName.parse("apache/kafka:3.9.1"))

        @JvmStatic
        fun dockerAvailable(): Boolean = DockerClientFactory.instance().isDockerAvailable
    }
}
