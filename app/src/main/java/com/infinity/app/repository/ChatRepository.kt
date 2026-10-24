package com.infinity.app.repository

import com.infinity.app.data.Conversation
import com.infinity.app.network.AIService
import com.infinity.app.network.ConversationService
import com.infinity.app.network.InfinityHttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChatRepository {
    private val store = ConversationStore()
    private val conversations = ConversationService(InfinityHttpClient.client)
    private val ai = AIService(InfinityHttpClient.client)

    fun currentConversationId(): String? = store.currentId()
    suspend fun createConversation(): Conversation = withContext(Dispatchers.IO) {
        conversations.createConversation().also { store.setCurrentId(it.id) }
    }
    suspend fun sendMessage(message: String, onChunk: (String) -> Unit, onSearch: (Boolean) -> Unit): AIService.StreamResult = withContext(Dispatchers.IO) {
        val conversationId = store.currentId()
            ?: conversations.createConversation().id.also(store::setCurrentId)
        ai.streamMessage(message, conversationId, onChunk, onSearch)
    }
    suspend fun loadHistory(): String = withContext(Dispatchers.IO) { conversations.loadHistory() }
    fun clearActiveConversation() = store.clear()
}
