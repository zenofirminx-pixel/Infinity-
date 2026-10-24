package com.infinity.app.data

data class Message(
    val text: String,
    val fromUser: Boolean,
    val questionForRating: String? = null,
    var rated: Boolean = false
)
