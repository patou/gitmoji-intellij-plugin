package com.github.patou.gitmoji

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

const val CONFIG_USE_UNICODE: String = "com.github.patou.gitmoji.use-unicode"
const val CONFIG_DISPLAY_ICON: String = "com.github.patou.gitmoji.display-icon"
const val CONFIG_AFTER_UNICODE: String = "com.github.patou.gitmoji.text-after-unicode"
const val CONFIG_LANGUAGE: String = "com.github.patou.gitmoji.language"
const val CONFIG_INSERT_IN_CURSOR_POSITION: String = "com.github.patou.gitmoji.insert-in-cursor-position"
const val CONFIG_INCLUDE_GITMOJI_DESCRIPTION: String = "com.github.patou.gitmoji.include-gitmoji-description"

data class GitmojiData(val code: String, val emoji: String, val description: String, val name: String) {
    private lateinit var _icon: Icon

    fun getIcon(): Icon {
        if (!this::_icon.isInitialized) {
            _icon = try {
                IconLoader.findIcon(
                    "/icons/gitmoji/" + code.replace(":", "") + ".png",
                    GitmojiData::class.java,
                    false,
                    true
                )!!
            } catch (e: Exception) {
                IconLoader.getIcon("/icons/gitmoji/anguished.png", GitmojiData::class.java)
            }
        }
        return _icon
    }

    val localeDescription: String
        get() = GitmojiLocale.t(name, description)
}
