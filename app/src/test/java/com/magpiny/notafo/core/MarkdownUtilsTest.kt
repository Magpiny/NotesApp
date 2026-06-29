package com.magpiny.notafo.core

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test

class MarkdownUtilsTest {

    @Test
    fun `parseMarkdown should not crash on code blocks`() {
        val text = "```cpp\nstd::println(\"Hello world\");\n```"
        val result = parseMarkdown(text, stripMarkers = true)
        result.annotatedString.text.shouldContain("std::println")
    }

    @Test
    fun `parseMarkdown should render unordered list with bullets`() {
        val text = "* Item 1\n- Item 2"
        val result = parseMarkdown(text, stripMarkers = true)
        result.annotatedString.text.shouldContain("\u2022 Item 1")
        result.annotatedString.text.shouldContain("\u2022 Item 2")
    }

    @Test
    fun `parseMarkdown should handle ordered list`() {
        val text = "1. First\n2. Second"
        val result = parseMarkdown(text, stripMarkers = true)
        result.annotatedString.text.shouldContain("1. First")
        result.annotatedString.text.shouldContain("2. Second")
    }
}
