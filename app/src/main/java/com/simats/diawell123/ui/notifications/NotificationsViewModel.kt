package com.simats.diawell123.ui.notifications
import com.simats.diawell123.ui.theme.AppTheme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.diawell123.data.model.NotificationItemData
import com.simats.diawell123.data.repository.NotificationsRepository
import com.simats.diawell123.data.repository.NotificationsRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotificationsUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val dateHeader: String = "Today",
    val unreadCount: Int = 0,
    val notifications: List<NotificationItemData> = emptyList(),
    val errorMessage: String? = null
)

class NotificationsViewModel(
    private val repository: NotificationsRepository = NotificationsRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        loadNotifications()
    }

    fun loadNotifications(isPullToRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                if (isPullToRefresh) it.copy(isRefreshing = true, errorMessage = null)
                else it.copy(isLoading = it.notifications.isEmpty(), errorMessage = null)
            }

            val result = repository.getNotifications()
            result.fold(
                onSuccess = { data ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            dateHeader = data.dateHeader,
                            unreadCount = data.unreadCount,
                            notifications = data.notifications,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = error.message ?: "Failed to load notifications."
                        )
                    }
                }
            )
        }
    }

    fun markAsRead(notificationId: Int) {
        viewModelScope.launch {
            val result = repository.markAsRead(notificationId)
            if (result.isSuccess) {
                _uiState.update { current ->
                    val updatedList = current.notifications.map { item ->
                        if (item.id == notificationId) item.copy(isUnread = false, isRead = 1)
                        else item
                    }
                    val unread = updatedList.count { it.isUnread }
                    current.copy(notifications = updatedList, unreadCount = unread)
                }
            }
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            val result = repository.clearAll()
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(notifications = emptyList(), unreadCount = 0)
                }
            }
        }
    }
}

