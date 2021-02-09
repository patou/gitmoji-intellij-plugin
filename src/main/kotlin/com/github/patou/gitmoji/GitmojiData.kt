package com.github.patou.gitmoji

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

const val CONFIG_USE_UNICODE: String = "com.github.patou.gitmoji.use-unicode"
const val CONFIG_DISPLAY_ICON: String = "com.github.patou.gitmoji.display-icon"
const val CONFIG_AFTER_UNICODE: String = "com.github.patou.gitmoji.text-after-unicode"

data class GitmojiData(val code: String, val emoji: String, val description: String) {
    private lateinit var _icon : Icon

    public fun getIcon(): Icon {
        if (!this::_icon.isInitialized) {
            try {
                _icon = IconLoader.findIcon("/icons/gitmoji/" + code.replace(":", "") + ".png", true)!!
            } catch (e: Exception) {
                _icon = IconLoader.getIcon("/icons/gitmoji/anguished.png")
            }
        }
        return _icon
    }
}
