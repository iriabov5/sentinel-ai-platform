package com.ryabov.sentinelai.behavior.service

/**
 * Выбрасывается, когда feature computation не может прочитать event history.
 * На HTTP-слое маппится в `503 Service Unavailable`.
 */
class FeaturesUnavailableException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)
