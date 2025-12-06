package com.github.patou.gitmoji

import java.awt.Component
import javax.swing.DefaultListCellRenderer
import javax.swing.JList

data class OptionItem<ID>(val id: ID, val name: String)

class OptionItemRenderer : DefaultListCellRenderer() {

    override fun getListCellRendererComponent(
        list: JList<*>?,
        value: Any?,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean
    ): Component {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
        text = when (value) {
            is OptionItem<*> -> value.name
            else -> value?.toString() ?: ""
        }
        return this
    }
}
