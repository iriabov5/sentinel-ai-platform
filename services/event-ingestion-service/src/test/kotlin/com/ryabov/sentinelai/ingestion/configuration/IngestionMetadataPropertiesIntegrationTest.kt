package com.ryabov.sentinelai.ingestion.configuration

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Duration

@MicronautTest
@DisplayName("Конфигурация metadata limits")
class IngestionMetadataPropertiesIntegrationTest {

    @Inject
    lateinit var properties: IngestionMetadataProperties

    @Inject
    lateinit var kafkaProperties: IngestionKafkaProperties

    @Test
    @DisplayName("Биндит safe defaults из application.yml")
    fun `binds safe defaults from application yml`() {
        assertEquals(25, properties.maxEntries)
        assertEquals(64, properties.maxKeyLength)
        assertEquals(512, properties.maxValueLength)
        assertEquals("security.events.raw", kafkaProperties.topic)
        assertEquals(Duration.ofSeconds(2), kafkaProperties.publishTimeout)
        assertEquals(3, kafkaProperties.retries)
    }
}
