package com.esp32.smartcar

import com.esp32.smartcar.domain.model.CarCommand
import com.esp32.smartcar.domain.model.VoiceCommandResult
import com.esp32.smartcar.domain.usecase.ParseVoiceCommandUseCase
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class VoiceCommandParserTest {

    private lateinit var parser: ParseVoiceCommandUseCase

    @Before
    fun setUp() {
        parser = ParseVoiceCommandUseCase()
    }

    @Test
    fun testEnglishVoiceCommands() {
        // Forward
        var res = parser.execute("go forward")
        assertTrue(res is VoiceCommandResult.Success)
        assertEquals(CarCommand.Forward, (res as VoiceCommandResult.Success).command)

        res = parser.execute("move forward please!")
        assertTrue(res is VoiceCommandResult.Success)
        assertEquals(CarCommand.Forward, (res as VoiceCommandResult.Success).command)

        // Backward
        res = parser.execute("go backward")
        assertTrue(res is VoiceCommandResult.Success)
        assertEquals(CarCommand.Backward, (res as VoiceCommandResult.Success).command)

        res = parser.execute("reverse")
        assertTrue(res is VoiceCommandResult.Success)
        assertEquals(CarCommand.Backward, (res as VoiceCommandResult.Success).command)

        // Left / Right
        res = parser.execute("turn left")
        assertTrue(res is VoiceCommandResult.Success)
        assertEquals(CarCommand.Left, (res as VoiceCommandResult.Success).command)

        res = parser.execute("turn right")
        assertTrue(res is VoiceCommandResult.Success)
        assertEquals(CarCommand.Right, (res as VoiceCommandResult.Success).command)

        // Stop
        res = parser.execute("stop the car")
        assertTrue(res is VoiceCommandResult.Success)
        assertEquals(CarCommand.Stop, (res as VoiceCommandResult.Success).command)

        // Emergency Stop
        res = parser.execute("emergency stop now")
        assertTrue(res is VoiceCommandResult.Success)
        assertEquals(CarCommand.EmergencyStop, (res as VoiceCommandResult.Success).command)
    }

    @Test
    fun testHindiVoiceCommands() {
        // Aage / Forward
        var res = parser.execute("aage jao")
        assertTrue(res is VoiceCommandResult.Success)
        assertEquals(CarCommand.Forward, (res as VoiceCommandResult.Success).command)

        res = parser.execute("aage chalo")
        assertTrue(res is VoiceCommandResult.Success)
        assertEquals(CarCommand.Forward, (res as VoiceCommandResult.Success).command)

        // Peeche / Backward
        res = parser.execute("peeche jao")
        assertTrue(res is VoiceCommandResult.Success)
        assertEquals(CarCommand.Backward, (res as VoiceCommandResult.Success).command)

        res = parser.execute("piche chalo")
        assertTrue(res is VoiceCommandResult.Success)
        assertEquals(CarCommand.Backward, (res as VoiceCommandResult.Success).command)

        // Baaye / Left
        res = parser.execute("baaye mudo")
        assertTrue(res is VoiceCommandResult.Success)
        assertEquals(CarCommand.Left, (res as VoiceCommandResult.Success).command)

        // Daaye / Right
        res = parser.execute("daaye jao")
        assertTrue(res is VoiceCommandResult.Success)
        assertEquals(CarCommand.Right, (res as VoiceCommandResult.Success).command)

        // Ruk / Stop
        res = parser.execute("ruk jao")
        assertTrue(res is VoiceCommandResult.Success)
        assertEquals(CarCommand.Stop, (res as VoiceCommandResult.Success).command)

        res = parser.execute("gaadi roko")
        assertTrue(res is VoiceCommandResult.Success)
        assertEquals(CarCommand.Stop, (res as VoiceCommandResult.Success).command)

        // Emergency / Turant Roko
        res = parser.execute("turant roko")
        assertTrue(res is VoiceCommandResult.Success)
        assertEquals(CarCommand.EmergencyStop, (res as VoiceCommandResult.Success).command)
    }

    @Test
    fun testUnrecognizedVoiceInput() {
        val res = parser.execute("what is the weather today in Delhi")
        assertTrue(res is VoiceCommandResult.Unrecognized)
    }
}
