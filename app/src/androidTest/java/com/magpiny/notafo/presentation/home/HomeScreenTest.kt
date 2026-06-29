package com.magpiny.notafo.presentation.home

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.magpiny.notafo.domain.model.Note
import com.magpiny.notafo.presentation.theme.NotesAppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun noteCard_displaysTitleAndContent() {
        val note = Note(
            id = "1",
            title = "Test Title",
            content = "Test Content",
            color = 0xFFFFFFFF,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            isPinned = false,
            isTrashed = false,
            isArchived = false,
            isLocked = false,
            labels = emptyList(),
            notebookId = null,
            position = 0
        )

        composeTestRule.setContent {
            NotesAppTheme {
                NoteCard(
                    note = note,
                    onClick = {},
                    onCopy = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Test Title").assertExists()
        composeTestRule.onNodeWithText("Test Content").assertExists()
    }
}
