package com.todoapp.service

import com.todoapp.data.Priority
import com.todoapp.data.Task
import com.todoapp.data.model.TaskCreateRequest
import com.todoapp.data.model.TaskDTO
import com.todoapp.data.model.TaskUpdateRequest
import com.todoapp.exception.BadRequestException
import com.todoapp.exception.TaskNotFoundException
import com.todoapp.repositories.TaskRepository
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime

@ExtendWith(MockKExtension::class)
class TaskServiceTest {

    @RelaxedMockK
    private lateinit var mockRepository: TaskRepository


    @InjectMockKs
    private lateinit var objectUnderTest: TaskService

    private val task = Task()

    private lateinit var createRequest: TaskCreateRequest

    private val taskId : Long = 543;


    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        createRequest = TaskCreateRequest(
                description = "test",
                isReminderSet = false,
                isTaskOpen = false,
                createdOn = LocalDateTime.now(),
                priority = Priority.LOW
        )
    }

    @Test
    fun `when all tasks get fetched then check if the given size is correct`() {
        // Given
        val expectedTasks: List<Task> = listOf(Task(), Task())

        // When
        every { mockRepository.findAll() } returns expectedTasks.toMutableList()
        val actualTasks: List<TaskDTO> = objectUnderTest.getAllTasks()

        // Then
        assertThat(actualTasks.size).isEqualTo(expectedTasks.size)
    }

    @Test
    fun `when task is created then check for the task properties`() {
        // Given
        task.description = createRequest.description
        task.isTaskOpen = createRequest.isTaskOpen
        task.priority = createRequest.priority

        // When
        every { mockRepository.save(any()) } returns task
        val actualTaskDTO: TaskDTO = objectUnderTest.createTask(createRequest)

        // Then
        assertThat(actualTaskDTO.description).isEqualTo(task.description)
        assertThat(actualTaskDTO.isTaskOpen).isEqualTo(task.isTaskOpen)
        assertThat(actualTaskDTO.isReminderSet).isEqualTo(task.isReminderSet)
    }

    @Test
    fun `when task description already exists then check for the bad request exception`() {
        // Given
        // when
        every { mockRepository.doesDescriptionExist(any()) } returns true

        val exception = assertThrows<BadRequestException> { objectUnderTest.createTask(createRequest) }

        // then
        assertThat(exception.message).isEqualTo("There is already a task with the description: ${createRequest.description}")
        verify { mockRepository.save(any()) wasNot called}
    }


    @Test
    fun `when get task by id is called then expect a task not found exception`(){
        every { mockRepository.existsById(any()) } returns false

        val exception: TaskNotFoundException = assertThrows{objectUnderTest.getTaskById((taskId)) }

        assertThat(exception.message).isEqualTo("Task with the ID: $taskId does not exist")
    }

    @Test
    fun `when all open tasks are fetched check the property is task open is true`(){
        task.isTaskOpen = true
        task.priority = Priority.HIGH

        val expectedTasks = listOf(task)

        every { mockRepository.queryAllOpenTasks() } returns expectedTasks.toMutableList()
        val actualList: List<TaskDTO> = objectUnderTest.getAllOpenTasks()

        assertThat(actualList[0].isTaskOpen).isEqualTo(expectedTasks[0].isTaskOpen)

     }

    @Test
    fun `when all closed tasks are fetched check the property if task open is false`(){
        task.isTaskOpen = false
        task.priority = Priority.HIGH

        val expectedTasks = listOf(task)

        every { mockRepository.queryAllClosedTasks() } returns expectedTasks.toMutableList()
        val actualList: List<TaskDTO> = objectUnderTest.getAllClosedTasks()

        assertThat(actualList[0].isTaskOpen).isEqualTo(expectedTasks[0].isTaskOpen)

    }

    @Test
    fun `when save task is called then check if argument could be captured`(){
        val taskSlot = slot<Task>()
        task.description = createRequest.description
        task.isTaskOpen = createRequest.isTaskOpen
        task.priority = createRequest.priority

        every { mockRepository.save(capture(taskSlot)) } returns task
        val actualTaskDTO: TaskDTO = objectUnderTest.createTask(createRequest)

        verify { mockRepository.save(capture(taskSlot)) }
        assertThat(taskSlot.captured.description).isEqualTo(actualTaskDTO.description)
        assertThat(taskSlot.captured.isReminderSet).isEqualTo(actualTaskDTO.isReminderSet)
        assertThat(taskSlot.captured.isTaskOpen).isEqualTo(actualTaskDTO.isTaskOpen)
        assertThat(taskSlot.captured.priority).isEqualTo(actualTaskDTO.priority)

    }

    @Test
    fun `when get task by id is called then check for a specific description`(){
        task.description = "Buy hummus"

        every { mockRepository.existsById(any()) } returns true
        every { mockRepository.findTasksById(any())} returns task

        val actualTaskDTO: TaskDTO = objectUnderTest.getTaskById((taskId))

        assertThat(actualTaskDTO.description).isEqualTo(task.description)
    }

    @Test
    fun `when get task by id is called then check if argument could be captured`() {
        val taskIdSlot = slot<Long>()

        every { mockRepository.existsById(any()) } returns true
        every { mockRepository.findTasksById(capture(taskIdSlot)) } returns task

        val actualTaskDTO: TaskDTO = objectUnderTest.getTaskById((taskId))

        assertThat(actualTaskDTO.description).isEqualTo(task.description)

        verify { mockRepository.findTasksById(capture(taskIdSlot)) }

        assertThat(taskIdSlot.captured).isEqualTo(taskId)
    }

    @Test
    fun `when delete task is called then check the response message`(){
        every {mockRepository.existsById(any())} returns true
        val actualMessage: String = objectUnderTest.deleteTask(taskId)

        assertThat(actualMessage).isEqualTo("Task with the ID: ${taskId} has been deleted.")
    }

    @Test
    fun `when delete task is called then check if argument could be captured`(){
        val taskIdSlot = slot<Long>()

        every {mockRepository.existsById(any())} returns true
        every { mockRepository.deleteById(capture((taskIdSlot))) } returns Unit

        objectUnderTest.deleteTask(taskId)

        verify { mockRepository.deleteById(capture(taskIdSlot))}
        assertThat(taskIdSlot.captured).isEqualTo(taskId)
    }

    @Test
    fun `when update task is called then check for the request properties`(){
        task.description = "Go to  restaurant"
        task.isReminderSet = false
        task.isTaskOpen = true
        task.priority = Priority.MEDIUM

        val request = TaskUpdateRequest(
                task.description,
                task.isReminderSet,
                task.isTaskOpen,
                task.createdOn,
                task.priority,
        )

        every {mockRepository.existsById(any())} returns true
        every { mockRepository.findTasksById(any()) } returns task
        every { mockRepository.save(any()) } returns task

        val actualTaskDTO: TaskDTO =  objectUnderTest.updateTask(taskId,request)

        assertThat(actualTaskDTO.description).isEqualTo(task.description)
        assertThat(actualTaskDTO.isReminderSet).isEqualTo(task.isReminderSet)
        assertThat(actualTaskDTO.isTaskOpen).isEqualTo(task.isTaskOpen)
        assertThat(actualTaskDTO.priority).isEqualTo(task.priority)
        assertThat(actualTaskDTO.createdOn).isEqualTo(task.createdOn)
    }

}
















