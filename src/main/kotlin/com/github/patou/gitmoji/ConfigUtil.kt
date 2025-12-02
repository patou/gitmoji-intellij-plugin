package com.github.patou.gitmoji

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.project.Project

object ConfigUtil {
    /**
     * Return PropertiesComponent to use for a given project.
     * Reads from project if CONFIG_USE_PROJECT_SETTINGS is true, otherwise from application.
     */
    fun propsFor(project: Project?): PropertiesComponent {
        val appProps = PropertiesComponent.getInstance()
        if (project == null) return appProps

        return try {
            val projProps = PropertiesComponent.getInstance(project)
            val useProject = projProps.getBoolean(CONFIG_USE_PROJECT_SETTINGS, false)
            if (useProject) projProps else appProps
        } catch (_: Exception) {
            appProps
        }
    }
}
