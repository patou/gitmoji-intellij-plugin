package com.github.patou.gitmoji

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import java.awt.FlowLayout
import java.awt.GridLayout
import javax.swing.*

class GitMojiConfig constructor(private val project: Project) : SearchableConfigurable {
    private val mainPanel: JPanel
    private val useUnicode = JCheckBox("Use unicode emoji instead of text version (:code:)")
    private var useUnicodeConfig: Boolean = false
    private val textAfterUnicodeOptions = arrayOf("<nothing>", "<space>", ":", "(", "_", "[")
    private val textAfterUnicode = ComboBox(textAfterUnicodeOptions)
    private var textAfterUnicodeConfig: String = " "

    override fun isModified(): Boolean = isModified(useUnicode, useUnicodeConfig) || isModified(textAfterUnicode, textAfterUnicodeConfig)
    override fun getDisplayName(): String = "Gitmoji"
    override fun getId(): String = "com.github.patou.gitmoji.config"

    init {
        val flow = GridLayout(20, 2)
        mainPanel = JPanel(flow)
        mainPanel.add(useUnicode, null)
        val textAfterUnicodePanel = JPanel(FlowLayout(FlowLayout.LEADING))
        textAfterUnicodePanel.add(JLabel("Character after inserted emoji ✨"))
        textAfterUnicodePanel.add(textAfterUnicode, null)
        mainPanel.add(textAfterUnicodePanel)
    }

    override fun apply() {
        useUnicodeConfig = useUnicode.isSelected
        textAfterUnicodeConfig = when (textAfterUnicode.selectedIndex) {
            0 -> ""
            1 -> " "
            else -> textAfterUnicodeOptions[textAfterUnicode.selectedIndex]
        }

        val projectInstance = PropertiesComponent.getInstance(project)
        projectInstance.setValue(CONFIG_USE_UNICODE, useUnicodeConfig)
        projectInstance.setValue(CONFIG_AFTER_UNICODE, textAfterUnicodeConfig)
    }

    override fun reset() {
        val propertiesComponent = PropertiesComponent.getInstance(project)

        useUnicodeConfig = propertiesComponent.getBoolean(CONFIG_USE_UNICODE, false)
        textAfterUnicodeConfig = propertiesComponent.getValue(CONFIG_AFTER_UNICODE, " ")

        useUnicode.isSelected = useUnicodeConfig
        textAfterUnicode.selectedIndex = when (textAfterUnicodeOptions.indexOf(textAfterUnicodeConfig)) {
            -1 -> 1
            else -> textAfterUnicodeOptions.indexOf(textAfterUnicodeConfig)
        }
    }

    override fun createComponent(): JComponent = mainPanel
}
