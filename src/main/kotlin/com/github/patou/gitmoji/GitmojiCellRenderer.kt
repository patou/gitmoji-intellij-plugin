package com.github.patou.gitmoji

import com.intellij.vcs.log.ui.table.VcsLogGraphTable
import com.intellij.vcs.log.ui.table.VcsLogIconCellRenderer

class GitmojiCellRenderer : VcsLogIconCellRenderer() {
    override fun customize(
        table: VcsLogGraphTable,
        value: Any?,
        selected: Boolean,
        hasFocus: Boolean,
        row: Int,
        column: Int
    ) {
        if (value == null || value !is GitmojiData) {
            return
        }
        if (value.name == "anguished") {
            append("-")
            return
        }
        icon = value.getIcon()
        toolTipText = value.localeDescription
    }
}
