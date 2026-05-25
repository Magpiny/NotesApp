package com.example.notesapp.core

import android.content.Context
import android.content.Intent

fun shareNote(context: Context, title: String, content: String) {
    val shareText = if (title.isNotBlank()) {
        "$title\n\n$content"
    } else {
        content
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, title)
        putExtra(Intent.EXTRA_TEXT, shareText)
    }

    context.startActivity(Intent.createChooser(intent, "Share Note"))
}
