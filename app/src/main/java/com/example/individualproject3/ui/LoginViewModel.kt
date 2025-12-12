package com.example.individualproject3.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.individualproject3.data.User
import com.example.individualproject3.data.UserDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(private val userDao: UserDao) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun onUsernameChange(newValue: String) {
        _uiState.value = _uiState.value.copy(username = newValue)
    }

    fun onPasswordChange(newValue: String) {
        _uiState.value = _uiState.value.copy(password = newValue)
    }

    fun onTabSelected(isParent: Boolean) {
        _uiState.value = _uiState.value.copy(isParentTab = isParent)
    }

    fun onLoginClick(onSuccess: (Boolean) -> Unit) {
        val username = uiState.value.username
        val password = uiState.value.password
        val isParent = uiState.value.isParentTab

        if (username.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Fields cannot be empty")
            return
        }

        viewModelScope.launch {
            val user = userDao.getUserByUsername(username)
            if (user != null) {
                if (user.passwordHash == password && user.isParent == isParent) {
                    onSuccess(isParent)
                } else {
                    _uiState.value = _uiState.value.copy(error = "Invalid credentials or role")
                }
            } else {
                _uiState.value = _uiState.value.copy(error = "User not found")
            }
        }
    }

    fun onRegisterClick(onSuccess: (Boolean) -> Unit) {
         val username = uiState.value.username
        val password = uiState.value.password
        val isParent = uiState.value.isParentTab

        if (username.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Fields cannot be empty")
            return
        }

        viewModelScope.launch {
            val existing = userDao.getUserByUsername(username)
            if (existing != null) {
                _uiState.value = _uiState.value.copy(error = "Username already exists")
            } else {
                val newUser = User(username = username, passwordHash = password, isParent = isParent)
                userDao.insertUser(newUser)
                onSuccess(isParent)
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isParentTab: Boolean = true,
    val error: String? = null
)
