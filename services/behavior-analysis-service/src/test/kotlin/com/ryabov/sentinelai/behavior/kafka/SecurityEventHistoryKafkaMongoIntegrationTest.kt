package com.ryabov.sentinelai.behavior.kafka

import com.mongodb.client.MongoClient
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import jakarta.inject.Inject
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.bson.Document
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.condition.EnabledIf
import org.testcontainers.DockerClientFactory
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.kafka.KafkaContainer
import org.testcontainers.utility.DockerImageName
import java.time.Duration
import java.util.Properties
import java.util.UUID

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnabledIf("dockerAvailable")
@DisplayName("Kafka/Mongo integration: consume, persist, DLQ")
class SecurityEventHistoryKafkaMongoIntegrationTest : TestPropertyProvider {

    @Inject
    lateinit var mongoClient: MongoClient

    override fun getProperties(): MutableMap<String, String> {
        if (!kafka.isRunning) {
            kafka.start()
        }
        if (!mongo.isRunning) {
            mongo.start()
        }
        return mutableMapOf(
            "kafka.enabled" to "true",
            "kafka.bootstrap.servers" to kafka.bootstrapServers,
            "kafka.consumers.default.auto-offset-reset" to "earliest",
            "kafka.consumers.behavior-analysis-service.auto-offset-reset" to "earliest",
            "mongodb.uri" to mongo.connectionString,
            "sentinel.persistence" to "mongo",
            "sentinel.kafka.topics.raw" to RAW_TOPIC,
            "sentinel.kafka.topics.dlq" to DLQ_TOPIC,
            "sentinel.mongodb.database" to "behavior_analysis",
            "sentinel.mongodb.collection" to COLLECTION
        )
    }

    @BeforeAll
    fun waitForKafkaListener() {
        Thread.sleep(3_000)
    }

    @Test
    @DisplayName("Сохраняет consumed event в MongoDB и идемпотентен по eventId")
    fun `persists consumed event and ignores duplicate eventId`() {
        val payload = acceptedEventJson("event-it-1")
        publish(RAW_TOPIC, "user-123", payload)
        publish(RAW_TOPIC, "user-123", payload)

        val documents = awaitDocuments { it.size == 1 }
        assertEquals("event-it-1", documents.first().getString("eventId"))
        assertEquals("LOGIN_FAILED", documents.first().getString("eventType"))
    }

    @Test
    @DisplayName("Отправляет poison payload в DLQ")
    fun `poison payload is published to dlq`() {
        publish(RAW_TOPIC, "user-123", "{not-json")

        val record = pollFirstRecord(DLQ_TOPIC)
        assertEquals("user-123", record.key())
        assertTrue(record.value().contains("{not-json"))
        assertTrue(record.value().contains(RAW_TOPIC))
    }

    private fun publish(topic: String, key: String, value: String) {
        KafkaProducer<String, String>(producerProperties()).use { producer ->
            producer.send(ProducerRecord(topic, key, value)).get()
            producer.flush()
        }
    }

    private fun awaitDocuments(predicate: (List<Document>) -> Boolean): List<Document> {
        val deadline = System.currentTimeMillis() + Duration.ofSeconds(20).toMillis()
        var last = emptyList<Document>()
        while (System.currentTimeMillis() < deadline) {
            last = collection().find().into(mutableListOf())
            if (predicate(last)) {
                return last
            }
            Thread.sleep(200)
        }
        throw AssertionError("MongoDB documents did not match expectation: $last")
    }

    private fun pollFirstRecord(topic: String) =
        KafkaConsumer<String, String>(consumerProperties()).use { consumer ->
            consumer.subscribe(listOf(topic))
            val deadline = System.currentTimeMillis() + Duration.ofSeconds(20).toMillis()
            while (System.currentTimeMillis() < deadline) {
                val records = consumer.poll(Duration.ofMillis(500))
                if (!records.isEmpty) {
                    return@use records.first()
                }
            }
            throw AssertionError("Kafka record was not published to $topic")
        }

    private fun collection() =
        mongoClient.getDatabase("behavior_analysis").getCollection(COLLECTION)

    private fun producerProperties(): Properties =
        Properties().apply {
            put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.bootstrapServers)
            put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
            put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
            put(ProducerConfig.ACKS_CONFIG, "all")
        }

    private fun consumerProperties(): Properties =
        Properties().apply {
            put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.bootstrapServers)
            put(ConsumerConfig.GROUP_ID_CONFIG, "behavior-it-${UUID.randomUUID()}")
            put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
            put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
            put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        }

    private fun acceptedEventJson(eventId: String): String =
        """{"eventId":"$eventId","receivedAt":"2026-08-20T10:15:00Z","eventType":"LOGIN_FAILED","subject":{"type":"USER","id":"user-123"},"occurredAt":"2026-08-20T10:14:00Z","source":{"application":"billing-api"},"metadata":{"reason":"INVALID_PASSWORD"}}"""

    companion object {
        private const val RAW_TOPIC = "security.events.raw"
        private const val DLQ_TOPIC = "security.events.raw.dlq"
        private const val COLLECTION = "event_history"
        private val kafka = KafkaContainer(DockerImageName.parse("apache/kafka:3.9.1"))
        private val mongo = MongoDBContainer(DockerImageName.parse("mongo:7.0"))

        @JvmStatic
        fun dockerAvailable(): Boolean = DockerClientFactory.instance().isDockerAvailable
    }
}
