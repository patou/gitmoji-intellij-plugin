package com.github.patou.gitmoji.source

import com.github.patou.gitmoji.CONFIG_GITMOJI_JSON_URL
import com.github.patou.gitmoji.CONFIG_GITMOJI_JSON_URL_DEFAULT
import com.github.patou.gitmoji.CONFIG_GITMOJI_SOURCE_TYPE
import com.github.patou.gitmoji.CONFIG_LOCALIZATION_URL
import com.github.patou.gitmoji.CONFIG_LOCALIZATION_URL_DEFAULT
import com.github.patou.gitmoji.ConfigUtil
import com.intellij.openapi.project.Project

object GitmojiSourceTypeProvider {

    fun provide(project: Project): GitmojiSourceType {
        val props = ConfigUtil.propsFor(project)
        val typeId = props.getValue(CONFIG_GITMOJI_SOURCE_TYPE)?.let(GitmojiSourceType::Id) ?: GitmojiSourceType.Gitmoji.id
        val jsonUrl = props.getValue(CONFIG_GITMOJI_JSON_URL, CONFIG_GITMOJI_JSON_URL_DEFAULT)
        val localizationUrl = props.getValue(CONFIG_LOCALIZATION_URL, CONFIG_LOCALIZATION_URL_DEFAULT)

        return GitmojiSourceTypeMapper.fromId(typeId, jsonUrl, localizationUrl)
    }
}