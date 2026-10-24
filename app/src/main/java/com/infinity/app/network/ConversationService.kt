package com.infinity.app.network

import com.infinity.app.config.AppConfig
import com.infinity.app.data.Conversation
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class ConversationService(private val client: okhttp3.OkHttpClient = InfinityHttpClient.client) {
    /** Server is the authority for IDs; never synthesizes a local convId. */
    @Throws(IOException::class)
    fun createConversation(): Conversation {
        val request = Request.Builder().url(AppConfig.CONVERSATIONS_ENDPOINT)
            .post(ByteArray(0).toRequestBody(null)).build()
        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) throw IOException("Création de conversation refusée (HTTP ${response.code}).")
            val json = runCatching { JSONObject(body) }.getOrNull()
                ?: throw IOException("Réponse de création de conversation invalide.")
            val id = json.optString("convId").takeIf { it.isNotBlank() }
                ?: json.optString("id").takeIf { it.isNotBlank() }
                ?: json.optJSONObject("conversation")?.optString("id")?.takeIf { it.isNotBlank() }
                ?: throw IOException("Le serveur n'a pas renvoyé de convId.")
            val title = json.optString("title").takeIf { it.isNotBlank() }
            return Conversation(id, title)
        }
    }

    @Throws(IOException::class)
    fun loadHistory(): String {
        val request = Request.Builder().url(AppConfig.HISTORY_ENDPOINT).get().build()
        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) throw IOException("Historique indisponible (HTTP ${response.code}).")
            return body
        }
    }
}
