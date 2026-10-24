package com.infinity.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class LocalChatStore(context: Context) {
    private val prefs = context.getSharedPreferences("infinity_native", Context.MODE_PRIVATE)

    fun load(): MutableList<ChatMessage> = decode(prefs.getString("messages", "[]"))

    fun save(messages: List<ChatMessage>) {
        prefs.edit().putString("messages", encode(messages).toString()).apply()
    }

    fun clear() { prefs.edit().remove("messages").apply() }

    /** Saves the current thread as a local conversation before starting a new one. */
    fun archive(messages: List<ChatMessage>) {
        if (messages.isEmpty()) return
        val all = runCatching { JSONArray(prefs.getString("conversation_history", "[]")) }.getOrNull() ?: JSONArray()
        val title = messages.firstOrNull { it.fromUser }?.text?.replace('\n', ' ')?.take(48) ?: "Conversation"
        val item = JSONObject().put("title", title).put("savedAt", System.currentTimeMillis()).put("messages", encode(messages))
        val updated = JSONArray().put(item)
        for (i in 0 until minOf(all.length(), 49)) {
            val old = all.optJSONObject(i) ?: continue
            if (old.optString("savedAt") != item.optString("savedAt")) updated.put(old)
        }
        prefs.edit().putString("conversation_history", updated.toString()).apply()
    }

    fun history(): List<ArchivedConversation> {
        val all = runCatching { JSONArray(prefs.getString("conversation_history", "[]")) }.getOrNull() ?: JSONArray()
        return (0 until all.length()).mapNotNull { i ->
            val obj = all.optJSONObject(i) ?: return@mapNotNull null
            ArchivedConversation(obj.optString("title", "Conversation"), obj.optLong("savedAt"), decode(obj.optJSONArray("messages")?.toString() ?: "[]"))
        }
    }

    fun restore(conversation: ArchivedConversation) = save(conversation.messages)

    private fun encode(messages: List<ChatMessage>): JSONArray = JSONArray().also { array ->
        messages.forEach { m -> array.put(JSONObject().put("text", m.text).put("fromUser", m.fromUser)
            .put("question", m.questionForRating ?: "").put("rated", m.rated)) }
    }

    private fun decode(raw: String?): MutableList<ChatMessage> {
        val array = runCatching { JSONArray(raw ?: "[]") }.getOrNull() ?: JSONArray()
        return MutableList(array.length()) { i ->
            val o = array.optJSONObject(i) ?: JSONObject()
            ChatMessage(o.optString("text"), o.optBoolean("fromUser"), o.optString("question").ifBlank { null }, o.optBoolean("rated"))
        }
    }
}

data class ArchivedConversation(val title: String, val savedAt: Long, val messages: MutableList<ChatMessage>)
