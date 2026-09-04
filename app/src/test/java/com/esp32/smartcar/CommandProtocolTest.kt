package com.esp32.smartcar

import com.esp32.smartcar.domain.model.CarCommand
import org.junit.Assert.*
import org.junit.Test

class CommandProtocolTest {

    @Test
    fun testCommandFormatting() {
        assertEquals("F\n", CarCommand.Forward.code)
        assertEquals("B\n", CarCommand.Backward.code)
        assertEquals("L\n", CarCommand.Left.code)
        assertEquals("R\n", CarCommand.Right.code)
        assertEquals("S\n", CarCommand.Stop.code)
        assertEquals("X\n", CarCommand.EmergencyStop.code)
        assertEquals("H\n", CarCommand.Heartbeat.code)
        assertEquals("V200\n", CarCommand.SetSpeed(200).code)
        assertEquals("V0\n", CarCommand.SetSpeed(-10).code)
        assertEquals("V255\n", CarCommand.SetSpeed(300).code)
    }

    @Test
    fun testCommandParsing() {
        assertEquals(CarCommand.Forward, CarCommand.fromCode("F"))
        assertEquals(CarCommand.Forward, CarCommand.fromCode("F\n"))
        assertEquals(CarCommand.Backward, CarCommand.fromCode("B"))
        assertEquals(CarCommand.Left, CarCommand.fromCode("L"))
        assertEquals(CarCommand.Right, CarCommand.fromCode("R"))
        assertEquals(CarCommand.Stop, CarCommand.fromCode("S"))
        assertEquals(CarCommand.EmergencyStop, CarCommand.fromCode("X"))
        assertEquals(CarCommand.Heartbeat, CarCommand.fromCode("H"))
        assertEquals(CarCommand.SetSpeed(150), CarCommand.fromCode("V150"))
        assertNull(CarCommand.fromCode("INVALID_CMD"))
    }
}
