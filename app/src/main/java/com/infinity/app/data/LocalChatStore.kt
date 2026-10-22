package com.infinity.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class LocalChatStore(context: Context) {
    private val prefs = context.getSharedPreferences("infinity_native", Context.MODE_PRIVATE)

    fun load(): MutableList<ChatMessage> {
        val array = runCatching { JSONArray(prefs.getString("messages", "[]")) }.getOrNull() ?: JSONArray()
        return MutableList(array.length()) { i ->
            val o = array.optJSONObject(i) ?: JSONObject()
            ChatMessage(o.optString("text"), o.optBoolean("fromUser"), o.optString("question").ifBlank { null }, o.optBoolean("rated"))
        }
    }

    fun save(messages: List<ChatMessage>) {
        val array = JSONArray()
        messages.forEach { m ->
            array.put(JSONObject().put("text", m.text).put("fromUser", m.fromUser)
                .put("question", m.questionForRating ?: "").put("rated", m.rated))
        }
        prefs.edit().putString("messages", array.toString()).apply()
    }

    fun clear() { prefs.edit().remove("messages").apply() }
}
