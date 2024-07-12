package com.github.patou.gitmoji
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.impl.VcsStartupActivity

class GitmojiStartupActivity : VcsStartupActivity {
    override val order = Integer.MAX_VALUE
    override fun runActivity(project: Project) {
        Gitmojis.ensureGitmojisLoaded();
    }
}