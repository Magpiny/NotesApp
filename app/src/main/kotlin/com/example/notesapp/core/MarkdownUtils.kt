package com.example.notesapp.core

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.input.OffsetMapping

data class MarkdownParseResult(
    val annotatedString: AnnotatedString,
    val offsetMapping: OffsetMapping
)

/**
 * Parses raw text and returns an [AnnotatedString] with Markdown styling.
 * If [stripMarkers] is true, formatting characters are removed from the result.
 */
fun parseMarkdown(text: String, stripMarkers: Boolean = false): MarkdownParseResult {
    if (!stripMarkers) {
        val annotatedString = buildAnnotatedString {
            append(text)
            applyAllStyles(text, this)
        }
        return MarkdownParseResult(annotatedString, OffsetMapping.Identity)
    }

    var mapping: OffsetMapping = OffsetMapping.Identity
    val annotatedString = buildAnnotatedString {
        val originalToTransformed = IntArray(text.length + 1)
        val transformedToOriginal = mutableListOf<Int>()
        
        var currentOriginalIndex = 0
        var currentTransformedIndex = 0

        // Find all matches for all styles
        val allMatches = mutableListOf<MarkdownMatch>()
        styles.forEach { style ->
            style.regex.findAll(text).forEach { match ->
                allMatches.add(MarkdownMatch(match.range, style.style, style.tokenLength))
            }
        }
        
        // Sort matches by start index to process them linearly
        allMatches.sortBy { it.range.first }
        
        val filteredMatches = mutableListOf<MarkdownMatch>()
        var lastEnd = -1
        allMatches.forEach { match ->
            if (match.range.first > lastEnd) {
                filteredMatches.add(match)
                lastEnd = match.range.last
            }
        }

        filteredMatches.forEach { match ->
            // Append text before the match
            while (currentOriginalIndex < match.range.first) {
                append(text[currentOriginalIndex])
                originalToTransformed[currentOriginalIndex] = currentTransformedIndex
                transformedToOriginal.add(currentOriginalIndex)
                currentOriginalIndex++
                currentTransformedIndex++
            }
            
            // Map the start markers to the start of the styled text
            repeat(match.tokenLength) {
                originalToTransformed[currentOriginalIndex++] = currentTransformedIndex
            }
            
            // Append the content of the match and style it
            val startStyle = currentTransformedIndex
            val contentStart = match.range.first + match.tokenLength
            val contentEnd = match.range.last - match.tokenLength + 1
            
            for (i in contentStart until contentEnd) {
                append(text[i])
                originalToTransformed[currentOriginalIndex++] = currentTransformedIndex
                transformedToOriginal.add(i)
                currentTransformedIndex++
            }
            addStyle(match.style, startStyle, currentTransformedIndex)
            
            // Map the end markers to the end of the styled text
            repeat(match.tokenLength) {
                originalToTransformed[currentOriginalIndex++] = currentTransformedIndex
            }
        }
        
        // Append remaining text
        while (currentOriginalIndex < text.length) {
            append(text[currentOriginalIndex])
            originalToTransformed[currentOriginalIndex] = currentTransformedIndex
            transformedToOriginal.add(currentOriginalIndex)
            currentOriginalIndex++
            currentTransformedIndex++
        }
        
        originalToTransformed[text.length] = currentTransformedIndex
        transformedToOriginal.add(text.length)

        mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int = originalToTransformed[offset.coerceIn(0, text.length)]
            override fun transformedToOriginal(offset: Int): Int = transformedToOriginal[offset.coerceIn(0, transformedToOriginal.size - 1)]
        }
    }
    
    return MarkdownParseResult(annotatedString, mapping)
}

private data class MarkdownMatch(
    val range: IntRange,
    val style: SpanStyle,
    val tokenLength: Int
)

private data class MarkdownStyle(
    val regex: Regex,
    val style: SpanStyle,
    val tokenLength: Int
)

private val styles = listOf(
    MarkdownStyle(Regex("\\*\\*(.*?)\\*\\*"), SpanStyle(fontWeight = FontWeight.Bold), 2),
    MarkdownStyle(Regex("(?<!\\*)\\*(?!\\*)(.*?)(?<!\\*)\\*(?!\\*)"), SpanStyle(fontWeight = FontWeight.Bold), 1),
    MarkdownStyle(Regex("__(.*?)__"), SpanStyle(textDecoration = TextDecoration.Underline), 2),
    MarkdownStyle(Regex("_(.*?)_"), SpanStyle(fontStyle = FontStyle.Italic), 1),
    MarkdownStyle(Regex("~(.*?)~"), SpanStyle(textDecoration = TextDecoration.LineThrough), 1),
    MarkdownStyle(Regex("`(.*?)`"), SpanStyle(fontFamily = FontFamily.Monospace, background = Color.LightGray.copy(alpha = 0.2f)), 1),
    MarkdownStyle(Regex("- \\[ \\] (.*)"), SpanStyle(color = Color.Gray), 6),
    MarkdownStyle(Regex("- \\[x] (.*)"), SpanStyle(color = Color.Gray, textDecoration = TextDecoration.LineThrough), 6)
)

private fun applyAllStyles(text: String, builder: AnnotatedString.Builder) {
    styles.forEach { style ->
        style.regex.findAll(text).forEach { matchResult ->
            builder.addStyle(style.style, matchResult.range.first, matchResult.range.last + 1)
        }
    }
}
