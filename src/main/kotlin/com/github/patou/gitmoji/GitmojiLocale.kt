package com.github.patou.gitmoji

import com.fasterxml.jackson.databind.ObjectMapper
import com.intellij.ide.util.PropertiesComponent
import org.yaml.snakeyaml.Yaml
import java.util.*

class GitmojiLocale {

    private var map: Map<String, Any> = emptyMap()

    init {
        val yaml = Yaml()
        val instance = PropertiesComponent.getInstance()
        val language = instance.getValue(CONFIG_LANGUAGE)
        var default = Locale.getDefault().toString()
        if (language != "auto") {
            default = language.toString()
        }
        javaClass.getResourceAsStream("/gitmojis-${default}.yaml").use { inputStream ->
            if (inputStream != null) {
                map = yaml.loadAs(inputStream, HashMap::class.java)
            }
        }
    }

    fun t(name: String, description: String): String {
        if (map.isEmpty()) {
            return description
        }
        // Maybe not very elegant, maybe it can be optimized ?
        return (map["gitmojis"] as Map<*, *>)[name]?.toString() ?: return description
    }

}