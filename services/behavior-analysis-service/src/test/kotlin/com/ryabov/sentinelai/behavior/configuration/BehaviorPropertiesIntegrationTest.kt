package com.ryabov.sentinelai.behavior.configuration

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@MicronautTest
@DisplayName("Конфигурация behavior-analysis-service")
class BehaviorPropertiesIntegrationTest {

    @Inject
    lateinit var kafkaProperties: BehaviorKafkaProperties

    @Inject
    lateinit var mongoProperties: BehaviorMongoProperties

    @Test
    @DisplayName("Биндит safe defaults из application.yml")
    fun `binds safe defaults`() {
        assertEquals("security.events.raw", kafkaProperties.topics.raw)
        assertEquals("security.events.raw.dlq", kafkaProperties.topics.dlq)
        assertEquals(3, kafkaProperties.processingRetries)
        assertEquals("behavior_analysis", mongoProperties.database)
        assertEquals("event_history", mongoProperties.collection)
    }
}
