package com.github.patou.gitmoji
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class GitmojiStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        Gitmojis.ensureGitmojisLoaded();
    }
}