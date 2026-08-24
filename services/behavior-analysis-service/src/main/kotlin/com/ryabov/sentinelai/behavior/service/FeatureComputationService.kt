package com.ryabov.sentinelai.behavior.service

import com.ryabov.sentinelai.behavior.configuration.FeatureProperties
import com.ryabov.sentinelai.behavior.model.EventHistoryDocument
import com.ryabov.sentinelai.behavior.model.FeatureName
import com.ryabov.sentinelai.behavior.model.FeatureValue
import com.ryabov.sentinelai.behavior.model.FeatureWindow
import com.ryabov.sentinelai.behavior.model.SubjectFeatures
import jakarta.inject.Singleton
import java.time.Instant
import java.time.ZoneOffset

/**
 * On-demand вычисление explainable behavioral features по owned event history.
 *
 * Формулы (см. design.md Phase 4):
 * - `new_ip_device`: distinct IP/device в window, не встречавшиеся в baseline;
 * - `unusual_time`: доля событий window в часах, нехарактерных для subject;
 * - `request_rate`: события window / длительность window в часах;
 * - `download_volume`: сумма `metadata.bytes` download-type событий в window.
 *
 * Фичи считаются только в момент запроса; consume path их не вычисляет.
 * Сбой чтения history транслируется в [FeaturesUnavailableException] -> 503.
 */
@Singleton
open class FeatureComputationService(
    private val queryRepository: EventHistoryQueryRepository,
    private val featureProperties: FeatureProperties
) {

    /**
     * Считает фичи для subject по history на момент `now`.
     */
    suspend fun computeFeatures(subjectId: String, now: Instant = Instant.now()): SubjectFeatures {
        val windowEnd = now
        val windowStart = now.minus(featureProperties.window)
        val baselineStart = windowStart.minus(featureProperties.baselineLookback)
        val windowEvents = load(subjectId, windowStart, windowEnd)
        val baselineEvents = load(subjectId, baselineStart, windowStart)
        return SubjectFeatures(
            subjectId = subjectId,
            computedAt = now,
            window = FeatureWindow(windowStart, windowEnd),
            features = listOf(
                newIpDevice(windowEvents, baselineEvents),
                unusualTime(windowEvents, baselineEvents),
                requestRate(windowEvents),
                downloadVolume(windowEvents)
            )
        )
    }

    private suspend fun load(
        subjectId: String,
        from: Instant,
        to: Instant
    ): List<EventHistoryDocument> = try {
        queryRepository.findForSubject(subjectId, from, to)
    } catch (ex: Exception) {
        throw FeaturesUnavailableException(
            "Failed to read event history for subject '$subjectId' in [$from, $to)",
            ex
        )
    }

    private fun newIpDevice(
        windowEvents: List<EventHistoryDocument>,
        baselineEvents: List<EventHistoryDocument>
    ): FeatureValue {
        val baselineIps = baselineEvents.mapNotNull { it.source.ip }.toSet()
        val baselineDevices = baselineEvents.mapNotNull { it.source.deviceId }.toSet()
        val newIps = windowEvents
            .mapNotNull { it.source.ip }
            .distinct()
            .filterNot { it in baselineIps }
            .sorted()
        val newDevices = windowEvents
            .mapNotNull { it.source.deviceId }
            .distinct()
            .filterNot { it in baselineDevices }
            .sorted()
        val explanation = buildString {
            append(if (newIps.isEmpty()) "no new IPs" else "new IPs: ${newIps.joinToString()}")
            append("; ")
            append(if (newDevices.isEmpty()) "no new devices" else "new devices: ${newDevices.joinToString()}")
        }
        return FeatureValue(
            name = FeatureName.NEW_IP_DEVICE.wireName,
            value = (newIps.size + newDevices.size).toDouble(),
            unit = "count",
            explanation = explanation
        )
    }

    private fun unusualTime(
        windowEvents: List<EventHistoryDocument>,
        baselineEvents: List<EventHistoryDocument>
    ): FeatureValue {
        val minEventsPerHour = featureProperties.unusualTime.minEventsPerHour
        val typicalHours = baselineEvents
            .map { it.occurredAt.atZone(ZoneOffset.UTC).hour }
            .groupingBy { it }
            .eachCount()
            .filterValues { it >= minEventsPerHour }
            .keys
        val unusualCount = windowEvents.count {
            it.occurredAt.atZone(ZoneOffset.UTC).hour !in typicalHours
        }
        val value = if (windowEvents.isEmpty()) 0.0 else unusualCount.toDouble() / windowEvents.size
        val explanation = "typical hours: ${typicalHours.sorted().joinToString()}; " +
            "$unusualCount of ${windowEvents.size} events outside"
        return FeatureValue(
            name = FeatureName.UNUSUAL_TIME.wireName,
            value = value,
            unit = "fraction",
            explanation = explanation
        )
    }

    private fun requestRate(windowEvents: List<EventHistoryDocument>): FeatureValue {
        val windowHours = featureProperties.window.toMinutes().toDouble() / 60.0
        val value = if (windowHours > 0.0) windowEvents.size / windowHours else 0.0
        val explanation = "${windowEvents.size} events in ${windowLabel()} window"
        return FeatureValue(
            name = FeatureName.REQUEST_RATE.wireName,
            value = value,
            unit = "events/hour",
            explanation = explanation
        )
    }

    private fun downloadVolume(windowEvents: List<EventHistoryDocument>): FeatureValue {
        val downloadTypes = featureProperties.downloadVolume.eventTypes.toSet()
        val downloadEvents = windowEvents.filter { it.eventType in downloadTypes }
        val totalBytes = downloadEvents.sumOf { event ->
            event.metadata["bytes"]?.toLongOrNull() ?: 0L
        }
        val explanation = "downloaded $totalBytes bytes in ${downloadEvents.size} download events"
        return FeatureValue(
            name = FeatureName.DOWNLOAD_VOLUME.wireName,
            value = totalBytes.toDouble(),
            unit = "bytes",
            explanation = explanation
        )
    }

    private fun windowLabel(): String {
        val minutes = featureProperties.window.toMinutes()
        return if (minutes % 60 == 0L) "${minutes / 60}h" else "$minutes minutes"
    }
}
