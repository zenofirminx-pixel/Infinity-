package com.infinity.app.data

data class ChatUiState(
    val isSending: Boolean = false,
    val isSearchingWeb: Boolean = false,
    val errorMessage: String? = null,
    val activeConversationId: String? = null
)
