package com.example.notesapp.core

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * Extension to calculate the best contrasting color (Black or White) for a background.
 */
fun Color.calculateOnColor(): Color {
    return if (this.luminance() > 0.5f) Color.Black else Color.White
}
