package com.magpiny.notafo.core

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.unit.sp

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
                // Check if this match should be processed based on its capture groups
                if (style.type == MarkdownType.LINK) {
                    val labelGroup = if (match.groups.size > 1) match.groups[1] else null
                    val urlGroup = if (match.groups.size > 2) match.groups[2] else null
                    
                    val labelRange = labelGroup?.range ?: return@forEach
                    val urlRange = urlGroup?.range ?: return@forEach
                    // For links, we hide [ ] and (url)
                    allMatches.add(MarkdownMatch(match.range, style, listOf(
                        match.range.first..match.range.first, // [
                        labelRange.last + 1..labelRange.last + 2, // ](
                        labelRange.last + 3..match.range.last // url)
                    )))
                } else if (style.type == MarkdownType.LIST_UNORDERED || style.type == MarkdownType.LIST_ORDERED) {
                    val markerGroup = if (match.groups.size > 1) match.groups[1] else null
                    val markerRange = markerGroup?.range ?: return@forEach
                    allMatches.add(MarkdownMatch(match.range, style, listOf(markerRange)))
                } else {
                    val contentGroup = if (match.groups.size > 1) match.groups[1] else null
                    val contentRange = contentGroup?.range ?: match.range
                    val markers = mutableListOf<IntRange>()
                    
                    // Only hide markers if explicitly requested for this style
                    if (style.hideMarkers) {
                        if (match.range.first < contentRange.first) {
                            markers.add(match.range.first until contentRange.first)
                        }
                        if (match.range.last > contentRange.last) {
                            markers.add(contentRange.last + 1..match.range.last)
                        }
                    }
                    
                    allMatches.add(MarkdownMatch(match.range, style, markers))
                }
            }
        }
        
        // Sort matches by start index to process them linearly
        allMatches.sortBy { it.range.first }
        
        val filteredMatches = mutableListOf<MarkdownMatch>()
        var lastEnd = -1
        allMatches.forEach { match ->
            if (match.range.first > lastEnd) {
                // Ensure the match content doesn't overlap with others
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
            
            val contentStart = match.range.first
            val contentEnd = match.range.last + 1
            
            val markers = match.markers
            val startStyle = currentTransformedIndex
            
            for (i in contentStart until contentEnd) {
                val isMarker = markers.any { i in it }
                if (isMarker) {
                    if (match.style.type == MarkdownType.LIST_UNORDERED) {
                        append("•")
                        originalToTransformed[currentOriginalIndex++] = currentTransformedIndex
                        transformedToOriginal.add(i)
                        currentTransformedIndex++
                    } else if (match.style.type == MarkdownType.LIST_ORDERED) {
                        // Keep the original number but style it
                        append(text[i])
                        originalToTransformed[currentOriginalIndex++] = currentTransformedIndex
                        transformedToOriginal.add(i)
                        currentTransformedIndex++
                    } else {
                        originalToTransformed[currentOriginalIndex++] = currentTransformedIndex
                    }
                } else {
                    append(text[i])
                    originalToTransformed[currentOriginalIndex++] = currentTransformedIndex
                    transformedToOriginal.add(i)
                    currentTransformedIndex++
                }
            }
            addStyle(match.style.style, startStyle, currentTransformedIndex)
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

private enum class MarkdownType {
    SPAN, BLOCK, LINK, LIST_UNORDERED, LIST_ORDERED
}

private data class MarkdownMatch(
    val range: IntRange,
    val style: MarkdownStyle,
    val markers: List<IntRange>
)

private data class MarkdownStyle(
    val regex: Regex,
    val style: SpanStyle,
    val type: MarkdownType = MarkdownType.SPAN,
    val hideMarkers: Boolean = true
)

private val styles = listOf(
    // Headers
    MarkdownStyle(Regex("(?m)^# (.*)"), SpanStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp), MarkdownType.BLOCK, hideMarkers = true),
    MarkdownStyle(Regex("(?m)^## (.*)"), SpanStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp), MarkdownType.BLOCK, hideMarkers = true),
    MarkdownStyle(Regex("(?m)^### (.*)"), SpanStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp), MarkdownType.BLOCK, hideMarkers = true),
    
    // Blockquotes
    MarkdownStyle(Regex("(?m)^> (.*)"), SpanStyle(color = Color.Gray, fontStyle = FontStyle.Italic), MarkdownType.BLOCK, hideMarkers = false),
    
    // Inline Styles
    MarkdownStyle(Regex("\\*\\*(.*?)\\*\\*"), SpanStyle(fontWeight = FontWeight.Bold)),
    MarkdownStyle(Regex("(?<!\\*)\\*(?!\\*)(.*?)(?<!\\*)\\*(?!\\*)"), SpanStyle(fontWeight = FontWeight.Bold)),
    MarkdownStyle(Regex("__(.*?)__"), SpanStyle(textDecoration = TextDecoration.Underline)),
    MarkdownStyle(Regex("_(.*?)_"), SpanStyle(fontStyle = FontStyle.Italic)),
    MarkdownStyle(Regex("~(.*?)~"), SpanStyle(textDecoration = TextDecoration.LineThrough)),
    MarkdownStyle(Regex("`(.*?)`"), SpanStyle(fontFamily = FontFamily.Monospace, background = Color.LightGray.copy(alpha = 0.2f))),
    
    // Links
    MarkdownStyle(Regex("\\[(.*?)\\]\\((.*?)\\)"), SpanStyle(color = Color(0xFF2196F3), textDecoration = TextDecoration.Underline), MarkdownType.LINK),
    
    // Lists
    MarkdownStyle(Regex("(?m)^([*\\+-]) (.*)"), SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF006064)), MarkdownType.LIST_UNORDERED, hideMarkers = true),
    MarkdownStyle(Regex("(?m)^(\\d+\\.) (.*)"), SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF006064)), MarkdownType.LIST_ORDERED, hideMarkers = true),
    
    // Code Blocks
    MarkdownStyle(Regex("(?s)(```.*?```)"), SpanStyle(fontFamily = FontFamily.Monospace, background = Color.LightGray.copy(alpha = 0.1f)), MarkdownType.BLOCK, hideMarkers = false),
    
    // Tables
    MarkdownStyle(Regex("(?m)^(\\|.*\\|)$"), SpanStyle(fontFamily = FontFamily.Monospace), MarkdownType.BLOCK, hideMarkers = false)
)

private fun applyAllStyles(text: String, builder: AnnotatedString.Builder) {
    styles.forEach { style ->
        style.regex.findAll(text).forEach { matchResult ->
            builder.addStyle(style.style, matchResult.range.first, matchResult.range.last + 1)
        }
    }
}
