package com.ryabov.sentinelai.behavior.configuration

import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.core.bind.annotation.Bindable
import jakarta.validation.constraints.NotBlank

/**
 * Runtime-настройки owned MongoDB storage для event history.
 */
@ConfigurationProperties("sentinel.mongodb")
interface BehaviorMongoProperties {

    @get:Bindable(defaultValue = "behavior_analysis")
    @get:NotBlank
    val database: String

    @get:Bindable(defaultValue = "event_history")
    @get:NotBlank
    val collection: String
}
