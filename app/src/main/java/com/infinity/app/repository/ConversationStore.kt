package com.infinity.app.repository

/** Keeps only the active server-issued conversation ID in process memory. */
class ConversationStore {
    @Volatile private var activeId: String? = null
    fun currentId(): String? = activeId
    fun setCurrentId(id: String?) { activeId = id }
    fun clear() { activeId = null }
}
