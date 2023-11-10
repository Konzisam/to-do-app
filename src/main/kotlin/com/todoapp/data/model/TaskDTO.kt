package com.todoapp.data.model

import com.todoapp.data.Priority
import java.time.LocalDateTime


data class TaskDTO(
    val id: Long = 0,
    val description: String = "",
    val isReminderSet: Boolean = false,
    val isTaskOpen: Boolean = true,
    val createdOn: LocalDateTime = LocalDateTime.now(),
    val priority: Priority = com.todoapp.data.Priority.LOW,
)