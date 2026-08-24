package com.ryabov.sentinelai.behavior.controller

import com.ryabov.sentinelai.behavior.model.FeatureName
import com.ryabov.sentinelai.behavior.model.SubjectFeatures
import com.ryabov.sentinelai.behavior.persistence.InMemoryEventHistoryQueryRepository
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@MicronautTest
@DisplayName("Subject features REST endpoint")
class SubjectFeaturesControllerTest {

    @Inject
    @field:Client("/")
    lateinit var httpClient: HttpClient

    @Inject
    lateinit var queryRepository: InMemoryEventHistoryQueryRepository

    @BeforeEach
    fun setUp() {
        queryRepository.reset()
    }

    @Test
    @DisplayName("Возвращает фичи с нулевыми значениями для subject без history")
    fun `returns zero features for subject without history`() {
        val response = httpClient.toBlocking().exchange(
            HttpRequest.GET<Any>("/api/v1/subjects/user-123/features"),
            SubjectFeatures::class.java
        )

        assertEquals(HttpStatus.OK, response.status)
        val body = response.body()
        assertEquals("user-123", body.subjectId)
        assertEquals(FeatureName.entries.size, body.features.size)
        body.features.forEach { assertEquals(0.0, it.value) }
    }

    @Test
    @DisplayName("Возвращает 503 при сбое чтения history")
    fun `returns 503 when history read fails`() {
        queryRepository.failNext(1)

        val exception = assertThrows(HttpClientResponseException::class.java) {
            httpClient.toBlocking().exchange(
                HttpRequest.GET<Any>("/api/v1/subjects/user-123/features"),
                String::class.java
            )
        }

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.response.status)
    }

    @Test
    @DisplayName("Возвращает 400 для пустого subject id")
    fun `returns 400 for blank subject id`() {
        val exception = assertThrows(HttpClientResponseException::class.java) {
            httpClient.toBlocking().exchange(
                HttpRequest.GET<Any>("/api/v1/subjects/%20/features"),
                String::class.java
            )
        }

        assertEquals(HttpStatus.BAD_REQUEST, exception.response.status)
    }
}
