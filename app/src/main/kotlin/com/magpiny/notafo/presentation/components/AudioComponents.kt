package com.magpiny.notafo.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp

@Composable
fun WaveformVisualizer(
    amplitudes: List<Float>,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val barWidth = 4.dp.toPx()
        val gap = 2.dp.toPx()
        val maxBars = (width / (barWidth + gap)).toInt()
        
        val visibleAmplitudes = amplitudes.takeLast(maxBars)
        
        visibleAmplitudes.forEachIndexed { index, amplitude ->
            val x = width - (visibleAmplitudes.size - index) * (barWidth + gap)
            val barHeight = amplitude * height
            
            drawLine(
                color = color,
                start = Offset(x, (height - barHeight) / 2),
                end = Offset(x, (height + barHeight) / 2),
                strokeWidth = barWidth,
                cap = StrokeCap.Round
            )
        }
    }
}
