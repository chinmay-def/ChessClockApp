package com.example.chessclockapp.data

import com.example.chessclockapp.ChessClockViewModel.Companion.defaultTimeControls

data class TimeControl(
    val name: String,
    val initialTimeMinutes: Int,
    val incrementSeconds: Int = 0,
    val delaySeconds: Int = 0
)

data class TimeControlSettings(
    val predefinedControls: List<TimeControl> = defaultTimeControls,
    val selectedControl: TimeControl,
    val isCustom: Boolean = false
)