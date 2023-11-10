package com.todoapp.data.model

import com.todoapp.data.Priority
import java.time.LocalDateTime

data class TaskUpdateRequest(
    val description: String?,
    val isReminderSet: Boolean?,
    val isTaskOpen: Boolean?,
    val createdOn: LocalDateTime?,
    val priority: Priority?,
)
