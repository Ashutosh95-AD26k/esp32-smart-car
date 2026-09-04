package com.esp32.smartcar.domain.usecase

import com.esp32.smartcar.domain.model.CarCommand
import com.esp32.smartcar.domain.model.VoiceCommandResult
import java.util.Locale

class ParseVoiceCommandUseCase {

    fun execute(rawSpokenText: String): VoiceCommandResult {
        val normalized = normalizeText(rawSpokenText)
        if (normalized.isBlank()) {
            return VoiceCommandResult.Unrecognized(rawSpokenText)
        }

        // 1. Emergency Stop keywords (Highest Priority)
        if (matchesAny(normalized, listOf("emergency", "emergency stop", "danger", "turant roko", "aapatkaal"))) {
            return VoiceCommandResult.Success(
                spokenText = rawSpokenText,
                command = CarCommand.EmergencyStop,
                actionName = "EMERGENCY STOP"
            )
        }

        // 2. Stop keywords (English & Hindi)
        val stopKeywords = listOf(
            "stop", "stop car", "stop the car", "halt", "freeze", "brake",
            "ruk", "ruk jao", "ruko", "gaadi roko", "gadi roko", "roko", "thahar jao", "tham jao"
        )
        if (matchesAny(normalized, stopKeywords)) {
            return VoiceCommandResult.Success(
                spokenText = rawSpokenText,
                command = CarCommand.Stop,
                actionName = "STOP"
            )
        }

        // 3. Forward keywords (English & Hindi)
        val forwardKeywords = listOf(
            "forward", "go forward", "move forward", "straight", "ahead", "front", "drive forward",
            "aage", "aage jao", "aage chalo", "aage badho", "samne", "samne jao", "samne chalo"
        )
        if (matchesAny(normalized, forwardKeywords)) {
            return VoiceCommandResult.Success(
                spokenText = rawSpokenText,
                command = CarCommand.Forward,
                actionName = "FORWARD"
            )
        }

        // 4. Backward keywords (English & Hindi)
        val backwardKeywords = listOf(
            "back", "backward", "backwards", "go back", "go backward", "move back", "reverse", "drive back",
            "peeche", "peeche jao", "peeche chalo", "peeche lo", "piche", "piche jao", "piche chalo", "piche lo"
        )
        if (matchesAny(normalized, backwardKeywords)) {
            return VoiceCommandResult.Success(
                spokenText = rawSpokenText,
                command = CarCommand.Backward,
                actionName = "BACKWARD"
            )
        }

        // 5. Left keywords (English & Hindi)
        val leftKeywords = listOf(
            "left", "turn left", "go left", "take left", "move left",
            "baaye", "baaye jao", "baaye mudo", "baaye ghumo", "baye", "baye jao", "baye mudo", "baye ghumo", "ulta hath"
        )
        if (matchesAny(normalized, leftKeywords)) {
            return VoiceCommandResult.Success(
                spokenText = rawSpokenText,
                command = CarCommand.Left,
                actionName = "TURN LEFT"
            )
        }

        // 6. Right keywords (English & Hindi)
        val rightKeywords = listOf(
            "right", "turn right", "go right", "take right", "move right",
            "daaye", "daaye jao", "daaye mudo", "daaye ghumo", "daye", "daye jao", "daye mudo", "daye ghumo", "seedha hath"
        )
        if (matchesAny(normalized, rightKeywords)) {
            return VoiceCommandResult.Success(
                spokenText = rawSpokenText,
                command = CarCommand.Right,
                actionName = "TURN RIGHT"
            )
        }

        return VoiceCommandResult.Unrecognized(rawSpokenText)
    }

    private fun normalizeText(text: String): String {
        return text.lowercase(Locale.ROOT)
            .replace(Regex("[^a-zA-Z0-9\\s\\u0900-\\u097F]"), " ") // keep alphanumeric and Hindi Devanagari Unicode
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun matchesAny(input: String, keywords: List<String>): Boolean {
        for (keyword in keywords) {
            if (input == keyword || input.contains(keyword)) {
                return true
            }
        }
        return false
    }
}
