package com.github.patou.gitmoji

import org.yaml.snakeyaml.Yaml
import java.util.*

class GitmojiLocale {

    private var map: Map<String, String> = emptyMap()


    init {
        val yaml = Yaml()
        val default = Locale.getDefault()
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
        return map.get("gitmojis") ?: return description
    }

}