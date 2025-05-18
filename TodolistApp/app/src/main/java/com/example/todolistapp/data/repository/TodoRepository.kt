package com.example.todolistapp.data.repository

import com.example.todolistapp.data.local.TodoDao
import com.example.todolistapp.data.model.Todo
import com.example.todolistapp.data.remote.TodoApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.emitAll
import javax.inject.Inject

class TodoRepository @Inject constructor(
    private val todoApi: TodoApi,
    private val todoDao: TodoDao
) {
    fun getTodos(): Flow<List<Todo>> = flow {
        // Emit cached data first
        emitAll(todoDao.getAllTodos())

        try {
            // Fetch fresh data from network
            val todos = todoApi.getTodos()
            // Update cache
            todoDao.deleteAll()
            todoDao.insertAll(todos)
        } catch (e: Exception) {
            // If network request fails, cached data will still be emitted
            e.printStackTrace()
        }
    }

    suspend fun getTodoById(id: Int): Todo? {
        return try {
            // Try to get from network first
            val todo = todoApi.getTodoById(id)
            // Update cache
            todoDao.insertAll(listOf(todo))
            todo
        } catch (e: Exception) {
            // If network fails, get from cache
            todoDao.getTodoById(id)
        }
    }
} 