package com.github.patou.gitmoji

class JsonGitmojis(val gitmojis: List<JsonGitmoji>) {
    class JsonGitmoji(val emoji: String, val code: String, val description: String, val name: String)
}
