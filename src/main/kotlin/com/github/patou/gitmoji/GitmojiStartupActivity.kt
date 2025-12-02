package com.github.patou.gitmoji

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.ui.Messages

class GitmojiStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        offerMigrationIfNeeded(project)
        Gitmojis.ensureGitmojisLoaded()
    }

    private fun offerMigrationIfNeeded(project: Project) {
        val projectProps = PropertiesComponent.getInstance(project)
        val appProps = PropertiesComponent.getInstance()

        println("[Gitmoji Migration] Starting migration check for project: ${project.name}")

        // Check if already migrated or already has project settings toggle
        val migrationDoneKey = "com.github.patou.gitmoji.migration-offered"
        val alreadyMigrated = projectProps.getBoolean(migrationDoneKey, false)

        println("[Gitmoji Migration] Already migrated flag: $alreadyMigrated")

        if (alreadyMigrated) {
            println("[Gitmoji Migration] Migration already offered for project: ${project.name}")
            return
        }

        // Check if CONFIG_USE_PROJECT_SETTINGS is already set (new version)
        val hasNewConfig = projectProps.getValue(CONFIG_USE_PROJECT_SETTINGS) != null
        println("[Gitmoji Migration] Has new config system: $hasNewConfig")

        if (hasNewConfig) {
            println("[Gitmoji Migration] Project already using new config system: ${project.name}")
            projectProps.setValue(migrationDoneKey, true)
            return
        }

        // Check if project has old settings (stored per-project before migration)
        // Check for both string values and the keys existence
        val displayIcon = projectProps.getValue(CONFIG_DISPLAY_ICON)
        val useUnicode = projectProps.getValue(CONFIG_USE_UNICODE)
        val afterUnicode = projectProps.getValue(CONFIG_AFTER_UNICODE)
        val insertPos = projectProps.getValue(CONFIG_INSERT_IN_CURSOR_POSITION)
        val includeDesc = projectProps.getValue(CONFIG_INCLUDE_GITMOJI_DESCRIPTION)
        val language = projectProps.getValue(CONFIG_LANGUAGE)

        println("[Gitmoji Migration] Old settings check:")
        println("[Gitmoji Migration]   CONFIG_DISPLAY_ICON: $displayIcon")
        println("[Gitmoji Migration]   CONFIG_USE_UNICODE: $useUnicode")
        println("[Gitmoji Migration]   CONFIG_AFTER_UNICODE: $afterUnicode")
        println("[Gitmoji Migration]   CONFIG_INSERT_IN_CURSOR_POSITION: $insertPos")
        println("[Gitmoji Migration]   CONFIG_INCLUDE_GITMOJI_DESCRIPTION: $includeDesc")
        println("[Gitmoji Migration]   CONFIG_LANGUAGE: $language")

        val hasOldProjectSettings =
            displayIcon != null ||
            useUnicode != null ||
            afterUnicode != null ||
            insertPos != null ||
            includeDesc != null ||
            language != null

        println("[Gitmoji Migration] Has old project settings: $hasOldProjectSettings")

        if (!hasOldProjectSettings) {
            // No old settings, mark as migrated
            println("[Gitmoji Migration] No old settings found, marking as migrated")
            projectProps.setValue(migrationDoneKey, true)
            return
        }

        println("[Gitmoji Migration] Showing migration dialog for project: ${project.name}")

        // Ask user what to do with old project settings
        ApplicationManager.getApplication().invokeLater {
            val result = Messages.showYesNoDialog(
                project,
                "This project has Gitmoji settings stored per-project from a previous version.\n\n" +
                "Would you like to migrate them to global settings?\n" +
                "(You can still use project-specific settings later via the checkbox in Settings)\n\n" +
                "• Yes: Move settings to global and remove from project\n" +
                "• No: Keep settings in this project (enable project-specific mode)",
                "Gitmoji Settings Migration",
                "Migrate to Global",
                "Keep in Project",
                Messages.getQuestionIcon()
            )

            if (result == Messages.YES) {
                // Migrate to global
                migrateToGlobal(projectProps, appProps)
                projectProps.setValue(CONFIG_USE_PROJECT_SETTINGS, false)
            } else {
                // Keep in project - enable project-specific mode
                projectProps.setValue(CONFIG_USE_PROJECT_SETTINGS, true)
            }

            projectProps.setValue(migrationDoneKey, true)
        }
    }

    private fun migrateToGlobal(projectProps: PropertiesComponent, appProps: PropertiesComponent) {
        val keys = listOf(
            CONFIG_DISPLAY_ICON,
            CONFIG_INSERT_IN_CURSOR_POSITION,
            CONFIG_USE_UNICODE,
            CONFIG_INCLUDE_GITMOJI_DESCRIPTION,
            CONFIG_AFTER_UNICODE,
            CONFIG_LANGUAGE
        )

        for (key in keys) {
            projectProps.getValue(key)?.let { value ->
                // Only migrate if global doesn't have a value yet
                if (appProps.getValue(key) == null) {
                    appProps.setValue(key, value)
                }
                // Remove from project
                try {
                    projectProps.unsetValue(key)
                } catch (_: Exception) {
                    // Ignore errors during cleanup
                }
            }
        }
    }
}