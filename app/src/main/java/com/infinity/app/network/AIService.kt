package com.infinity.app.network

import com.infinity.app.config.AppConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okio.BufferedSource
import org.json.JSONObject
import java.io.IOException

/** SSE parser follows the verified web client: data: JSON, content/delta.content/choices delta, [DONE]. */
class AIService(private val client: okhttp3.OkHttpClient = InfinityHttpClient.client) {
    data class StreamResult(val answer: String, val searchedWeb: Boolean)
    private val jsonType = "application/json; charset=utf-8".toMediaType()

    @Throws(IOException::class)
    fun streamMessage(
        message: String,
        convId: String,
        onChunk: (String) -> Unit,
        onSearch: (Boolean) -> Unit = {}
    ): StreamResult {
        val payload = JSONObject().put("message", message).put("convId", convId)
        val request = Request.Builder().url(AppConfig.CHAT_ENDPOINT)
            .header("Accept", "text/event-stream")
            .post(payload.toString().toRequestBody(jsonType)).build()
        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Serveur Infinity : HTTP ${response.code}")
                val body = response.body ?: throw IOException("Réponse vide du serveur.")
                val contentType = response.header("Content-Type").orEmpty().lowercase()
                if (!contentType.contains("text/event-stream")) {
                    val raw = body.string()
                    val obj = runCatching { JSONObject(raw) }.getOrNull()
                    val answer = obj?.optString("answer")?.takeIf { it.isNotBlank() }
                        ?: obj?.optString("response")?.takeIf { it.isNotBlank() }
                        ?: throw IOException("Le serveur n'a pas renvoyé un flux SSE ni une réponse JSON reconnue.")
                    onChunk(answer)
                    return StreamResult(answer, false)
                }
                return parseSse(body.source(), onChunk, onSearch)
            }
        } finally {
            onSearch(false)
        }
    }

    private fun parseSse(source: BufferedSource, onChunk: (String) -> Unit, onSearch: (Boolean) -> Unit): StreamResult {
        val answer = StringBuilder()
        var searched = false
        var done = false
        while (!source.exhausted()) {
            val line = source.readUtf8Line() ?: break
            val trimmed = line.trim()
            if (!trimmed.startsWith("data:")) continue
            val data = trimmed.removePrefix("data:").trim()
            if (data == "[DONE]") { done = true; break }
            if (data.isEmpty() || !data.startsWith("{")) continue
            val obj = runCatching { JSONObject(data) }.getOrNull() ?: continue
            if (obj.has("error") && !obj.isNull("error")) {
                throw IOException(obj.optString("error").ifBlank { "Erreur du serveur Infinity." })
            }
            val type = (obj.optString("type") + " " + obj.optString("event")).lowercase()
            when {
                type.contains("search_started") -> { searched = true; onSearch(true); continue }
                type.contains("search_completed") || type.contains("search_unavailable") -> { onSearch(false); continue }
            }
            val delta = obj.optJSONObject("delta")
            val choices = obj.optJSONArray("choices")
            val choiceDelta = choices?.optJSONObject(0)?.optJSONObject("delta")
            val content = obj.optString("content").takeIf { it.isNotEmpty() }
                ?: delta?.optString("content")?.takeIf { it.isNotEmpty() }
                ?: choiceDelta?.optString("content")?.takeIf { it.isNotEmpty() }
            if (content != null) { answer.append(content); onChunk(content) }
        }
        if (answer.isBlank()) throw IOException(if (done) "Infinity a renvoyé une réponse vide." else "Le flux du serveur s'est terminé sans texte exploitable.")
        return StreamResult(answer.toString(), searched)
    }
}
