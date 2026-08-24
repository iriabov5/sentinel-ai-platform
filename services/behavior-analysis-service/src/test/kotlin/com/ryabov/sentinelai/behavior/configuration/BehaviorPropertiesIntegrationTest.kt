package com.ryabov.sentinelai.behavior.configuration

import com.ryabov.sentinelai.behavior.model.SecurityEventType
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Duration

@MicronautTest
@DisplayName("Конфигурация behavior-analysis-service")
class BehaviorPropertiesIntegrationTest {

    @Inject
    lateinit var kafkaProperties: BehaviorKafkaProperties

    @Inject
    lateinit var mongoProperties: BehaviorMongoProperties

    @Inject
    lateinit var featureProperties: FeatureProperties

    @Test
    @DisplayName("Биндит safe defaults из application.yml")
    fun `binds safe defaults`() {
        assertEquals("security.events.raw", kafkaProperties.topics.raw)
        assertEquals("security.events.raw.dlq", kafkaProperties.topics.dlq)
        assertEquals(3, kafkaProperties.processingRetries)
        assertEquals("behavior_analysis", mongoProperties.database)
        assertEquals("event_history", mongoProperties.collection)
    }

    @Test
    @DisplayName("Биндит safe defaults фич из application.yml")
    fun `binds feature defaults`() {
        assertEquals(Duration.ofHours(24), featureProperties.window)
        assertEquals(Duration.ofDays(30), featureProperties.baselineLookback)
        assertEquals(1, featureProperties.unusualTime.minEventsPerHour)
        assertEquals(
            listOf(SecurityEventType.FILE_DOWNLOAD, SecurityEventType.DATA_EXPORT),
            featureProperties.downloadVolume.eventTypes
        )
    }
}
