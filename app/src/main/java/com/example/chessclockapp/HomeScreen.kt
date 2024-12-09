package com.example.chessclockapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.chessclockapp.data.GameEvent
import com.example.chessclockapp.data.GameStatus
import com.example.chessclockapp.data.Player
import com.example.chessclockapp.data.TimeControl
import com.example.chessclockapp.data.TimeControlSettings

@Composable
fun ChessClockScreen(viewModel: ChessClockViewModel) {
    val gameState by viewModel.gameState.collectAsState()
    val timeSettings by viewModel.timeSettings.collectAsState()

    var showSettings by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Player 2 Clock (Rotated 180 degrees)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .rotate(180f)
                .clip(RoundedCornerShape(16.dp))
        ) {
            PlayerClock(
                player = gameState.player2,
                isGamePaused = gameState.isGamePaused,
                onPlayerMove = { viewModel.handleEvent(GameEvent.PlayerMove(2)) }
            )
        }

        // Control buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {

            IconButton(onClick = { showSettings = true }) {
                Icon(Icons.Default.Settings, "Settings")
            }
            GameControls(
                gameStatus = gameState.gameStatus,
                onResetClick = { viewModel.handleEvent(GameEvent.ResetGame) },
                onPauseResumeClick = {
                    if (gameState.isGamePaused) {
                        viewModel.handleEvent(GameEvent.ResumeGame)
                    } else {
                        viewModel.handleEvent(GameEvent.PauseGame)
                    }
                }
            )
        }

        // Player 1 Clock
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
        ) {
            PlayerClock(
                player = gameState.player1,
                isGamePaused = gameState.isGamePaused,
                onPlayerMove = { viewModel.handleEvent(GameEvent.PlayerMove(1)) }
            )
        }
        if (showSettings) {
            TimeSettingsDialog(
                currentSettings = timeSettings,
                onDismiss = { showSettings = false },
                onTimeControlSelected = { viewModel.updateTimeControl(it) },
                onCustomTimeSet = { minutes, increment, delay ->
                    viewModel.setCustomTimeControl(minutes, increment, delay)
                }
            )
        }

    }
}

@Composable
fun TimeSettingsDialog(
    currentSettings: TimeControlSettings,
    onDismiss: () -> Unit,
    onTimeControlSelected: (TimeControl) -> Unit,
    onCustomTimeSet: (minutes: Int, increment: Int, delay: Int) -> Unit
) {
    var showCustomDialog by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Time Control Settings") },
        text = {
            LazyColumn {
                items(currentSettings.predefinedControls) { timeControl ->
                    TimeControlItem(
                        timeControl = timeControl,
                        isSelected = timeControl == currentSettings.selectedControl,
                        onClick = {
                            if (timeControl.name == "Custom") {
                                showCustomDialog = true
                            } else {
                                onTimeControlSelected(timeControl)
                                onDismiss()
                            }
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )

    if (showCustomDialog) {
        CustomTimeControlDialog(
            onDismiss = { showCustomDialog = false },
            onConfirm = { minutes, increment, delay ->
                onCustomTimeSet(minutes, increment, delay)
                showCustomDialog = false
                onDismiss()
            }
        )
    }
}

@Composable
fun TimeControlItem(
    timeControl: TimeControl,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = timeControl.name,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = buildString {
                    append("${timeControl.initialTimeMinutes} min")
                    if (timeControl.incrementSeconds > 0) {
                        append(" + ${timeControl.incrementSeconds}s")
                    }
                    if (timeControl.delaySeconds > 0) {
                        append(" (${timeControl.delaySeconds}s delay)")
                    }
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        if (isSelected) {
            Icon(
                Icons.Default.Check,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun CustomTimeControlDialog(
    onDismiss: () -> Unit,
    onConfirm: (minutes: Int, increment: Int, delay: Int) -> Unit
) {
    var minutes by remember { mutableStateOf("5") }
    var increment by remember { mutableStateOf("0") }
    var delay by remember { mutableStateOf("0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Custom Time Control") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = minutes,
                    onValueChange = { minutes = it.filter { it.isDigit() } },
                    label = { Text("Minutes per player") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = increment,
                    onValueChange = { increment = it.filter { it.isDigit() } },
                    label = { Text("Increment (seconds)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = delay,
                    onValueChange = { delay = it.filter { it.isDigit() } },
                    label = { Text("Delay (seconds)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        minutes.toIntOrNull() ?: 5,
                        increment.toIntOrNull() ?: 0,
                        delay.toIntOrNull() ?: 0
                    )
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun PlayerClock(
    player: Player,
    isGamePaused: Boolean,
    onPlayerMove: () -> Unit
) {
    val backgroundColor = when {
        !player.isActive -> Color.LightGray
        isGamePaused -> Color.Gray
        else -> Color.Green.copy(alpha = 0.3f)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .clickable(enabled = !isGamePaused, onClick = onPlayerMove)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = formatTime(player.timeLeft),
            style = MaterialTheme.typography.displayLarge
        )
    }
}

@Composable
fun GameControls(
    gameStatus: GameStatus,
    onResetClick: () -> Unit,
    onPauseResumeClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {

        IconButton(onClick = onResetClick) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null
            )
        }
        Button(onClick = onPauseResumeClick) {
            Text(
                when (gameStatus) {
                    GameStatus.NOT_STARTED -> "Start"
                    GameStatus.PAUSED -> "Resume"
                    GameStatus.IN_PROGRESS -> "Pause"
                    GameStatus.FINISHED -> "New Game"
                }
            )
        }
    }
}

fun formatTime(timeInSeconds: Float): String {
    val minutes = (timeInSeconds / 60).toInt()
    val seconds = (timeInSeconds % 60).toInt()
    val tenths = ((timeInSeconds * 10) % 10).toInt()
    return "%02d:%02d.%01d".format(minutes, seconds, tenths)
}