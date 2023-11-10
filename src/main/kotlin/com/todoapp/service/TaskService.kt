package com.todoapp.service

import com.todoapp.data.Task
import com.todoapp.data.model.TaskCreateRequest
import com.todoapp.data.model.TaskDTO
import com.todoapp.data.model.TaskUpdateRequest
import com.todoapp.exception.BadRequestException
import com.todoapp.exception.TaskNotFoundException
import com.todoapp.repositories.TaskRepository
import org.springframework.stereotype.Service
import org.springframework.util.ReflectionUtils
import java.lang.reflect.Field
import java.util.stream.Collectors
import kotlin.reflect.full.memberProperties

@Service
class TaskService(private val repository: TaskRepository){

    private fun mapEntityToDTO(task: Task) = TaskDTO(
                task.id,
                task.description,
                task.isReminderSet,
                task.isTaskOpen,
                task.createdOn,
                task.priority
        )

    private fun mapDTOToEntity(task: Task, request:TaskCreateRequest){
        task.description=request.description
        task.isReminderSet=request.isReminderSet
        task.isTaskOpen=request.isTaskOpen
        task.priority=request.priority
        task.description=request.description
    }


    private fun checkTaskForId(id: Long){
        if (!repository.existsById(id)){
            throw TaskNotFoundException("Task with the ID: $id does not exist")
        }
    }

    fun getTaskById(id: Long): TaskDTO{
        checkTaskForId(id)
        val task: Task = repository.findTasksById(id)
        return mapEntityToDTO(task)
    }

    fun getAllTasks(): List<TaskDTO> =
            repository.findAll().stream().map(this::mapEntityToDTO).collect(Collectors.toList())


    fun getAllOpenTasks(): List<TaskDTO> =
            repository.queryAllOpenTasks().stream().map(this::mapEntityToDTO).collect(Collectors.toList())


    fun getAllClosedTasks(): List<TaskDTO> =
            repository.queryAllClosedTasks().stream().map(this::mapEntityToDTO).collect(Collectors.toList())


    fun createTask(request:TaskCreateRequest): TaskDTO{
        if (repository.doesDescriptionExist(request.description)){
            throw BadRequestException("There is already a task with the description: ${request.description}")
        }
        val task = Task()

        mapDTOToEntity(task,request)
        val savedTask = repository.save(task)

        return mapEntityToDTO(savedTask)
    }

    fun updateTask(id: Long, request: TaskUpdateRequest): TaskDTO{
        checkTaskForId(id)

        val existingTask = repository.findTasksById((id))
        // Refresh on kotlin reflect properties
        for (prop in TaskUpdateRequest::class.memberProperties){
            if(prop.get(request) != null){
                val field: Field? = ReflectionUtils.findField(Task::class.java,prop.name)
                field?.let{
                    it.isAccessible = true
                    ReflectionUtils.setField(it,existingTask, prop.get(request))
                }
            }
        }

        val savedTask: Task = repository.save(existingTask)
        return mapEntityToDTO(savedTask)

    }

    fun deleteTask(id: Long): String{
        checkTaskForId(id)

        repository.deleteById(id)

        return "Task with the ID: ${id} has been deleted."
    }


}



