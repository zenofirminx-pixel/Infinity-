package com.infinity.app

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModel
import com.infinity.app.data.ChatUiState
import com.infinity.app.repository.ChatRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/** Coordinates chat work without persisting messages or conversation IDs to disk. */
class ChatViewModel : ViewModel() {
    private val repository = ChatRepository()
    private val workScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val mainHandler = Handler(Looper.getMainLooper())
    var uiState: ChatUiState = ChatUiState()
        private set

    fun sendMessage(
        text: String,
        onChunk: (String) -> Unit,
        onSearch: (Boolean) -> Unit,
        onComplete: (String) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        if (uiState.isSending) return
        uiState = uiState.copy(isSending = true, errorMessage = null)
        workScope.launch {
            try {
                val result = repository.sendMessage(text,
                    onChunk = { chunk -> mainHandler.post { onChunk(chunk) } },
                    onSearch = { active -> mainHandler.post {
                        uiState = uiState.copy(isSearchingWeb = active)
                        onSearch(active)
                    } }
                )
                uiState = uiState.copy(isSending = false, isSearchingWeb = false, activeConversationId = repository.currentConversationId())
                onComplete(result.answer)
            } catch (error: Throwable) {
                uiState = uiState.copy(isSending = false, isSearchingWeb = false, errorMessage = error.message)
                onError(error)
            }
        }
    }

    fun newConversation(onComplete: (String) -> Unit, onError: (Throwable) -> Unit) {
        workScope.launch {
            try {
                val conversation = repository.createConversation()
                uiState = ChatUiState(activeConversationId = conversation.id)
                onComplete(conversation.id)
            } catch (error: Throwable) { onError(error) }
        }
    }

    fun loadHistory(onComplete: (String) -> Unit, onError: (Throwable) -> Unit) {
        workScope.launch {
            try { onComplete(repository.loadHistory()) } catch (error: Throwable) { onError(error) }
        }
    }

    override fun onCleared() {
        workScope.cancel()
        super.onCleared()
    }
}
