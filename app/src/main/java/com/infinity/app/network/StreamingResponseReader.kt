package com.infinity.app.network

import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException

/** Reads Server-Sent Events (SSE) from the backend's POST /api/chat response. */
internal object StreamingResponseReader {
    data class Result(val text: String, val searchedWeb: Boolean)

    @Throws(IOException::class)
    fun read(reader: BufferedReader, onChunk: (String) -> Unit, onSearch: (Boolean) -> Unit): Result {
        val answer = StringBuilder()
        var event = ""
        var searched = false
        val dataLines = mutableListOf<String>()

        fun dispatch() {
            if (dataLines.isEmpty()) { event = ""; return }
            val data = dataLines.joinToString("\n")
            dataLines.clear()
            if (data == "[DONE]") { event = ""; return }
            val obj = runCatching { JSONObject(data) }.getOrNull()
            val type = obj?.optString("type").orEmpty().ifBlank { event }
            when {
                type.contains("search_started", ignoreCase = true) -> { searched = true; onSearch(true) }
                type.contains("search_completed", ignoreCase = true) || type.contains("search_unavailable", ignoreCase = true) -> onSearch(false)
            }
            val choices = obj?.optJSONArray("choices")
            val delta = choices?.optJSONObject(0)?.optJSONObject("delta")
            val content = delta?.optString("content")?.takeIf { it.isNotEmpty() }
                ?: obj?.optString("text")?.takeIf { it.isNotEmpty() }
                ?: obj?.optString("token")?.takeIf { it.isNotEmpty() }
                ?: obj?.optString("content")?.takeIf { it.isNotEmpty() }
                ?: obj?.optString("delta")?.takeIf { it.isNotEmpty() }
                ?: obj?.optString("answer")?.takeIf { it.isNotEmpty() }
                ?: obj?.optString("response")?.takeIf { it.isNotEmpty() }
                ?: if (obj == null && data.isNotBlank()) data else null
            if (content != null) {
                answer.append(content)
                onChunk(content)
            }
            event = ""
        }

        while (true) {
            val line = reader.readLine() ?: break
            when {
                line.isEmpty() -> dispatch()
                line.startsWith(":") -> Unit // SSE comment / keep-alive
                line.startsWith("event:") -> event = line.substringAfter("event:").trim()
                line.startsWith("data:") -> dataLines += line.substringAfter("data:").removePrefix(" ")
            }
        }
        dispatch()
        onSearch(false)
        return Result(answer.toString(), searched)
    }
}
