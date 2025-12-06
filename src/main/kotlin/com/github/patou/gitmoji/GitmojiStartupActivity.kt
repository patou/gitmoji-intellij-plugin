package com.github.patou.gitmoji

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.ui.Messages

class GitmojiStartupActivity : ProjectActivity {
    private val logger = Logger.getInstance(GitmojiStartupActivity::class.java)


    companion object {
        private val MIGRATION_KEYS = listOf(
            CONFIG_DISPLAY_ICON,
            CONFIG_INSERT_IN_CURSOR_POSITION,
            CONFIG_USE_UNICODE,
            CONFIG_INCLUDE_GITMOJI_DESCRIPTION,
            CONFIG_AFTER_UNICODE,
            CONFIG_LANGUAGE
        )
    }

    override suspend fun execute(project: Project) {
        offerMigrationIfNeeded(project)
        Gitmojis.ensureGitmojisLoaded(project)
    }

    private fun offerMigrationIfNeeded(project: Project) {
        val projectProps = PropertiesComponent.getInstance(project)
        val appProps = PropertiesComponent.getInstance()

        logger.debug("Starting migration check for project: ${project.name}")

        // Check if already migrated or already has project settings toggle
        val migrationDoneKey = "com.github.patou.gitmoji.migration-offered"
        val alreadyMigrated = projectProps.getBoolean(migrationDoneKey, false)

        if (alreadyMigrated) {
            logger.debug("Migration already offered for project: ${project.name}")
            return
        }

        // Check if CONFIG_USE_PROJECT_SETTINGS is already set (new version)
        val hasNewConfig = projectProps.getValue(CONFIG_USE_PROJECT_SETTINGS) != null

        if (hasNewConfig) {
            logger.debug("Project already using new config system: ${project.name}")
            projectProps.setValue(migrationDoneKey, true)
            return
        }

        // Check if project has old settings (stored per-project before migration)
        val hasOldProjectSettings = MIGRATION_KEYS.any { projectProps.getValue(it) != null }

        logger.debug("Old project settings present: ${MIGRATION_KEYS.filter { projectProps.getValue(it) != null }}")

        if (!hasOldProjectSettings) {
            logger.debug("No old settings found, marking as migrated")
            projectProps.setValue(migrationDoneKey, true)
            return
        }

        logger.info("Old Gitmoji settings detected in project '${project.name}', offering migration dialog")

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
                logger.info("Migrating Gitmoji settings to global for project '${project.name}'")
                migrateToGlobal(projectProps, appProps)
                projectProps.setValue(CONFIG_USE_PROJECT_SETTINGS, false)
            } else {
                logger.info("Keeping project-specific Gitmoji settings for project '${project.name}'")
                projectProps.setValue(CONFIG_USE_PROJECT_SETTINGS, true)
            }

            projectProps.setValue(migrationDoneKey, true)
        }
    }

    private fun migrateToGlobal(projectProps: PropertiesComponent, appProps: PropertiesComponent) {

        var migratedCount = 0
        for (key in MIGRATION_KEYS) {
            projectProps.getValue(key)?.let { value ->
                if (appProps.getValue(key) == null) {
                    appProps.setValue(key, value)
                    logger.debug("Migrated $key: $value")
                    migratedCount++
                } else {
                    logger.debug("Skipped $key (global already has value)")
                }
                try {
                    projectProps.unsetValue(key)
                } catch (e: Exception) {
                    logger.warn("Failed to remove $key from project", e)
                }
            }
        }

        logger.info("Migration completed: $migratedCount setting(s) migrated to global")
    }
}