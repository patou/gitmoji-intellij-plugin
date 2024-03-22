package com.github.patou.gitmoji

import com.intellij.ide.util.PropertiesComponent
import okhttp3.*
import org.yaml.snakeyaml.Yaml
import java.io.IOException
import java.util.*

class GitmojiLocale {

    private var map: Map<String, Any> = emptyMap()

    companion object {
        val LANGUAGE_CONFIG_LIST = arrayOf("auto", "en_US", "zh_CN")
    }

    fun t(name: String, description: String): String {
        if (map.isEmpty()) {
            return description
        }
        // Maybe not very elegant, maybe it can be optimized ?
        return (map["gitmojis"] as Map<*, *>)[name]?.toString() ?: return description
    }

    fun loadMap(onMapLoaded:() -> Unit) {
        val instance = PropertiesComponent.getInstance()
        val language = instance.getValue(CONFIG_LANGUAGE)
        var defaultLanguage = Locale.getDefault().toString()
        if (language != null && language != "auto") {
            defaultLanguage = language.toString()
        }
        val client = OkHttpClient().newBuilder().addInterceptor(SafeGuardInterceptor()).build()
        val request: Request = Request.Builder()
            .url("https://raw.githubusercontent.com/caiyucong/gitmoji-intellij-plugin-chinese/t/gitmojis-${defaultLanguage}.yaml")
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                loadLocaleYaml(defaultLanguage)
                onMapLoaded()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        loadLocaleYaml(defaultLanguage)
                        onMapLoaded()
                    } else {
                        loadYaml(response.body!!.string())
                        onMapLoaded()
                    }
                }
            }
        })
    }

    private fun loadLocaleYaml(language: String) {
        val yaml = Yaml()
        javaClass.getResourceAsStream("/gitmojis-${language}.yaml").use { inputStream ->
            if (inputStream != null) {
                map = yaml.loadAs(inputStream, HashMap::class.java)
            }
        }
    }

    private fun loadYaml(text: String) {
        val yaml = Yaml()
        map = yaml.loadAs(text, HashMap::class.java)
    }
}
