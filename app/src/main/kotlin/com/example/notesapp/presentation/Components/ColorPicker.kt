package com.example.notesapp.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.notesapp.core.calculateOnColor
import com.example.notesapp.core.dimensions

private val NoteColors = listOf(
    0xFFFFFFFF, 0xFFFFB4AB, 0xFFFFD8E4, 0xFFFFDCC1, 0xFFFFF0C6, 0xFFE5F5D0,
    0xFFC4EED0, 0xFFC2E7FF, 0xFFD0BCFF, 0xFFF2B8B5, 0xFFCCC2DC, 0xFF4A4458
)

/**
 * A horizontal scrolling list of 12 Material You tonal colors for notes.
 *
 * @param selectedColor The currently selected color as an ARGB Long.
 * @param onColorSelected Callback triggered when a new color is tapped.
 */
@Composable
fun ColorPicker(
    selectedColor: Long,
    onColorSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = MaterialTheme.dimensions.paddingMedium),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.dimensions.paddingSmall)
    ) {
        items(NoteColors) { colorInt ->
            val color = Color(colorInt.toInt())
            val isSelected = selectedColor == colorInt

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f),
                        shape = CircleShape
                    )
                    .clickable { onColorSelected(colorInt) },
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = color.calculateOnColor()
                    )
                }
            }
        }
    }
}
