package com.infinity.app.network

import com.infinity.app.config.AppConfig
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/** Shared client; session cookies exist in memory only and are not persisted to disk. */
object InfinityHttpClient {
    private val cookieStore = mutableMapOf<String, MutableList<Cookie>>()
    val client: OkHttpClient = OkHttpClient.Builder()
        .cookieJar(object : CookieJar {
            override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                synchronized(cookieStore) { cookieStore[url.host] = cookies.toMutableList() }
            }
            override fun loadForRequest(url: HttpUrl): List<Cookie> =
                synchronized(cookieStore) { cookieStore[url.host]?.filter { it.matches(url) } ?: emptyList() }
        })
        .connectTimeout(AppConfig.CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(AppConfig.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .callTimeout(150, TimeUnit.SECONDS)
        .build()
}
