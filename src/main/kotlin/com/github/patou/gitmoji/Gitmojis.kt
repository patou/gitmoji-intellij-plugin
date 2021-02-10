package com.github.patou.gitmoji

import com.intellij.openapi.util.SystemInfo

class Gitmojis(val gitmojis: List<Gitmoji>) {
    class Gitmoji(val emoji: String, val code: String, val description: String)

    companion object {
        fun defaultDisplayType(): String {
            return if (SystemInfo.isWindows) "icon" else "emoji"
        }
    }
}
