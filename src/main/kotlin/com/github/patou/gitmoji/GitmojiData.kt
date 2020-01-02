package com.github.patou.gitmoji

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

const val CONFIG_USE_UNICODE: String = "com.github.patou.gitmoji.use-unicode"
data class GitmojiData(val code: String, val unicode: String, val description: String) {
    var icon : Icon = loadIcon(code)
    var emoji : String = ":" + code + ":"

    private fun loadIcon(code: String) : Icon {
        try {
            return IconLoader.getIcon("/icons/" + code + ".png")
        }
        catch (e : Exception) {
            return IconLoader.getIcon("/icons/anguished.png")
        }
    }
}
