package com.github.patou.gitmoji

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.project.ProjectManager
import com.intellij.vcs.log.ui.table.GraphTableModel
import com.intellij.vcs.log.ui.table.VcsLogGraphTable
import com.intellij.vcs.log.ui.table.column.VcsLogCustomColumn
import javax.swing.table.TableCellRenderer


@Suppress("UnstableApiUsage")
class GitmojiCustomColumn : VcsLogCustomColumn<GitmojiData> {
    override val id: String
        get() = "gitmoji"
    override val isDynamic: Boolean
        get() = true
    override val localizedName: String
        get() = "Gitmoji"
    override val isResizable: Boolean
        get() = false

    override fun createTableCellRenderer(table: VcsLogGraphTable): TableCellRenderer {
        return GitmojiCellRenderer()
    }

    override fun isEnabledByDefault(): Boolean {
        val projectInstance = PropertiesComponent.getInstance(ProjectManager.getInstance().defaultProject)
        val useUnicode = projectInstance.getBoolean(CONFIG_USE_UNICODE, false)
        return useUnicode
    }

    override fun getStubValue(model: GraphTableModel): GitmojiData {
        return GitmojiData("anguished", "😧", "anguished", "anguished")
    }

    override fun getValue(model: GraphTableModel, row: Int): GitmojiData? {
        model.getCommitMetadata(row).let {
            for (moji in Gitmojis.gitmojis) {
                if (it.subject.contains(moji.emoji) || it.subject.contains(moji.code)) {
                    return moji
                }
            }
        }
        return null
    }

}