package com.example.notesapp.core

import com.example.notesapp.domain.model.TaskPriority
import java.util.*

data class QuickAddResult(
    val title: String,
    val priority: TaskPriority,
    val labels: List<String>,
    val dueDate: Long?
)

object NLPUtils {
    fun parseQuickAdd(input: String): QuickAddResult {
        var text = input
        var priority = TaskPriority.MEDIUM
        val labels = mutableListOf<String>()
        var dueDate: Long? = null

        // Parse Priority: !low, !medium, !high, !urgent
        val priorityMatch = Regex("!(low|medium|high|urgent)").find(text)
        priorityMatch?.let {
            priority = when (it.groupValues[1].lowercase()) {
                "low" -> TaskPriority.LOW
                "medium" -> TaskPriority.MEDIUM
                "high" -> TaskPriority.HIGH
                "urgent" -> TaskPriority.URGENT
                else -> TaskPriority.MEDIUM
            }
            text = text.replace(it.value, "").trim()
        }

        // Parse Labels: #labelname
        val labelMatches = Regex("#(\\w+)").findAll(text)
        labelMatches.forEach {
            labels.add(it.groupValues[1])
            text = text.replace(it.value, "").trim()
        }

        // Parse Dates: today, tomorrow
        val today = Calendar.getInstance()
        if (text.contains("today", ignoreCase = true)) {
            dueDate = today.timeInMillis
            text = text.replace("today", "", ignoreCase = true).trim()
        } else if (text.contains("tomorrow", ignoreCase = true)) {
            today.add(Calendar.DAY_OF_YEAR, 1)
            dueDate = today.timeInMillis
            text = text.replace("tomorrow", "", ignoreCase = true).trim()
        }

        // Clean up title
        val finalTitle = text.replace("\\s+".toRegex(), " ").trim()

        return QuickAddResult(
            title = if (finalTitle.isBlank()) "New Task" else finalTitle,
            priority = priority,
            labels = labels,
            dueDate = dueDate
        )
    }
}
