package com.github.patou.gitmoji

import com.github.patou.gitmoji.source.GitmojiSourceTypeProvider
import com.google.gson.Gson
import com.intellij.openapi.project.Project
import okhttp3.*
import okhttp3.Request.Builder
import java.io.IOException

object Gitmojis {
    val gitmojis = ArrayList<GitmojiData>()

    private fun loadGitmojiFromHTTP(project: Project) {
        val sourceType = GitmojiSourceTypeProvider.provide(project)
        val client = OkHttpClient().newBuilder().addInterceptor(SafeGuardInterceptor()).build()
        val request: Request = Builder()
            .url(sourceType.jsonUrl)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                loadDefaultGitmoji(project)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) loadDefaultGitmoji(project)
                    else {
                        loadGitmoji(project, response.body.string())
                    }
                }
            }
        })
    }

    private fun loadDefaultGitmoji(project: Project) {
        javaClass.getResourceAsStream("/gitmojis.json").use { inputStream ->
            if (inputStream != null) {
                val text = inputStream.bufferedReader().readText()
                loadGitmoji(project, text)
            }
        }
    }

    private fun loadGitmoji(project: Project, text: String) {
        Gson().fromJson(text, JsonGitmojis::class.java).also {
            it.gitmojis.forEach { gitmoji ->
                gitmojis.add(GitmojiData(gitmoji.code, gitmoji.emoji, gitmoji.description, gitmoji.name))
            }
        }
        GitmojiLocale.loadTranslations(project)
    }

    fun ensureGitmojisLoaded(project: Project) {
        if (gitmojis.isEmpty()) {
            loadGitmojiFromHTTP(project)
        }
    }

}