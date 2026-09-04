package com.esp32.smartcar

import com.esp32.smartcar.domain.model.ObstacleStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class SafetyLogicTest {

    @Test
    fun testObstacleStatusEvaluation() {
        // Safe (> 30 cm)
        assertEquals(ObstacleStatus.SAFE, ObstacleStatus.evaluate(45f, 30f, 20f, 10f))
        assertEquals(ObstacleStatus.SAFE, ObstacleStatus.evaluate(31f, 30f, 20f, 10f))

        // Warning (20 - 30 cm)
        assertEquals(ObstacleStatus.WARNING, ObstacleStatus.evaluate(30f, 30f, 20f, 10f))
        assertEquals(ObstacleStatus.WARNING, ObstacleStatus.evaluate(21f, 30f, 20f, 10f))

        // Blocked (<= 20 cm)
        assertEquals(ObstacleStatus.BLOCKED, ObstacleStatus.evaluate(20f, 30f, 20f, 10f))
        assertEquals(ObstacleStatus.BLOCKED, ObstacleStatus.evaluate(11f, 30f, 20f, 10f))

        // Emergency Stop (<= 10 cm)
        assertEquals(ObstacleStatus.EMERGENCY, ObstacleStatus.evaluate(10f, 30f, 20f, 10f))
        assertEquals(ObstacleStatus.EMERGENCY, ObstacleStatus.evaluate(5f, 30f, 20f, 10f))
    }
}
