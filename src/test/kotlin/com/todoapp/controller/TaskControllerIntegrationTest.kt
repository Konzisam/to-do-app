package com.todoapp.controller

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.todoapp.data.Priority
import com.todoapp.data.model.TaskCreateRequest
import com.todoapp.data.model.TaskDTO
import com.todoapp.data.model.TaskUpdateRequest
import com.todoapp.exception.TaskNotFoundException
import com.todoapp.service.TaskService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import java.time.LocalDateTime


@ExtendWith(SpringExtension::class)
@WebMvcTest(controllers = [TaskController::class])
class TaskControllerIntegrationTest (@Autowired private val mockMvc: MockMvc){

    @MockBean
    private lateinit var mockService: TaskService

    private val taskId: Long = 77

    val dummyTaskDTO = TaskDTO(
            77,
            "finish everything",
            true,
            true,
            LocalDateTime.now(),
            Priority.MEDIUM
    )
    private val mapper = jacksonObjectMapper()

    @BeforeEach
    fun setUp() {
        mapper.registerModule(JavaTimeModule())
    }

    @Test
    fun `given all tasks endpoint is called then check for number of tasks`(){
        //Given
        val taskDTO = TaskDTO(22,
                "new desc",
                true,
                false,
                LocalDateTime.now(),
                Priority.HIGH
        )
         val tasks = listOf(dummyTaskDTO,taskDTO)

        // when
        `when`(mockService.getAllTasks()).thenReturn(tasks)

        val resultActions: ResultActions = mockMvc.perform(MockMvcRequestBuilders.get(
                "/api/all-tasks"))

        // then
        resultActions.andExpect(MockMvcResultMatchers.status().`is`(200))
        resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON))
        resultActions.andExpect(jsonPath("$.size()").value(2))
    }

    @Test
     fun `when task id does not exist then expect is not found response`(){
         `when`(mockService.getTaskById(taskId)).thenThrow(TaskNotFoundException(
                 "Task with id: $taskId does not exist"))

        val resultActions: ResultActions = mockMvc.perform(MockMvcRequestBuilders.get(
                "/api/task/$taskId"))

        resultActions.andExpect(MockMvcResultMatchers.status().isNotFound)
     }

    @Test
    fun `when get task by id is called with a character in the url then expect a bad request message`()
    {
        val resultActions: ResultActions = mockMvc.perform(MockMvcRequestBuilders.get(
                "/api/task/53L"))

        resultActions.andExpect(MockMvcResultMatchers.status().isBadRequest)
    }


    @Test
    fun `given update task when task is updated the check for correct properties` (){
        val request = TaskUpdateRequest(
                dummyTaskDTO.description,
                dummyTaskDTO.isReminderSet,
                dummyTaskDTO.isTaskOpen,
                dummyTaskDTO.createdOn,
                dummyTaskDTO.priority
        )

        `when` (mockService.updateTask(dummyTaskDTO.id, request)).thenReturn(dummyTaskDTO)
        val resultActions: ResultActions = mockMvc.perform(MockMvcRequestBuilders
                .patch("/api/update/${dummyTaskDTO.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
        )

        resultActions.andExpect(MockMvcResultMatchers.status().isOk)
        resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON))
        resultActions.andExpect(jsonPath("$.description").value(dummyTaskDTO.description))
        resultActions.andExpect(jsonPath("$.isReminderSet").value(dummyTaskDTO.isReminderSet))
        resultActions.andExpect(jsonPath("$.isTaskOpen").value(dummyTaskDTO.isTaskOpen))
//        resultActions.andExpect(jsonPath("$.priority").value(dummyTaskDTO.priority))
    }

    @Test
    fun `given create task request when task is created then check for the properties`(){
        val request = TaskCreateRequest(
                dummyTaskDTO.description,
                dummyTaskDTO.isReminderSet,
                dummyTaskDTO.isTaskOpen,
                LocalDateTime.now(),
                dummyTaskDTO.priority
        )

        `when` (mockService.createTask(request)).thenReturn(dummyTaskDTO)

        val resultActions: ResultActions = mockMvc.perform(MockMvcRequestBuilders.post("/api/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
        )

        resultActions.andExpect(MockMvcResultMatchers.status().isOk)
        resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON))
        resultActions.andExpect(jsonPath("$.description").value(dummyTaskDTO.description))
        resultActions.andExpect(jsonPath("$.isReminderSet").value(dummyTaskDTO.isReminderSet))
        resultActions.andExpect(jsonPath("$.isTaskOpen").value(dummyTaskDTO.isTaskOpen))


    }

    @Test
    fun `given id for delete request when task is deleted then check for the response messsage`(){
        val expectedMessage = "Task with the ID: ${taskId} has been deleted."

        `when`(mockService.deleteTask(taskId)).thenReturn(expectedMessage)
        val resultActions: ResultActions = mockMvc.perform(MockMvcRequestBuilders.delete("/api/delete/$taskId"))


        resultActions.andExpect(MockMvcResultMatchers.status().`is`(200))
        resultActions.andExpect(content().string(expectedMessage))

    }












}