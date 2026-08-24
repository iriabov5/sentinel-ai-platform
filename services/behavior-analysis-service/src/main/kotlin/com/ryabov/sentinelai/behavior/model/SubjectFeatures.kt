package com.ryabov.sentinelai.behavior.model

import io.micronaut.serde.annotation.Serdeable
import java.time.Instant

/**
 * On-demand behavioral features для одного subject.
 */
@Serdeable
data class SubjectFeatures(
    val subjectId: String,
    val computedAt: Instant,
    val window: FeatureWindow,
    val features: List<FeatureValue>
)

/**
 * Временное окно, за которое вычислены фичи: `[start, end)`.
 */
@Serdeable
data class FeatureWindow(
    val start: Instant,
    val end: Instant
)

/**
 * Одна вычисленная фича с объяснением результата.
 */
@Serdeable
data class FeatureValue(
    val name: String,
    val value: Double,
    val unit: String,
    val explanation: String
)

/**
 * Wire-имена фич, зафиксированные в контракте `behavioral-features`.
 */
enum class FeatureName(val wireName: String) {
    NEW_IP_DEVICE("new_ip_device"),
    UNUSUAL_TIME("unusual_time"),
    REQUEST_RATE("request_rate"),
    DOWNLOAD_VOLUME("download_volume")
}
