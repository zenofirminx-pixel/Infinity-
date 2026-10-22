package com.infinity.app.network

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Centralise les appels réseau. Le backend reste la source de l'IA.
 * Les cookies de session sont conservés en mémoire pendant l'exécution de l'application.
 */
class InfinityApi {
    companion object {
        val BASE_URL = "https://" + "aur" + "-" + "x" + "-backend.vercel.app"
        private val JSON = "application/json; charset=utf-8".toMediaType()
    }

    private val cookieStore = mutableMapOf<String, MutableList<Cookie>>()
    private val client = OkHttpClient.Builder()
        .cookieJar(object : CookieJar {
            override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                synchronized(cookieStore) { cookieStore[url.host] = cookies.toMutableList() }
            }
            override fun loadForRequest(url: HttpUrl): List<Cookie> =
                synchronized(cookieStore) { cookieStore[url.host]?.filter { it.matches(url) } ?: emptyList() }
        })
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .callTimeout(150, TimeUnit.SECONDS)
        .build()

    data class ChatResult(val answer: String, val searchedWeb: Boolean = false)

    @Throws(IOException::class)
    fun sendMessage(message: String, convId: String? = null, onSearch: (Boolean) -> Unit = {}): ChatResult {
        val payload = JSONObject().put("message", message)
        if (!convId.isNullOrBlank()) payload.put("convId", convId)
        val request = Request.Builder().url("$BASE_URL/api/chat")
            .post(payload.toString().toRequestBody(JSON)).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Serveur Infinity : HTTP ${response.code}")
            val body = response.body ?: throw IOException("Réponse vide du serveur.")
            val contentType = response.header("Content-Type").orEmpty().lowercase()
            if (!contentType.contains("text/event-stream")) {
                val raw = body.string()
                val obj = runCatching { JSONObject(raw) }.getOrNull()
                return ChatResult(
                    obj?.optString("answer")?.takeIf { it.isNotBlank() }
                        ?: obj?.optString("response")?.takeIf { it.isNotBlank() }
                        ?: raw
                )
            }

            val answer = StringBuilder()
            var searched = false
            var eventName = ""
            body.charStream().buffered().useLines { lines ->
                lines.forEach { line ->
                    when {
                        line.startsWith("event:") -> eventName = line.removePrefix("event:").trim()
                        line.startsWith("data:") -> {
                            val data = line.removePrefix("data:").trim()
                            val obj = runCatching { JSONObject(data) }.getOrNull()
                            val type = obj?.optString("type").orEmpty().ifBlank { eventName }
                            if (type.contains("search_started")) { searched = true; onSearch(true) }
                            if (type.contains("search_completed") || type.contains("search_unavailable")) onSearch(false)
                            val text = obj?.optString("text")?.takeIf { it.isNotBlank() }
                                ?: obj?.optString("token")?.takeIf { it.isNotBlank() }
                                ?: obj?.optString("content")?.takeIf { it.isNotBlank() }
                            if (text != null) answer.append(text)
                            else if (obj == null && data.isNotBlank() && data != "[DONE]") answer.append(data)
                            eventName = ""
                        }
                        line.isBlank() -> eventName = ""
                    }
                }
            }
            onSearch(false)
            if (answer.isBlank()) throw IOException("Le serveur n'a pas renvoyé de texte exploitable.")
            return ChatResult(answer.toString(), searched)
        }
    }

    @Throws(IOException::class)
    fun sendFeedback(type: String, description: String? = null, rating: String? = null, question: String? = null, answer: String? = null) {
        val obj = JSONObject().put("type", type)
        if (description != null) obj.put("description", description)
        if (rating != null) obj.put("rating", rating)
        if (question != null) obj.put("question", question)
        if (answer != null) obj.put("answer", answer)
        val request = Request.Builder().url("$BASE_URL/api/feedback")
            .post(obj.toString().toRequestBody(JSON)).build()
        client.newCall(request).execute().use {
            if (!it.isSuccessful) throw IOException("Envoi impossible (HTTP ${it.code}).")
            val result = runCatching { JSONObject(it.body?.string().orEmpty()) }.getOrNull()
            if (result?.optBoolean("success", true) == false) throw IOException(result.optString("error", "Envoi impossible."))
        }
    }

    @Throws(IOException::class)
    fun loadHistory(): String {
        val request = Request.Builder().url("$BASE_URL/api/history").get().build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Aucune session serveur détectée.")
            return response.body?.string().orEmpty()
        }
    }
}
