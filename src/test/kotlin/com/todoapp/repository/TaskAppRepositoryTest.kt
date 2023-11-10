package com.todoapp.repository

import com.todoapp.data.Task
import com.todoapp.repositories.TaskRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.jdbc.Sql

@DataJpaTest(properties = ["spring.jpa.properties.javax.persistence.validation.mode=none"])
class TaskAppRepositoryTest {

    @Autowired
    private lateinit var objectUnderTest: TaskRepository

    private val numberOfRecordsInTestDataSql = 3;

    private val numberOfOpenRecordsInTestDataSql = 1;

    private val numberOfClosedRecordsInTestDataSql = 2;

    @Test
    @Sql("classpath:test-data.sql")

    fun `when task is saved then check for not null`(){
        //Given
        //Then
        val task: Task = objectUnderTest.findTasksById(111)
        // Then
        assertThat(task).isNotNull
    }

    @Test
    @Sql("classpath:test-data.sql")
    fun `when all tasks are fetched then check for the number of records`(){

        // Given
        // When
       val tasks: List<Task> = objectUnderTest.findAll()

        //Then
        assertThat(tasks.size).isEqualTo(numberOfRecordsInTestDataSql)
    }

    @Test
    @Sql("classpath:test-data.sql")
    fun `when task is deleted then check for the size of list`(){
        // Given
        // When
        objectUnderTest.deleteById(113)
        val tasks: List<Task> = objectUnderTest.findAll()

        //Then
        assertThat(tasks.size).isEqualTo(2)

    }

    @Test
    @Sql("classpath:test-data.sql")
    fun `when all open tasks are queried then check for the correct number`(){
        // Given
        // When
        val tasks: List<Task> = objectUnderTest.queryAllOpenTasks()

        //Then
        assertThat(tasks.size).isEqualTo(numberOfOpenRecordsInTestDataSql)

    }

    @Test
    @Sql("classpath:test-data.sql")
    fun `when all closed tasks are queried then check for the correct number`(){
        // Given
        // When
        val tasks: List<Task> = objectUnderTest.queryAllClosedTasks()

        //Then
        assertThat(tasks.size).isEqualTo(numberOfClosedRecordsInTestDataSql)
    }

    @Test
    @Sql("classpath:test-data.sql")
    fun `when description is queried the check if it already exists`(){
        // Given
        // When
        val firstDescription = objectUnderTest.doesDescriptionExist("first test todo");
        val thirdDescription = objectUnderTest.doesDescriptionExist("Free the cat");

        //Then
        assertThat(firstDescription).isTrue
        assertThat(thirdDescription).isFalse
    }
}