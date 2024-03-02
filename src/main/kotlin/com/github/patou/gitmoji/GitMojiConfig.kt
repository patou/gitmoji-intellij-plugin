package com.github.patou.gitmoji

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.util.Comparing
import java.awt.FlowLayout
import java.awt.GridLayout
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

class GitMojiConfig(private val project: Project) : SearchableConfigurable {
    private val mainPanel: JPanel
    private val useUnicode = JCheckBox("Use unicode emoji instead of text version (:code:)")
    private val displayEmoji =
        JCheckBox("Display emoji instead of icon in list (Bug in IntelliJ Windows or emoji in black and white)")
    private val insertInCursorPosition = JCheckBox("Insert the emoji in the cursor location")
    private val includeGitMojiDescription = JCheckBox("Include gitmoji description")
    private var useUnicodeConfig: Boolean = false
    private var displayEmojiConfig: String = "emoji"
    private var insertInCursorPositionConfig: Boolean = false
    private var includeGitMojiDescriptionConfig: Boolean = false
    private val textAfterUnicodeOptions = arrayOf("<nothing>", "<space>", ":", "(", "_", "[", "-")
    private val textAfterUnicode = ComboBox(textAfterUnicodeOptions)
    private val languageOptions = arrayOf("en_US", "zh_CN")
    private val languages = ComboBox(languageOptions)
    private var textAfterUnicodeConfig: String = " "
    private var languagesConfig:String = "en_US"

    override fun isModified(): Boolean =
        Configurable.isCheckboxModified(displayEmoji, displayEmojiConfig == "emoji") || Configurable.isCheckboxModified(
            useUnicode,
            useUnicodeConfig
        ) || isModified(textAfterUnicode, textAfterUnicodeConfig) || Configurable.isCheckboxModified(
            insertInCursorPosition,
            insertInCursorPositionConfig
        ) || Configurable.isCheckboxModified(includeGitMojiDescription, includeGitMojiDescriptionConfig)

    private fun isModified(comboBox: ComboBox<String>, value: String): Boolean {
        return !Comparing.equal(comboBox.selectedItem, value)
    }

    override fun getDisplayName(): String = "Gitmoji"
    override fun getId(): String = "com.github.patou.gitmoji.config"

    init {
        val flow = GridLayout(20, 2)
        mainPanel = JPanel(flow)
        mainPanel.add(displayEmoji, null)
        mainPanel.add(useUnicode, null)
        mainPanel.add(insertInCursorPosition, null)
        mainPanel.add(includeGitMojiDescription, null)
        val textAfterUnicodePanel = JPanel(FlowLayout(FlowLayout.LEADING))
        textAfterUnicodePanel.add(JLabel("Character after inserted emoji ✨"))
        textAfterUnicodePanel.add(textAfterUnicode, null)
        mainPanel.add(textAfterUnicodePanel)
        val languageJPanel = JPanel(FlowLayout(FlowLayout.LEADING))
        languageJPanel.add(JLabel("Language (Take effect after restart) "))
        languageJPanel.add(languages, null)
        mainPanel.add(languageJPanel)
    }

    override fun apply() {
        displayEmojiConfig = if (displayEmoji.isSelected) "emoji" else "icon"
        useUnicodeConfig = useUnicode.isSelected
        insertInCursorPositionConfig = insertInCursorPosition.isSelected
        includeGitMojiDescriptionConfig = includeGitMojiDescription.isSelected
        textAfterUnicodeConfig = when (textAfterUnicode.selectedIndex) {
            0 -> ""
            1 -> " "
            else -> textAfterUnicodeOptions[textAfterUnicode.selectedIndex]
        }
        languagesConfig = languageOptions[languages.selectedIndex]

        val projectInstance = PropertiesComponent.getInstance(project)
        val instance = PropertiesComponent.getInstance()
        projectInstance.setValue(CONFIG_DISPLAY_ICON, displayEmojiConfig)
        projectInstance.setValue(CONFIG_INSERT_IN_CURSOR_POSITION, insertInCursorPositionConfig)
        projectInstance.setValue(CONFIG_USE_UNICODE, useUnicodeConfig)
        projectInstance.setValue(CONFIG_INCLUDE_GITMOJI_DESCRIPTION, includeGitMojiDescriptionConfig)
        projectInstance.setValue(CONFIG_AFTER_UNICODE, textAfterUnicodeConfig)
        instance.setValue(CONFIG_LANGUAGE, languagesConfig)
    }

    override fun reset() {
        val propertiesComponent = PropertiesComponent.getInstance(project)
        val instance = PropertiesComponent.getInstance()

        displayEmojiConfig = propertiesComponent.getValue(CONFIG_DISPLAY_ICON, Gitmojis.defaultDisplayType())
        useUnicodeConfig = propertiesComponent.getBoolean(CONFIG_USE_UNICODE, false)
        insertInCursorPositionConfig = propertiesComponent.getBoolean(CONFIG_INSERT_IN_CURSOR_POSITION, false)
        includeGitMojiDescriptionConfig = propertiesComponent.getBoolean(CONFIG_INCLUDE_GITMOJI_DESCRIPTION, false)
        textAfterUnicodeConfig = propertiesComponent.getValue(CONFIG_AFTER_UNICODE, " ")
        languagesConfig = instance.getValue(CONFIG_LANGUAGE, "en_US")

        displayEmoji.isSelected = displayEmojiConfig == "emoji"
        useUnicode.isSelected = useUnicodeConfig
        insertInCursorPosition.isSelected = insertInCursorPositionConfig
        includeGitMojiDescription.isSelected = includeGitMojiDescriptionConfig
        textAfterUnicode.selectedIndex = when (textAfterUnicodeOptions.indexOf(textAfterUnicodeConfig)) {
            -1 -> if (textAfterUnicodeConfig == " ") 1 else 0
            else -> textAfterUnicodeOptions.indexOf(textAfterUnicodeConfig)
        }
        languages.selectedIndex = languageOptions.indexOf(languagesConfig)
    }

    override fun createComponent(): JComponent = mainPanel
}
