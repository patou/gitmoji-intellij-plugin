package com.github.patou.gitmoji

const val CONFIG_USE_UNICODE: String = "com.github.patou.gitmoji.use-unicode"
data class GitmojiData(val name: String, val emoji: String, val description: String) {
    var code : String = ":$name:"
}
