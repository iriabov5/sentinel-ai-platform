package com.ryabov.sentinelai.ingestion.configuration

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@MicronautTest
@DisplayName("Конфигурация metadata limits")
class IngestionMetadataPropertiesIntegrationTest {

    @Inject
    lateinit var properties: IngestionMetadataProperties

    @Test
    @DisplayName("Биндит safe defaults из application.yml")
    fun `binds safe defaults from application yml`() {
        assertEquals(25, properties.maxEntries)
        assertEquals(64, properties.maxKeyLength)
        assertEquals(512, properties.maxValueLength)
    }
}
