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
    private val useProjectSettings = JCheckBox("Use project-specific settings (instead of global)")
    private val useUnicode = JCheckBox(GitmojiBundle.message("config.useUnicode"))
    private val displayEmoji =
        JCheckBox(GitmojiBundle.message("config.displayEmoji"))
    private val insertInCursorPosition = JCheckBox(GitmojiBundle.message("config.insertInCursorPosition"))
    private val includeGitMojiDescription = JCheckBox(GitmojiBundle.message("config.includeGitMojiDescription"))
    private var useProjectSettingsConfig: Boolean = false
    private var useUnicodeConfig: Boolean = false
    private var displayEmojiConfig: String = "emoji"
    private var insertInCursorPositionConfig: Boolean = false
    private var includeGitMojiDescriptionConfig: Boolean = false
    private val textAfterUnicodeOptions = arrayOf("<nothing>", "<space>", ":", "(", "_", "[", "-")
    private val textAfterUnicode = ComboBox(textAfterUnicodeOptions)
    private val languageOptions = GitmojiLocale.LANGUAGE_CONFIG_LIST
    private val languages = ComboBox(languageOptions)
    private var textAfterUnicodeConfig: String = " "
    private var languagesConfig:String = "auto"

    override fun isModified(): Boolean =
        Configurable.isCheckboxModified(useProjectSettings, useProjectSettingsConfig) ||
        Configurable.isCheckboxModified(displayEmoji, displayEmojiConfig == "emoji") ||
        Configurable.isCheckboxModified(useUnicode, useUnicodeConfig) ||
        isModified(textAfterUnicode, textAfterUnicodeConfig) ||
        Configurable.isCheckboxModified(insertInCursorPosition, insertInCursorPositionConfig) ||
        Configurable.isCheckboxModified(includeGitMojiDescription, includeGitMojiDescriptionConfig)

    private fun isModified(comboBox: ComboBox<String>, value: String): Boolean {
        return !Comparing.equal(comboBox.selectedItem, value)
    }

    override fun getDisplayName(): String = GitmojiBundle.message("projectName")
    override fun getId(): String = "com.github.patou.gitmoji.config"

    init {
        val flow = GridLayout(20, 2)
        mainPanel = JPanel(flow)
        mainPanel.add(useProjectSettings, null)
        mainPanel.add(JPanel(), null) // empty cell
        mainPanel.add(displayEmoji, null)
        mainPanel.add(useUnicode, null)
        mainPanel.add(insertInCursorPosition, null)
        mainPanel.add(includeGitMojiDescription, null)
        val textAfterUnicodePanel = JPanel(FlowLayout(FlowLayout.LEADING))
        textAfterUnicodePanel.add(JLabel("Character after inserted emoji ✨"))
        textAfterUnicodePanel.add(textAfterUnicode, null)
        mainPanel.add(textAfterUnicodePanel)
        val languageJPanel = JPanel(FlowLayout(FlowLayout.LEADING))
        languageJPanel.add(JLabel(GitmojiBundle.message("config.language")))
        languageJPanel.add(languages, null)
        mainPanel.add(languageJPanel)
    }

    override fun apply() {
        val wasProjectSettings = useProjectSettingsConfig
        useProjectSettingsConfig = useProjectSettings.isSelected

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

        val projectProps = PropertiesComponent.getInstance(project)
        val appProps = PropertiesComponent.getInstance()

        // Save the toggle itself in project
        projectProps.setValue(CONFIG_USE_PROJECT_SETTINGS, useProjectSettingsConfig)

        val propsToSave = if (useProjectSettingsConfig) projectProps else appProps
        propsToSave.setValue(CONFIG_DISPLAY_ICON, displayEmojiConfig)
        propsToSave.setValue(CONFIG_INSERT_IN_CURSOR_POSITION, insertInCursorPositionConfig)
        propsToSave.setValue(CONFIG_USE_UNICODE, useUnicodeConfig)
        propsToSave.setValue(CONFIG_INCLUDE_GITMOJI_DESCRIPTION, includeGitMojiDescriptionConfig)
        propsToSave.setValue(CONFIG_AFTER_UNICODE, textAfterUnicodeConfig)
        propsToSave.setValue(CONFIG_LANGUAGE, languagesConfig)

        // If we just unchecked, remove project settings
        if (wasProjectSettings && !useProjectSettingsConfig) {
            clearProjectSettings(projectProps)
        }

        GitmojiLocale.loadTranslations()
    }

    private fun clearProjectSettings(props: PropertiesComponent) {
        try {
            props.unsetValue(CONFIG_DISPLAY_ICON)
            props.unsetValue(CONFIG_INSERT_IN_CURSOR_POSITION)
            props.unsetValue(CONFIG_USE_UNICODE)
            props.unsetValue(CONFIG_INCLUDE_GITMOJI_DESCRIPTION)
            props.unsetValue(CONFIG_AFTER_UNICODE)
            props.unsetValue(CONFIG_LANGUAGE)
        } catch (_: Exception) {
            // Ignore errors during cleanup
        }
    }

    override fun reset() {
        val projectProps = PropertiesComponent.getInstance(project)
        val appProps = PropertiesComponent.getInstance()

        // Check if using project settings
        useProjectSettingsConfig = projectProps.getBoolean(CONFIG_USE_PROJECT_SETTINGS, false)
        useProjectSettings.isSelected = useProjectSettingsConfig

        // Load from appropriate source
        val props = if (useProjectSettingsConfig) projectProps else appProps

        displayEmojiConfig = props.getValue(CONFIG_DISPLAY_ICON, defaultDisplayType())
        useUnicodeConfig = props.getBoolean(CONFIG_USE_UNICODE, false)
        insertInCursorPositionConfig = props.getBoolean(CONFIG_INSERT_IN_CURSOR_POSITION, false)
        includeGitMojiDescriptionConfig = props.getBoolean(CONFIG_INCLUDE_GITMOJI_DESCRIPTION, false)
        textAfterUnicodeConfig = props.getValue(CONFIG_AFTER_UNICODE, " ")
        languagesConfig = props.getValue(CONFIG_LANGUAGE, "auto")

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
