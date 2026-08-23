package com.ryabov.sentinelai.behavior.configuration

import io.micronaut.context.annotation.Factory
import jakarta.inject.Named
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Точка конфигурации coroutine dispatcher для blocking Kafka и MongoDB IO.
 *
 * Dispatcher выносится в bean, чтобы adapters не хардкодили `Dispatchers.IO`.
 */
@Factory
class CoroutineDispatcherFactory {

    @Singleton
    @Named("io")
    fun ioDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
