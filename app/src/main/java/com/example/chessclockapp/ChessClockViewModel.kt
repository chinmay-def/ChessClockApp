package com.example.chessclockapp


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chessclockapp.data.GameEvent
import com.example.chessclockapp.data.GameState
import com.example.chessclockapp.data.GameStatus
import com.example.chessclockapp.data.Player
import com.example.chessclockapp.data.TimeControl
import com.example.chessclockapp.data.TimeControlSettings
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


class ChessClockViewModel : ViewModel() {
    private val _gameState = MutableStateFlow(GameState())
    val gameState = _gameState.asStateFlow()

    private var timer: Job? = null

    private val _timeSettings = MutableStateFlow(
        TimeControlSettings(
            selectedControl = defaultTimeControls.first(),
            predefinedControls = defaultTimeControls
        )
    )
    val timeSettings = _timeSettings.asStateFlow()

    fun handleEvent(event: GameEvent) {
        when (event) {
            is GameEvent.PlayerMove -> switchPlayer(event.playerId)
            is GameEvent.PauseGame -> pauseGame()
            is GameEvent.ResumeGame -> resumeGame()
            is GameEvent.ResetGame -> resetGame()
            is GameEvent.TimeUpdate -> updatePlayerTime(event.playerId, event.newTime)
        }
    }

    private fun switchPlayer(playerId: Int) {
        timer?.cancel()
        val increment = _timeSettings.value.selectedControl.incrementSeconds
        val currentState = _gameState.value
        val updatedState = when (playerId) {
            2 -> currentState.copy(player2 = currentState.player2.copy(timeLeft = currentState.player2.timeLeft + increment))
            1 -> currentState.copy(player1 = currentState.player1.copy(timeLeft = currentState.player1.timeLeft + increment))
            else -> currentState

        }
        val newState = when (playerId) {
            1 -> currentState.copy(
                player1 = updatedState.player1.copy(isActive = false),
                player2 = updatedState.player2.copy(isActive = true),
                gameStatus = GameStatus.IN_PROGRESS
            )

            2 -> currentState.copy(
                player1 = updatedState.player1.copy(isActive = true),
                player2 = updatedState.player2.copy(isActive = false),
                gameStatus = GameStatus.IN_PROGRESS
            )

            else -> currentState
        }

        _gameState.value = newState
        startTimer()
    }

    private fun startTimer() {
        timer?.cancel()

        timer = viewModelScope.launch {
            while (isActive) {
                delay(100L)
                val currentState = _gameState.value

                val activePlayer = when {
                    currentState.player1.isActive -> currentState.player1
                    currentState.player2.isActive -> currentState.player2
                    else -> null
                }

                activePlayer?.let { player ->
                    val newTime = (player.timeLeft - 0.1f).coerceAtLeast(0f)
                    handleEvent(GameEvent.TimeUpdate(player.id, newTime))

                    if (newTime <= 0f) {
                        _gameState.value = currentState.copy(gameStatus = GameStatus.FINISHED)
                        timer?.cancel()
                    }
                }
            }
        }
    }

    private fun updatePlayerTime(playerId: Int, newTime: Float) {
        val currentState = _gameState.value
        _gameState.value = when (playerId) {
            1 -> currentState.copy(
                player1 = currentState.player1.copy(timeLeft = newTime)
            )

            2 -> currentState.copy(
                player2 = currentState.player2.copy(timeLeft = newTime)
            )

            else -> currentState
        }
    }

    private fun pauseGame() {
        timer?.cancel()
        _gameState.value = _gameState.value.copy(
            gameStatus = GameStatus.PAUSED,
            isGamePaused = true
        )
    }

    private fun resumeGame() {
        _gameState.value = _gameState.value.copy(
            gameStatus = GameStatus.IN_PROGRESS,
            isGamePaused = false
        )
        startTimer()
    }


    companion object {
        val defaultTimeControls = listOf(
            TimeControl("Bullet", 1),
            TimeControl("Bullet+1", 1, incrementSeconds = 1),
            TimeControl("Blitz 3+0", 3),
            TimeControl("Blitz 3+2", 3, incrementSeconds = 2),
            TimeControl("Rapid 10+0", 10),
            TimeControl("Rapid 15+10", 15, incrementSeconds = 10),
            TimeControl("Classical 30+0", 30),
            TimeControl("Custom", 5)
        )
    }

    fun updateTimeControl(timeControl: TimeControl) {
        viewModelScope.launch {
            _timeSettings.update {
                it.copy(
                    selectedControl = timeControl,
                    isCustom = false
                )
            }
            resetGame()
        }
    }

    fun setCustomTimeControl(minutes: Int, increment: Int = 0, delay: Int = 0) {
        viewModelScope.launch {
            val customControl = TimeControl(
                name = "Custom",
                initialTimeMinutes = minutes,
                incrementSeconds = increment,
                delaySeconds = delay
            )
            _timeSettings.update {
                it.copy(
                    selectedControl = customControl,
                    isCustom = true
                )
            }
            resetGame()
        }
    }
// Reset the game
    private fun resetGame() {
        val selectedControl = _timeSettings.value.selectedControl
        val initialTimeInSeconds = selectedControl.initialTimeMinutes * 60f

        _gameState.update {
            GameState(
                player1 = Player(1, initialTimeInSeconds),
                player2 = Player(2, initialTimeInSeconds),
                gameStatus = GameStatus.NOT_STARTED,
                isGamePaused = true,
                incrementSeconds = selectedControl.incrementSeconds,
                delaySeconds = selectedControl.delaySeconds
            )
        }
    }

}