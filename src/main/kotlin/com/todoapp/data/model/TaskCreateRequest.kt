package com.todoapp.data.model

import com.todoapp.data.Priority
import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime

data class TaskCreateRequest(

    @NotBlank(message = "Task description cant be empty")
    val description: String = "",
    val isReminderSet: Boolean = false,
    val isTaskOpen: Boolean = true,

    @NotBlank(message = "Task created on cannot be empty")
    val createdOn: LocalDateTime = LocalDateTime.now(),
    val priority: Priority = com.todoapp.data.Priority.LOW,
)
