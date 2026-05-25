package com.example.notesapp.core

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Formats a timestamp into a beautiful human-readable string.
 * Example: "Oct 24, 14:30"
 */
fun Long.formatToReadableDate(): String {
    val date = Date(this)
    val formatter = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
    return formatter.format(date)
}
