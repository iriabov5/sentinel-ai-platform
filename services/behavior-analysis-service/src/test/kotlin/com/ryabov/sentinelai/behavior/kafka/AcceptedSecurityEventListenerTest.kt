package com.ryabov.sentinelai.behavior.kafka

import com.ryabov.sentinelai.behavior.service.SecurityEventHistoryService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Kafka listener accepted security events")
class AcceptedSecurityEventListenerTest {

    @Test
    @DisplayName("Делегирует raw record в history service")
    fun `delegates record to history service`() {
        val historyService = mockk<SecurityEventHistoryService>()
        coEvery { historyService.handleRaw(any(), any()) } returns Unit

        AcceptedSecurityEventListener(historyService).receive("user-123", "{}")

        coVerify { historyService.handleRaw("user-123", "{}") }
    }
}
