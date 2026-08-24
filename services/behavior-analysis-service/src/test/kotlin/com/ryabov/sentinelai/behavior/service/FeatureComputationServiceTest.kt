package com.ryabov.sentinelai.behavior.service

import com.ryabov.sentinelai.behavior.configuration.FeatureProperties
import com.ryabov.sentinelai.behavior.model.EventHistoryDocument
import com.ryabov.sentinelai.behavior.model.FeatureName
import com.ryabov.sentinelai.behavior.model.SecurityEventSource
import com.ryabov.sentinelai.behavior.model.SecurityEventSubject
import com.ryabov.sentinelai.behavior.model.SecurityEventType
import com.ryabov.sentinelai.behavior.model.SubjectType
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

@DisplayName("Feature computation service")
class FeatureComputationServiceTest {

    private val now = Instant.parse("2026-08-20T12:00:00Z")

    @Test
    @DisplayName("Считает new_ip_device по IP/device, отсутствующим в baseline")
    fun `new_ip_device counts ips and devices unseen in baseline`() = runBlocking {
        val service = service(
            events = listOf(
                event("b1", now.minus(Duration.ofDays(2)), ip = "10.0.0.1", deviceId = "dev-1"),
                event("w1", now.minus(Duration.ofHours(1)), ip = "10.0.0.1", deviceId = "dev-1"),
                event("w2", now.minus(Duration.ofHours(2)), ip = "203.0.113.7", deviceId = "dev-2")
            )
        )

        val features = service.computeFeatures(SUBJECT, now).features
        val feature = features.first { it.name == FeatureName.NEW_IP_DEVICE.wireName }

        assertEquals(2.0, feature.value)
        assertTrue(feature.explanation.contains("203.0.113.7"))
        assertTrue(feature.explanation.contains("dev-2"))
    }

    @Test
    @DisplayName("unusual_time считает долю событий вне типичных часов")
    fun `unusual_time computes fraction of events outside typical hours`() = runBlocking {
        val service = service(
            minEventsPerHour = 1,
            events = listOf(
                event("b1", utc(9, 0, dayOffset = 1)),
                event("b2", utc(9, 30, dayOffset = 1)),
                event("w1", utc(9, 45)),
                event("w2", utc(3, 0))
            )
        )

        val features = service.computeFeatures(SUBJECT, now).features
        val feature = features.first { it.name == FeatureName.UNUSUAL_TIME.wireName }

        assertEquals(0.5, feature.value)
        assertTrue(feature.explanation.contains("typical hours: 9"))
    }

    @Test
    @DisplayName("request_rate считает события в час")
    fun `request_rate computes events per hour`() = runBlocking {
        val service = service(
            window = Duration.ofHours(2),
            events = (0 until 10).map { i ->
                event("w$i", now.minus(Duration.ofMinutes(((i + 1) * 10).toLong())))
            }
        )

        val features = service.computeFeatures(SUBJECT, now).features
        val feature = features.first { it.name == FeatureName.REQUEST_RATE.wireName }

        assertEquals(5.0, feature.value)
    }

    @Test
    @DisplayName("download_volume суммирует metadata.bytes download-событий, пропуская отсутствующие значения")
    fun `download_volume sums metadata bytes and ignores missing values`() = runBlocking {
        val service = service(
            events = listOf(
                event(
                    "w1",
                    now.minus(Duration.ofHours(1)),
                    type = SecurityEventType.FILE_DOWNLOAD,
                    metadata = mapOf("bytes" to "100")
                ),
                event(
                    "w2",
                    now.minus(Duration.ofHours(1)),
                    type = SecurityEventType.FILE_DOWNLOAD,
                    metadata = mapOf("fileName" to "report.pdf")
                ),
                event(
                    "w3",
                    now.minus(Duration.ofHours(1)),
                    type = SecurityEventType.DATA_EXPORT,
                    metadata = mapOf("bytes" to "50")
                ),
                event(
                    "w4",
                    now.minus(Duration.ofHours(1)),
                    type = SecurityEventType.LOGIN_FAILED,
                    metadata = mapOf("bytes" to "999")
                ),
                event(
                    "w5",
                    now.minus(Duration.ofHours(1)),
                    type = SecurityEventType.FILE_DOWNLOAD,
                    metadata = mapOf("bytes" to "not-a-number")
                )
            )
        )

        val features = service.computeFeatures(SUBJECT, now).features
        val feature = features.first { it.name == FeatureName.DOWNLOAD_VOLUME.wireName }

        assertEquals(150.0, feature.value)
    }

