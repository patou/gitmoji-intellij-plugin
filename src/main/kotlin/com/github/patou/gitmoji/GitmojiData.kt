package com.github.patou.gitmoji

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

data class GitmojiData(val code: String, val description: String) {
    var icon : Icon = loadIcon(code)
    var emotji : String = ":" + code + ":"

    private fun loadIcon(code: String) : Icon {
        try {
            return IconLoader.getIcon("/icons/" + code + ".png")
        }
        catch (e : Exception) {
            return IconLoader.getIcon("/icons/anguished.png")
        }
    }
}
