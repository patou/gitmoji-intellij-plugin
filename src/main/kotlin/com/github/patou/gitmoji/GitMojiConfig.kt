package com.github.patou.gitmoji

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import java.awt.BorderLayout
import javax.swing.*

class GitMojiConfig constructor(private val project: Project) : SearchableConfigurable {
    private val mainPanel: JPanel = JPanel(BorderLayout())
    private val useUnicode = JCheckBox("Use unicode emoji instead of text version (:code:)")
    private var useUnicodeConfig : Boolean = false
    private val textAfterUnicode = JTextField("Character / text displayed after unicode character. Default: ' '")
    private var textAfterUnicodeConfig : String = " "

    private val group : GroupLayout = GroupLayout(mainPanel)

    override fun isModified(): Boolean = isModified(useUnicode, useUnicodeConfig) || isModified(textAfterUnicode, textAfterUnicodeConfig)
    override fun getDisplayName(): String = "Gitmoji"
    override fun getId(): String = "com.github.patou.gitmoji.config"
    init
    {
        mainPanel.add(BorderLayout.NORTH, useUnicode)
        mainPanel.add(BorderLayout.AFTER_LAST_LINE, textAfterUnicode)
    }

    override fun apply() {
        useUnicodeConfig = useUnicode.isSelected
        textAfterUnicodeConfig = textAfterUnicode.text

        PropertiesComponent.getInstance(project).setValue(CONFIG_USE_UNICODE, useUnicodeConfig)
        PropertiesComponent.getInstance(project).setValue(CONFIG_AFTER_UNICODE, textAfterUnicodeConfig)
    }

    override fun reset() {
        useUnicodeConfig = PropertiesComponent.getInstance(project).getBoolean(CONFIG_USE_UNICODE, false)
        textAfterUnicodeConfig = PropertiesComponent.getInstance(project).getValue(CONFIG_AFTER_UNICODE, " ")
        useUnicode.isSelected = useUnicodeConfig
        textAfterUnicode.text = textAfterUnicodeConfig
    }

    override fun createComponent(): JComponent? = mainPanel


}
