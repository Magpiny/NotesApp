package com.magpiny.notafo.core

import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * A [VisualTransformation] that applies Markdown styling in real-time.
 */
class MarkdownVisualTransformation : VisualTransformation {
    override fun filter(text: androidx.compose.ui.text.AnnotatedString): TransformedText {
        val result = parseMarkdown(text.text, stripMarkers = true)
        return TransformedText(
            text = result.annotatedString,
            offsetMapping = result.offsetMapping
        )
    }
}
