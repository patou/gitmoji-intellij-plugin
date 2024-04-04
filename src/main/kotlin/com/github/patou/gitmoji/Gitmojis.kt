package com.github.patou.gitmoji

import com.google.gson.Gson
import okhttp3.*
import okhttp3.Request.Builder
import java.io.IOException

object Gitmojis {
    val gitmojis = ArrayList<GitmojiData>()

    init {
        loadGitmojiFromHTTP()
    }

    private fun loadGitmojiFromHTTP() {
        val client = OkHttpClient().newBuilder().addInterceptor(SafeGuardInterceptor()).build()
        val request: Request = Builder()
            .url("https://gitmoji.dev/api/gitmojis")
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                loadDefaultGitmoji()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) loadDefaultGitmoji()
                    else {
                        loadGitmoji(response.body!!.string())
                    }
                }
            }
        })
    }

    private fun loadDefaultGitmoji() {
        javaClass.getResourceAsStream("/gitmojis.json").use { inputStream ->
            if (inputStream != null) {
                val text = inputStream.bufferedReader().readText()
                loadGitmoji(text)
            }
        }
    }

    private fun loadGitmoji(text: String) {
        Gson().fromJson(text, JsonGitmojis::class.java).also {
            it.gitmojis.forEach { gitmoji ->
                gitmojis.add(GitmojiData(gitmoji.code, gitmoji.emoji, gitmoji.description, gitmoji.name))
            }
        }
        GitmojiLocale.loadTranslations()
    }

}