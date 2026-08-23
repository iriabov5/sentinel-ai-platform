package com.ryabov.sentinelai.ingestion.configuration

import io.micronaut.context.annotation.Factory
import jakarta.inject.Named
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Точка конфигурации coroutine dispatcher для blocking IO.
 *
 * Dispatcher выносится в bean, чтобы Kafka producer не хардкодил
 * `Dispatchers.IO` в business/adapter коде.
 */
@Factory
class CoroutineDispatcherFactory {

    @Singleton
    @Named("io")
    fun ioDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
