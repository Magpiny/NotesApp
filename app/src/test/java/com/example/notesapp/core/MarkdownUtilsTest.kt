package com.example.notesapp.core

import org.junit.Test
import org.junit.Assert.*

class MarkdownUtilsTest {
    @Test
    fun testParseMarkdownCodeBlock() {
        val text = "```cpp\nstd::println(\"Hello world\");\n```"
        val result = parseMarkdown(text, stripMarkers = true)
        assertNotNull(result)
    }

    @Test
    fun testParseMarkdownUnorderedListBullets() {
        val text = "* Item 1\n- Item 2\n+ Item 3"
        val result = parseMarkdown(text, stripMarkers = true)
        val transformedText = result.annotatedString.text
        // Checking for the bullet point in a way that avoids potential encoding issues in stdout
        assertTrue("Text should contain bullet", transformedText.contains("\u2022"))
    }
}
