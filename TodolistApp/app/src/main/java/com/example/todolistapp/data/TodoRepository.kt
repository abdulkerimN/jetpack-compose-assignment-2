package com.example.todolistapp.data

import com.example.todolistapp.data.local.TodoDao
import com.example.todolistapp.data.model.Todo
import com.example.todolistapp.data.remote.TodoApi
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TodoRepository @Inject constructor(
    private val todoApi: TodoApi,
    private val todoDao: TodoDao
) {
    fun getAllTodos(): Flow<List<Todo>> = todoDao.getAllTodos()

    suspend fun refreshTodos() {
        val remoteTodos = todoApi.getTodos()
        todoDao.insertAll(remoteTodos)
    }

    suspend fun getTodoById(id: Int): Todo? = todoDao.getTodoById(id)
} 