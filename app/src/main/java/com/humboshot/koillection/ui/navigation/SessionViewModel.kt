package com.humboshot.koillection.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humboshot.koillection.data.repository.UserRepository
import com.humboshot.koillection.data.user.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** The logged-in user for the side menu. Stays null when loading fails, and the menu then shows every entry. */
class SessionViewModel : ViewModel() {
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    fun load() {
        _user.value = null
        viewModelScope.launch {
            _user.value = UserRepository.getCurrentUser().getOrNull()
        }
    }
}
