package com.example.chessclockapp.data

data class GameState(
    val player1: Player = Player(id = 1, timeLeft = 600f),
    val player2: Player = Player(id = 2, timeLeft = 600f),
    val isGamePaused: Boolean = true,
    val gameStatus: GameStatus = GameStatus.NOT_STARTED,
    val incrementSeconds: Int = 0,
    val delaySeconds: Int = 0
)
enum class GameStatus {
    NOT_STARTED,
    IN_PROGRESS,
    PAUSED,
    FINISHED
}