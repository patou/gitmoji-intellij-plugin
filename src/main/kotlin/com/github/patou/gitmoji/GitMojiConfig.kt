package com.github.patou.gitmoji

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import java.awt.BorderLayout
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JPanel

class GitMojiConfig constructor(val project: Project) : SearchableConfigurable {
    private val mainPanel: JPanel
    private val useUnicode = JCheckBox("Use unicode emoji instead of text version (:code:)")
    private var useUnicodeConfig : Boolean = false
    override fun isModified(): Boolean = isModified(useUnicode, useUnicodeConfig)
    override fun getDisplayName(): String = "Gitmoji"
    override fun getId(): String = "com.github.patou.gitmoji.config"
    init
    {
        mainPanel = JPanel(BorderLayout())
        mainPanel.add(BorderLayout.NORTH, useUnicode)
    }

    override fun apply() {
        useUnicodeConfig = useUnicode.isSelected
        PropertiesComponent.getInstance(project).setValue(CONFIG_USE_UNICODE, useUnicodeConfig)
    }

    override fun reset() {
        useUnicodeConfig = PropertiesComponent.getInstance(project).getBoolean(CONFIG_USE_UNICODE, false)
        useUnicode.isSelected = useUnicodeConfig
    }

    override fun createComponent(): JComponent? = mainPanel


}
