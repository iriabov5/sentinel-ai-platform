package com.ryabov.sentinelai.behavior.configuration

import com.ryabov.sentinelai.behavior.model.SecurityEventType
import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.core.bind.annotation.Bindable
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import java.time.Duration

/**
 * Runtime-настройки on-demand feature extraction.
 *
 * Window — окно, за которое считаются фичи. Baseline — глубина history до начала
 * window, на которой строится baseline для `new_ip_device` и `unusual_time`.
 * Значения длительностей приходят в ISO-8601 формате (например, `PT24H`,
 * `P30D`); положительность окон контролируется safe defaults.
 */
@ConfigurationProperties("sentinel.behavior.features")
interface FeatureProperties {
    @get:Bindable(defaultValue = "PT24H")
    val window: Duration

    @get:Bindable(defaultValue = "P30D")
    val baselineLookback: Duration

    val unusualTime: UnusualTime

    val downloadVolume: DownloadVolume

    /**
     * Параметры фичи `unusual_time`.
     */
    @ConfigurationProperties("unusual-time")
    interface UnusualTime {
        /**
         * Минимальное число событий в часе baseline, чтобы час считался
         * типичным для subject.
         */
        @get:Bindable(defaultValue = "1")
        @get:Positive
        val minEventsPerHour: Int
    }

    /**
     * Параметры фичи `download_volume`.
     */
    @ConfigurationProperties("download-volume")
    interface DownloadVolume {
        /**
         * Event types, участвующие в подсчёте download volume.
         */
        @get:Bindable(defaultValue = "FILE_DOWNLOAD,DATA_EXPORT")
        @get:Size(min = 1)
        val eventTypes: List<SecurityEventType>
    }
}
