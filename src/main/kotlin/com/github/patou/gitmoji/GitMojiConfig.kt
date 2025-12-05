package com.github.patou.gitmoji

import com.github.patou.gitmoji.source.GitmojiSourceType
import com.github.patou.gitmoji.source.GitmojiSourceTypeMapper
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.util.Comparing
import java.awt.Color
import java.awt.Cursor
import java.awt.Desktop
import java.awt.FlowLayout
import java.awt.GridLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.net.URI
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

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

    private val gitmojiSourceField = ComboBox(GitmojiSourceType.OPTIONS)
    private val gitmojiJsonUrlField = JTextField()
    private val localizationUrlField = JTextField()
    private var gitmojiJsonPanel: JPanel
    private var localizationPanel: JPanel
    private val sourceTooltipLabel: JLabel = JLabel()
    private var gitmojiSourceConfig: GitmojiSourceType = GitmojiSourceType.Gitmoji
    private var gitmojiJsonUrlConfig: String = ""
    private var localizationUrlConfig: String = ""

    override fun isModified(): Boolean =
        Configurable.isCheckboxModified(useProjectSettings, useProjectSettingsConfig) ||
        Configurable.isCheckboxModified(displayEmoji, displayEmojiConfig == "emoji") ||
        Configurable.isCheckboxModified(useUnicode, useUnicodeConfig) ||
        isModified(textAfterUnicode, textAfterUnicodeConfig) ||
        isModified(languages, languagesConfig) ||
        Configurable.isCheckboxModified(insertInCursorPosition, insertInCursorPositionConfig) ||
        Configurable.isCheckboxModified(includeGitMojiDescription, includeGitMojiDescriptionConfig) ||
        isModified(gitmojiSourceField, gitmojiSourceConfig.id) ||
        gitmojiJsonUrlField.text != gitmojiJsonUrlConfig ||
        localizationUrlField.text != localizationUrlConfig

    private fun isModified(comboBox: ComboBox<String>, value: String): Boolean {
        return !Comparing.equal(comboBox.selectedItem, value)
    }

    private fun <T> isModified(comboBox: ComboBox<OptionItem<T>>, value: T): Boolean {
        val selectedItem = comboBox.selectedItem as? OptionItem<*>
        return !Comparing.equal(selectedItem?.id, value)
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

        // Gitmoji Source Type panel
        val gitmojiSourcePanel = JPanel(FlowLayout(FlowLayout.LEADING))
        gitmojiSourcePanel.add(JLabel(GitmojiBundle.message("config.source.type")))
        gitmojiSourceField.renderer = OptionItemRenderer()
        gitmojiSourcePanel.add(gitmojiSourceField, null)

        sourceTooltipLabel.text = gitmojiSourceConfig.tooltipText
        sourceTooltipLabel.font = sourceTooltipLabel.font.deriveFont((sourceTooltipLabel.font.size - 2).toFloat())
        sourceTooltipLabel.foreground = Color(120, 120, 120)
        sourceTooltipLabel.toolTipText = gitmojiSourceConfig.tooltipUrl
        sourceTooltipLabel.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        sourceTooltipLabel.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                try {
                    val url = sourceTooltipLabel.toolTipText
                    if (!url.isNullOrBlank() && Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().browse(URI(url))
                    }
                } catch (_: Exception) {
                    // ignore errors opening link
                }
            }
        })

        gitmojiSourcePanel.add(sourceTooltipLabel)
        mainPanel.add(gitmojiSourcePanel)

        gitmojiSourceField.addItemListener {
            val selectedId = getCurrentGitmojiSourceId()
            val type = GitmojiSourceTypeMapper.fromId(selectedId, gitmojiJsonUrlField.text.trim(), localizationUrlField.text.trim())
            setGitmojiSourceFieldsVisibility(type)
            updateSourceTooltip(type)
        }

        // Gitmoji JSON URL panel
        gitmojiJsonPanel = JPanel(FlowLayout(FlowLayout.LEADING))
        gitmojiJsonPanel.add(JLabel(GitmojiBundle.message("config.source.jsonUrl")))
        gitmojiJsonPanel.add(gitmojiJsonUrlField, null)
        mainPanel.add(gitmojiJsonPanel)

        // Localization URL panel
        localizationPanel = JPanel(FlowLayout(FlowLayout.LEADING))
        localizationPanel.add(JLabel(GitmojiBundle.message("config.source.localizationUrl")))
        localizationPanel.add(localizationUrlField, null)
        mainPanel.add(localizationPanel)
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
        gitmojiJsonUrlConfig = gitmojiJsonUrlField.text.trim()
        localizationUrlConfig = localizationUrlField.text.trim()
        val gitmojiSourceConfigId = getCurrentGitmojiSourceId()
        gitmojiSourceConfig = GitmojiSourceTypeMapper.fromId(gitmojiSourceConfigId, gitmojiJsonUrlConfig, localizationUrlConfig)

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
        propsToSave.setValue(CONFIG_GITMOJI_SOURCE_TYPE, gitmojiSourceConfig.id.value)
        propsToSave.setValue(CONFIG_GITMOJI_JSON_URL, gitmojiJsonUrlConfig)
        propsToSave.setValue(CONFIG_LOCALIZATION_URL, localizationUrlConfig)

        // If we just unchecked, remove project settings
        if (wasProjectSettings && !useProjectSettingsConfig) {
            clearProjectSettings(projectProps)
        }

        GitmojiLocale.loadTranslations(project)
        Gitmojis.gitmojis.clear()
        Gitmojis.ensureGitmojisLoaded(project)
    }

    private fun clearProjectSettings(props: PropertiesComponent) {
        try {
            props.unsetValue(CONFIG_DISPLAY_ICON)
            props.unsetValue(CONFIG_INSERT_IN_CURSOR_POSITION)
            props.unsetValue(CONFIG_USE_UNICODE)
            props.unsetValue(CONFIG_INCLUDE_GITMOJI_DESCRIPTION)
            props.unsetValue(CONFIG_AFTER_UNICODE)
            props.unsetValue(CONFIG_LANGUAGE)
            props.unsetValue(CONFIG_GITMOJI_SOURCE_TYPE)
            props.unsetValue(CONFIG_GITMOJI_JSON_URL)
            props.unsetValue(CONFIG_LOCALIZATION_URL)
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
        gitmojiJsonUrlConfig = props.getValue(CONFIG_GITMOJI_JSON_URL, CONFIG_GITMOJI_JSON_URL_DEFAULT)
        localizationUrlConfig = props.getValue(CONFIG_LOCALIZATION_URL, CONFIG_LOCALIZATION_URL_DEFAULT)
        val gitmojiSourceConfigId = (props.getValue(CONFIG_GITMOJI_SOURCE_TYPE, GitmojiSourceType.Gitmoji.id.value)).let(GitmojiSourceType::Id)
        gitmojiSourceConfig = GitmojiSourceTypeMapper.fromId(gitmojiSourceConfigId, gitmojiJsonUrlConfig, localizationUrlConfig)

        displayEmoji.isSelected = displayEmojiConfig == "emoji"
        useUnicode.isSelected = useUnicodeConfig
        insertInCursorPosition.isSelected = insertInCursorPositionConfig
        includeGitMojiDescription.isSelected = includeGitMojiDescriptionConfig
        textAfterUnicode.selectedIndex = when (textAfterUnicodeOptions.indexOf(textAfterUnicodeConfig)) {
            -1 -> if (textAfterUnicodeConfig == " ") 1 else 0
            else -> textAfterUnicodeOptions.indexOf(textAfterUnicodeConfig)
        }
        languages.selectedIndex = languageOptions.indexOf(languagesConfig)
        gitmojiSourceField.selectedIndex = GitmojiSourceType.OPTIONS.indexOfFirst { it.id == gitmojiSourceConfig.id }
        gitmojiJsonUrlField.text = gitmojiJsonUrlConfig
        localizationUrlField.text = localizationUrlConfig
        setGitmojiSourceFieldsVisibility(gitmojiSourceConfig)
        updateSourceTooltip(gitmojiSourceConfig)
    }

    private fun setGitmojiSourceFieldsVisibility(type: GitmojiSourceType) {
        val gitmojiSourceFieldsVisibility = type.id == GitmojiSourceType.Custom.ID
        gitmojiJsonPanel.isVisible = gitmojiSourceFieldsVisibility
        localizationPanel.isVisible = gitmojiSourceFieldsVisibility
    }

    private fun updateSourceTooltip(type: GitmojiSourceType) {
        sourceTooltipLabel.text = type.tooltipText
        sourceTooltipLabel.toolTipText = type.tooltipUrl
    }

    private fun getCurrentGitmojiSourceId(): GitmojiSourceType.Id {
        return (gitmojiSourceField.selectedItem as OptionItem<*>).id as GitmojiSourceType.Id
    }

    override fun createComponent(): JComponent = mainPanel
}
