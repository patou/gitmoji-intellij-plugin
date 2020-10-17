package com.github.patou.gitmoji

class Gitmojis(val gitmojis: List<Gitmoji>) {
    class Gitmoji(val emoji: String, val code: String, val description: String)
}
