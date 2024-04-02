package com.github.patou.gitmoji

import com.intellij.ide.util.PropertiesComponent
import okhttp3.*
import org.yaml.snakeyaml.Yaml
import java.io.IOException
import java.util.*

object GitmojiLocale {

    private var translations: MutableMap<String, String> = HashMap()

    val LANGUAGE_CONFIG_LIST = arrayOf("auto", "en_US", "zh_CN")

    fun t(name: String, description: String): String {
        if (translations.isEmpty()) {
            return description
        }
        // Maybe not very elegant, maybe it can be optimized ?
        return translations[name] ?: return description
    }

    fun loadTranslations() {
        translations.clear()
        val instance = PropertiesComponent.getInstance()
        var language = instance.getValue(CONFIG_LANGUAGE) ?: "auto"
        if (language == "auto") {
            val defaultLanguage = Locale.getDefault().toString()
            if (LANGUAGE_CONFIG_LIST.contains(defaultLanguage)) {
                language = defaultLanguage
            }
            else {
                language = "en_US"
            }
        }
        val client = OkHttpClient().newBuilder().addInterceptor(SafeGuardInterceptor()).build()
        val request: Request = Request.Builder()
            .url("https://raw.githubusercontent.com/patou/gitmoji-intellij-plugin/master/src/main/resources/gitmojis-${language}.yaml")
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                loadLocalYaml(language)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        loadLocalYaml(language)
                    } else {
                        loadYaml(response.body!!.string())
                    }
                }
            }
        })
    }

    // load local yaml
    private fun loadLocalYaml(language: String) {
        val yaml = Yaml()
        javaClass.getResourceAsStream("/gitmojis-${language}.yaml").use { inputStream ->
            if (inputStream != null) {
                addTranslation(yaml.loadAs(inputStream, HashMap::class.java))
            }
        }
    }

    // load remote yaml
    private fun loadYaml(text: String) {
        val yaml = Yaml()
        addTranslation(yaml.loadAs(text, HashMap::class.java))
    }

    private fun addTranslation(loadedTranslation : HashMap<String, Any>) {
        loadedTranslation["gitmojis"]?.let { it ->
            if (it is Map<*, *>) {
                it.forEach { (key, value) ->
                    translations[key.toString()] = value.toString()
                }
            }
        }
    }
}
