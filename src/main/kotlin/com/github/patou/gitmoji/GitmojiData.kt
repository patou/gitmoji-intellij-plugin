package com.github.patou.gitmoji

const val CONFIG_USE_UNICODE: String = "com.github.patou.gitmoji.use-unicode"
data class GitmojiData(val code: String, val emoji: String, val description: String)
