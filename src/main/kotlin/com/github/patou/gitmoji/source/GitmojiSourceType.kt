package com.github.patou.gitmoji.source

import com.github.patou.gitmoji.GitmojiBundle
import com.github.patou.gitmoji.OptionItem

sealed interface GitmojiSourceType {

    val id: Id
    val settingsName: String
    val jsonUrl: String
    val localizationUrl: String
    val tooltipUrl: String
    val tooltipText: String

    @JvmInline
    value class Id(val value: String)

    fun getLocalizedUrl(locale: String): String {
        return localizationUrl.replace("{locale}", locale)
    }

    companion object {

        val OPTIONS = arrayOf(
            OptionItem(Gitmoji.id, Gitmoji.settingsName),
            OptionItem(ConventionalGitmoji.id, ConventionalGitmoji.settingsName),
            OptionItem(Custom.ID, Custom.NAME)
        )
    }

    data object Gitmoji : GitmojiSourceType {

        override val id: Id = Id("gitmoji")
        override val settingsName: String = "Gitmoji"
        override val jsonUrl: String = "https://gitmoji.dev/api/gitmojis"
        override val localizationUrl: String = "https://raw.githubusercontent.com/patou/gitmoji-plus-commit-button/refs/heads/master/src/main/resources/gitmojis-{locale}.yaml"
        override val tooltipUrl: String = "https://gitmoji.dev/"
        override val tooltipText: String = GitmojiBundle.message("config.source.type.gitmoji.tooltip")
    }

    data object ConventionalGitmoji : GitmojiSourceType {

        override val id: Id = Id("conventional-gitmoji")
        override val settingsName: String = "Gitmoji Conventional"
        override val jsonUrl: String = "https://raw.githubusercontent.com/glazrtom/conventional-gitmoji-intellij-config/refs/heads/master/gitmojis.json"
        override val localizationUrl: String = "https://raw.githubusercontent.com/glazrtom/conventional-gitmoji-intellij-config/refs/heads/master/localizations/gitmojis-{locale}.yaml"
        override val tooltipUrl: String = "https://conventional-gitmoji.web.app/"
        override val tooltipText: String = GitmojiBundle.message("config.source.type.conventionalGitmoji.tooltip")
    }

    data class Custom(
        override val jsonUrl: String,
        override val localizationUrl: String
    ) : GitmojiSourceType {

        override val id: Id = ID
        override val settingsName: String = NAME
        override val tooltipUrl: String = TOOLTIP_URL
        override val tooltipText: String = TOOLTIP_TEXT

        companion object {

            val ID = Id("custom")
            const val NAME = "Custom"
            const val TOOLTIP_URL = ""
            val TOOLTIP_TEXT = GitmojiBundle.message("config.source.type.custom.tooltip")
        }
    }
}

object GitmojiSourceTypeMapper {

    fun fromId(id: GitmojiSourceType.Id, customJsonUrl: String = "", customLocalizationUrl: String = ""): GitmojiSourceType {
        return when (id) {
            GitmojiSourceType.Gitmoji.id -> GitmojiSourceType.Gitmoji
            GitmojiSourceType.ConventionalGitmoji.id -> GitmojiSourceType.ConventionalGitmoji
            GitmojiSourceType.Custom.ID -> GitmojiSourceType.Custom(customJsonUrl, customLocalizationUrl)
            else -> throw IllegalArgumentException("Unknown GitmojiSourceType id: ${id.value}")
        }
    }
}