    @Test
    @DisplayName("Пустая history возвращает нулевые значения всех фич")
    fun `empty history returns zero feature values`() = runBlocking {
        val service = service(events = emptyList())

        val result = service.computeFeatures(SUBJECT, now)

        assertEquals(SUBJECT, result.subjectId)
        assertEquals(now, result.computedAt)
        assertEquals(FeatureName.entries.size, result.features.size)
        result.features.forEach { assertEquals(0.0, it.value) }
    }

    @Test
    @DisplayName("Сбой чтения history транслируется в FeaturesUnavailableException")
    fun `repository failure becomes FeaturesUnavailableException`() {
        val service = FeatureComputationService(
            queryRepository = EventHistoryQueryRepository { _, _, _ ->
                throw IllegalStateException("MongoDB unavailable")
            },
            featureProperties = properties()
        )

        assertThrows(FeaturesUnavailableException::class.java) {
            runBlocking { service.computeFeatures(SUBJECT, now) }
        }
    }

    private fun service(
        window: Duration = Duration.ofHours(24),
        minEventsPerHour: Int = 1,
        events: List<EventHistoryDocument>
    ): FeatureComputationService =
        FeatureComputationService(
            queryRepository = EventHistoryQueryRepository { subjectId, from, to ->
                events
                    .filter { it.subject.id == subjectId && it.occurredAt >= from && it.occurredAt < to }
                    .sortedBy { it.occurredAt }
            },
            featureProperties = properties(window, minEventsPerHour)
        )

    private fun properties(
        featureWindow: Duration = Duration.ofHours(24),
        minEvents: Int = 1
    ): FeatureProperties {
        val unusualTimeMock = mockk<FeatureProperties.UnusualTime> {
            every { minEventsPerHour } returns minEvents
        }
        val downloadVolumeMock = mockk<FeatureProperties.DownloadVolume> {
            every { eventTypes } returns listOf(
                SecurityEventType.FILE_DOWNLOAD,
                SecurityEventType.DATA_EXPORT
            )
        }
        return mockk {
            every { window } returns featureWindow
            every { baselineLookback } returns Duration.ofDays(30)
            every { unusualTime } returns unusualTimeMock
            every { downloadVolume } returns downloadVolumeMock
        }
    }

    private fun event(
        eventId: String,
        occurredAt: Instant,
        type: SecurityEventType = SecurityEventType.LOGIN_FAILED,
        ip: String? = null,
        deviceId: String? = null,
        metadata: Map<String, String> = emptyMap()
    ): EventHistoryDocument = EventHistoryDocument(
        eventId = eventId,
        receivedAt = occurredAt,
        eventType = type,
        subject = SecurityEventSubject(SubjectType.USER, SUBJECT),
        occurredAt = occurredAt,
        source = SecurityEventSource(application = "billing-api", ip = ip, deviceId = deviceId),
        metadata = metadata,
        storedAt = occurredAt
    )

    private fun utc(hour: Int, minute: Int, dayOffset: Long = 0): Instant =
        now.atZone(ZoneOffset.UTC).minusDays(dayOffset).withHour(hour).withMinute(minute).toInstant()

    companion object {
        private const val SUBJECT = "user-123"
    }
}
