package com.example.chessclockapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.Surface


import com.example.chessclockapp.ui.theme.ChessClockAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val viewModel : ChessClockViewModel by viewModels()
        super.onCreate(savedInstanceState)

        setContent {
            ChessClockAppTheme {


                    ChessClockScreen(viewModel)


            }
        }
    }
}

