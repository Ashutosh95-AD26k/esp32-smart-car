package com.esp32.smartcar.domain.model

sealed class VoiceCommandResult {
    data class Success(
        val spokenText: String,
        val command: CarCommand,
        val actionName: String
    ) : VoiceCommandResult()

    data class Unrecognized(
        val spokenText: String
    ) : VoiceCommandResult()

    data class Error(
        val errorMessage: String
    ) : VoiceCommandResult()

    object Idle : VoiceCommandResult()
    object Listening : VoiceCommandResult()
    object Processing : VoiceCommandResult()
}
