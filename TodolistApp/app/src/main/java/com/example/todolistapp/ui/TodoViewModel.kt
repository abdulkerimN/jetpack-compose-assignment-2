package com.example.todolistapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolistapp.data.local.TodoDao
import com.example.todolistapp.data.model.Todo
import com.example.todolistapp.data.remote.TodoApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.net.UnknownHostException
import javax.inject.Inject

@HiltViewModel
class TodoViewModel @Inject constructor(
    private val todoApi: TodoApi,
    private val todoDao: TodoDao
) : ViewModel() {
    private val _todos = MutableStateFlow<List<Todo>>(emptyList())
    val todos: StateFlow<List<Todo>> = _todos.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadTodos()
    }

    fun loadTodos() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // First try to load from local database
                val localTodos = todoDao.getAllTodos().first()
                if (localTodos.isNotEmpty()) {
                    _todos.value = localTodos
                }

                // Then fetch from network
                val remoteTodos = todoApi.getTodos()
                todoDao.insertAll(remoteTodos)
                _todos.value = remoteTodos
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is UnknownHostException -> "Unable to connect to the server. Please check your internet connection."
                    else -> e.message ?: "An error occurred while loading todos"
                }
                _error.value = errorMessage
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshTodos() {
        loadTodos()
    }
} 