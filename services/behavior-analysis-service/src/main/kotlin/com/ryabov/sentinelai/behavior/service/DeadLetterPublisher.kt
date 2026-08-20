package com.ryabov.sentinelai.behavior.service

/**
 * Публикует unprocessable Kafka record в dead-letter topic.
 */
fun interface DeadLetterPublisher {

    suspend fun publish(key: String, payload: String)
}
