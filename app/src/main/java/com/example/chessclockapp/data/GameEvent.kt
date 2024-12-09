package com.example.chessclockapp.data

sealed class GameEvent {
    data class PlayerMove(val playerId: Int) : GameEvent()
    data object PauseGame : GameEvent()
    data object ResumeGame : GameEvent()
    data object ResetGame : GameEvent()
    data class TimeUpdate(val playerId: Int, val newTime: Float) : GameEvent()
}
