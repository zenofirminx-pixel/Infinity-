package com.infinity.app.config

/** Endpoints verified against web-original/script.js. */
object AppConfig {
    const val BASE_URL = "https://aur-x-backend.vercel.app"
    const val CHAT_ENDPOINT = "$BASE_URL/api/chat"
    const val CONVERSATIONS_ENDPOINT = "$BASE_URL/api/conversations"
    const val HISTORY_ENDPOINT = "$BASE_URL/api/history"
    const val FEEDBACK_ENDPOINT = "$BASE_URL/api/feedback"
    const val AUTH_GOOGLE_ENDPOINT = "$BASE_URL/api/auth/google"
    const val AUTH_ME_ENDPOINT = "$BASE_URL/api/auth/me"
    const val AUTH_LOGOUT_ENDPOINT = "$BASE_URL/api/auth/logout"
    const val CONNECT_TIMEOUT_SECONDS = 20L
    const val READ_TIMEOUT_SECONDS = 120L
}
