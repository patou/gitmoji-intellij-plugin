package com.github.patou.gitmoji

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.diagnostic.Logger
import okhttp3.*
import org.yaml.snakeyaml.Yaml
import java.io.IOException
import java.util.*

object GitmojiLocale {

    private val logger = Logger.getInstance(GitmojiLocale::class.java)
    private var translations: MutableMap<String, String> = HashMap()

    val LANGUAGE_CONFIG_LIST = arrayOf("auto", "en_US", "zh_CN", "fr_FR", "ru_RU", "pt_BR")

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
        if (language == "en_US") {
            // no need to load english translations, as they are the default
            return
        }
        val requestUrl = instance.getValue(CONFIG_LOCALIZATION_URL, CONFIG_LOCALIZATION_URL_DEFAULT)
            .replace("{locale}", language)
        val client = OkHttpClient().newBuilder().addInterceptor(SafeGuardInterceptor()).build()
        val request: Request = Request.Builder()
            .url(requestUrl)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                logger.warn("Failed to download translations for $language, falling back to local", e)
                loadLocalYaml(language)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        logger.warn("Unsuccessful response (${response.code}) when downloading translations for $language, falling back to local")
                        loadLocalYaml(language)
                    } else {
                        val bodyText = try {
                            response.body?.string()
                        } catch (e: Exception) {
                            logger.warn("Failed to read response body for translations ($language), falling back to local", e)
                            null
                        }

                        if (bodyText.isNullOrEmpty()) {
                            loadLocalYaml(language)
                        } else {
                            try {
                                loadYaml(bodyText)
                            } catch (e: Exception) {
                                // parsing failed (malformed YAML) -> fallback local
                                logger.warn("Failed to parse remote YAML translations for $language, falling back to local", e)
                                loadLocalYaml(language)
                            }
                        }
                    }
                }
            }
        })
    }

    // load local yaml
    private fun loadLocalYaml(language: String) {
        val yaml = Yaml()
        try {
            javaClass.getResourceAsStream("/gitmojis-${language}.yaml").use { inputStream ->
                if (inputStream != null) {
                    try {
                        addTranslation(yaml.loadAs(inputStream, HashMap::class.java))
                    } catch (e: Exception) {
                        logger.error("Failed to parse local YAML translations for $language", e)
                    }
                } else {
                    logger.warn("Local translations resource not found for $language")
                }
            }
        } catch (e: Exception) {
            logger.error("Unexpected error while loading local translations for $language", e)
        }
    }

    // load remote yaml
    private fun loadYaml(text: String) {
        val yaml = Yaml()
        try {
            addTranslation(yaml.loadAs(text, HashMap::class.java))
        } catch (e: Exception) {
            logger.error("Failed to parse YAML translations from text", e)
            throw e
        }
    }

    private fun addTranslation(loadedTranslation : HashMap<*, *>) {
        loadedTranslation["gitmojis"]?.let { gitmojis ->
            if (gitmojis is Map<*, *>) {
                gitmojis.forEach { (key, value) ->
                    translations[key.toString()] = value.toString()
                }
            }
        }
    }
}
