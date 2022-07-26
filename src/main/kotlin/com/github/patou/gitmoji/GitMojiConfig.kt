package com.github.patou.gitmoji

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.util.Comparing
import com.intellij.refactoring.suggested.main
import java.awt.FlowLayout
import java.awt.GridLayout
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextArea

class GitMojiConfig constructor(private val project: Project) : SearchableConfigurable {
    private val mainPanel: JPanel
    private val useUnicode = JCheckBox("Use unicode emoji instead of text version (:code:)")
    private val displayEmoji =
        JCheckBox("Display emoji instead of icon in list (Bug in IntelliJ Windows or emoji in black and white)")
    private val insertInCursorPosition =
        JCheckBox("Insert the emoji in the cursor location")
    private val includeGitMojiDescription =
        JCheckBox("Include gitmoji description")
    private val previewGitCommitMessage = JTextArea(2, 80)
    private var useUnicodeConfig: Boolean = false
    private var displayEmojiConfig: String = "emoji"
    private var insertInCursorPositionConfig: Boolean = false
    private var includeGitMojiDescriptionConfig: Boolean = false
    private val textAfterUnicodeOptions = arrayOf("<nothing>", "<space>", ":", "(", "_", "[", "-")
    private val textAfterUnicode = ComboBox(textAfterUnicodeOptions)
    private var textAfterUnicodeConfig: String = " "

    override fun isModified(): Boolean =
                previewCommit() &&
                isModified(displayEmoji, displayEmojiConfig == "emoji") ||
                isModified(useUnicode, useUnicodeConfig) ||
                isModified(textAfterUnicode, textAfterUnicodeConfig) ||
                isModified(insertInCursorPosition, insertInCursorPositionConfig) ||
                isModified(includeGitMojiDescription, includeGitMojiDescriptionConfig)

    private fun isModified(comboBox: ComboBox<String>, value: String): Boolean {
        return !Comparing.equal(comboBox.selectedItem, value)
    }

    override fun getDisplayName(): String = "Gitmoji"
    override fun getId(): String = "com.github.patou.gitmoji.config"

    init {
        val flow = GridLayout(20, 2)
        useUnicode.addChangeListener { previewCommit() }
        insertInCursorPosition.addChangeListener { previewCommit() }
        includeGitMojiDescription.addChangeListener { previewCommit() }
        mainPanel = JPanel(flow)
        mainPanel.add(displayEmoji, null)
        mainPanel.add(useUnicode, null)
        mainPanel.add(insertInCursorPosition, null)
        mainPanel.add(includeGitMojiDescription, null)
        val textAfterUnicodePanel = JPanel(FlowLayout(FlowLayout.LEADING))
        textAfterUnicodePanel.add(JLabel("Character after inserted emoji ✨"))
        textAfterUnicodePanel.add(textAfterUnicode, null)
        mainPanel.add(textAfterUnicodePanel)
        previewGitCommitMessage.isEditable = false
        mainPanel.add(JLabel("Preview"))
        mainPanel.add(button)
        mainPanel.add(previewGitCommitMessage)
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

        val projectInstance = PropertiesComponent.getInstance(project)
        projectInstance.setValue(CONFIG_DISPLAY_ICON, displayEmojiConfig)
        projectInstance.setValue(CONFIG_INSERT_IN_CURSOR_POSITION, insertInCursorPositionConfig)
        projectInstance.setValue(CONFIG_USE_UNICODE, useUnicodeConfig)
        projectInstance.setValue(CONFIG_INCLUDE_GITMOJI_DESCRIPTION, includeGitMojiDescriptionConfig)
        projectInstance.setValue(CONFIG_AFTER_UNICODE, textAfterUnicodeConfig)
        previewCommit()
    }
    override fun reset() {
        val propertiesComponent = PropertiesComponent.getInstance(project)

        displayEmojiConfig =
            propertiesComponent.getValue(CONFIG_DISPLAY_ICON, Gitmojis.defaultDisplayType())
        useUnicodeConfig = propertiesComponent.getBoolean(CONFIG_USE_UNICODE, false)
        insertInCursorPositionConfig = propertiesComponent.getBoolean(CONFIG_INSERT_IN_CURSOR_POSITION, false)
        includeGitMojiDescriptionConfig = propertiesComponent.getBoolean(CONFIG_INCLUDE_GITMOJI_DESCRIPTION, false)
        textAfterUnicodeConfig = propertiesComponent.getValue(CONFIG_AFTER_UNICODE, " ")

        displayEmoji.isSelected = displayEmojiConfig == "emoji"
        useUnicode.isSelected = useUnicodeConfig
        insertInCursorPosition.isSelected = insertInCursorPositionConfig
        includeGitMojiDescription.isSelected = includeGitMojiDescriptionConfig
        textAfterUnicode.selectedIndex =
            when (textAfterUnicodeOptions.indexOf(textAfterUnicodeConfig)) {
                -1 -> if (textAfterUnicodeConfig.equals(" ")) 1 else 0
                else -> textAfterUnicodeOptions.indexOf(textAfterUnicodeConfig)
            }
        previewCommit()
    }

    private fun previewCommit():Boolean {
        var message = ""
        if (insertInCursorPositionConfig) {
            message += "Commit message|"
        }
        if (useUnicodeConfig) {
            message += "✨"
        }
        else {
            message += ":sparkles:"
        }
        message += textAfterUnicodeConfig
        val selectionStart = message.length
        if (includeGitMojiDescriptionConfig) {
            message += "Introduce new features."
        }
        else if (!insertInCursorPositionConfig) {
            message += "Commit message"
        }
        previewGitCommitMessage.text = message
        previewGitCommitMessage.select(selectionStart, message.length)
        return true
    }

    override fun createComponent(): JComponent = mainPanel
}
