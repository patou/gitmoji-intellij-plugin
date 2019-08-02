package com.github.patou.gitmoji

import com.intellij.ide.TextCopyProvider
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.application.ApplicationManager.getApplication
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.undo.UndoManager
import com.intellij.openapi.editor.actions.ContentChooser.RETURN_SYMBOL
import com.intellij.openapi.editor.colors.EditorFontType
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.JBPopupListener
import com.intellij.openapi.ui.popup.LightweightWindowEvent
import com.intellij.openapi.util.text.StringUtil.convertLineSeparators
import com.intellij.openapi.util.text.StringUtil.first
import com.intellij.openapi.vcs.VcsDataKeys
import com.intellij.openapi.vcs.ui.CommitMessage
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.awt.RelativePoint
import com.intellij.ui.speedSearch.SpeedSearchUtil.applySpeedSearchHighlighting
import com.intellij.util.ObjectUtils.sentinel
import com.intellij.util.containers.nullize
import com.intellij.util.ui.JBUI.scale
import com.intellij.vcs.commit.message.CommitMessageInspectionProfile.getSubjectRightMargin
import java.awt.Point
import javax.swing.JList
import javax.swing.ListSelectionModel

class GitCommitAction : AnAction() {
    val gitmojis = ArrayList<GitmojiData>()

    init {
        isEnabledInModalContext = true
        gitmojis.add(GitmojiData("art", "Improving structure / format of the code."));
        gitmojis.add(GitmojiData("zap", "Improving performance."));
        gitmojis.add(GitmojiData("fire", "Removing code or files."));
        gitmojis.add(GitmojiData("bug", "Fixing a bug."));
        gitmojis.add(GitmojiData("ambulance", "Critical hotfix."));
        gitmojis.add(GitmojiData("sparkles", "Introducing new features."));
        gitmojis.add(GitmojiData("pencil", "Writing docs."));
        gitmojis.add(GitmojiData("rocket", "Deploying stuff."));
        gitmojis.add(GitmojiData("lipstick", "Updating the UI and style files."));
        gitmojis.add(GitmojiData("tada", "Initial commit."));
        gitmojis.add(GitmojiData("white_check_mark", "Updating tests."));
        gitmojis.add(GitmojiData("lock", "Fixing security issues."));
        gitmojis.add(GitmojiData("apple", "Fixing something on macOS."));
        gitmojis.add(GitmojiData("penguin", "Fixing something on Linux."));
        gitmojis.add(GitmojiData("checkered_flag", "Fixing something on Windows."));
        gitmojis.add(GitmojiData("robot", "Fixing something on Android."));
        gitmojis.add(GitmojiData("green_apple", "Fixing something on iOS."));
        gitmojis.add(GitmojiData("bookmark", "Releasing / Version tags."));
        gitmojis.add(GitmojiData("rotating_light", "Removing linter warnings."));
        gitmojis.add(GitmojiData("construction", "Work in progress."));
        gitmojis.add(GitmojiData("green_heart", "Fixing CI Build."));
        gitmojis.add(GitmojiData("arrow_down", "Downgrading dependencies."));
        gitmojis.add(GitmojiData("arrow_up", "Upgrading dependencies."));
        gitmojis.add(GitmojiData("pushpin", "Pinning dependencies to specific versions."));
        gitmojis.add(GitmojiData("construction_worker", "Adding CI build system."));
        gitmojis.add(GitmojiData("chart_with_upwards_trend", "Adding analytics or tracking code."));
        gitmojis.add(GitmojiData("recycle", "Refactoring code."));
        gitmojis.add(GitmojiData("heavy_minus_sign", "Removing a dependency."));
        gitmojis.add(GitmojiData("whale", "Work about Docker."));
        gitmojis.add(GitmojiData("heavy_plus_sign", "Adding a dependency."));
        gitmojis.add(GitmojiData("wrench", "Changing configuration files."));
        gitmojis.add(GitmojiData("globe_with_meridians", "Internationalization and localization."));
        gitmojis.add(GitmojiData("pencil2", "Fixing typos."));
        gitmojis.add(GitmojiData("poop", "Writing bad code that needs to be improved."));
        gitmojis.add(GitmojiData("rewind", "Reverting changes."));
        gitmojis.add(GitmojiData("twisted_rightwards_arrows", "Merging branches."));
        gitmojis.add(GitmojiData("package", "Updating compiled files or packages."));
        gitmojis.add(GitmojiData("alien", "Updating code due to external API changes."));
        gitmojis.add(GitmojiData("truck", "Moving or renaming files."));
        gitmojis.add(GitmojiData("page_facing_up", "Adding or updating license."));
        gitmojis.add(GitmojiData("boom", "Introducing breaking changes."));
        gitmojis.add(GitmojiData("bento", "Adding or updating assets."));
        gitmojis.add(GitmojiData("ok_hand", "Updating code due to code review changes."));
        gitmojis.add(GitmojiData("wheelchair", "Improving accessibility."));
        gitmojis.add(GitmojiData("bulb", "Documenting source code."));
        gitmojis.add(GitmojiData("beers", "Writing code drunkenly."));
        gitmojis.add(GitmojiData("speech_balloon", "Updating text and literals."));
        gitmojis.add(GitmojiData("card_file_box", "Performing database related changes."));
        gitmojis.add(GitmojiData("loud_sound", "Adding logs."));
        gitmojis.add(GitmojiData("mute", "Removing logs."));
        gitmojis.add(GitmojiData("busts_in_silhouette", "Add contributor(s)."));
        gitmojis.add(GitmojiData("children_crossing", "Improving user experience / usability."));
        gitmojis.add(GitmojiData("building_construction", "Making architectural changes."));
        gitmojis.add(GitmojiData("iphone", "Working on responsive design."));
        gitmojis.add(GitmojiData("clown_face", "Mocking things."));
        gitmojis.add(GitmojiData("egg", "Adding an easter egg."));
        gitmojis.add(GitmojiData("see_no_evil", "Adding or updating a .gitignore file."));
        gitmojis.add(GitmojiData("camera_flash", "Adding or updating snapshots."));
        gitmojis.add(GitmojiData("alembic", "Experimenting new things."));
        gitmojis.add(GitmojiData("mag", "Improving SEO."));
        gitmojis.add(GitmojiData("wheel_of_dharma", "Work about Kubernetes."));
        gitmojis.add(GitmojiData("label", "Adding or updating types (Flow, TypeScript)"));
    }

    val codeRegex = Regex(":[a-z0-9_]+:")


    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project!!
        val commitMessage = getCommitMessage(e)!!

        createPopup(project, commitMessage, gitmojis)
            .showInBestPositionFor(e.dataContext)
    }

    private fun createPopup(project: Project, commitMessage: CommitMessage, listGitmoji: List<GitmojiData>): JBPopup {
        var chosenMessage: GitmojiData? = null
        var selectedMessage: GitmojiData? = null
        val rightMargin = getSubjectRightMargin(project)
        val previewCommandGroup = sentinel("Preview Commit Message")

        return JBPopupFactory.getInstance().createPopupChooserBuilder(listGitmoji)
            .setFont(commitMessage.editorField.editor?.colorsScheme?.getFont(EditorFontType.PLAIN))
            .setVisibleRowCount(7)
            .setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
            .setItemSelectedCallback {
                selectedMessage = it
                it?.let { preview(project, commitMessage, it, previewCommandGroup) }
            }
            .setItemChosenCallback { chosenMessage = it }
            .setRenderer(object : ColoredListCellRenderer<GitmojiData>() {
                override fun customizeCellRenderer(list: JList<out GitmojiData>, value: GitmojiData, index: Int, selected: Boolean, hasFocus: Boolean) {
                    icon = value.icon
                    append(value.emotji)
                    append(" ")
                    append(first(convertLineSeparators(value.description, RETURN_SYMBOL), rightMargin, false))
                    applySpeedSearchHighlighting(list, this, true, selected)
                }
            })
            .addListener(object : JBPopupListener {
                override fun beforeShown(event: LightweightWindowEvent) {
                    val popup = event.asPopup()
                    val relativePoint = RelativePoint(commitMessage.editorField, Point(0, -scale(3)))
                    val screenPoint = Point(relativePoint.screenPoint).apply { translate(0, -popup.size.height) }

                    popup.setLocation(screenPoint)
                }

                override fun onClosed(event: LightweightWindowEvent) {
                    // IDEA-195094 Regression: New CTRL-E in "commit changes" breaks keyboard shortcuts
                    commitMessage.editorField.requestFocusInWindow()
                    // Use invokeLater() as onClosed() is called before callback from setItemChosenCallback
                    getApplication().invokeLater { chosenMessage ?: cancelPreview(project, commitMessage) }
                }
            })
            .setNamerForFiltering { it.code + " " + it.description }
            .setAutoPackHeightOnFiltering(false)
            .createPopup()
            .apply {
                setDataProvider { dataId ->
                    when (dataId) {
                        // default list action does not work as "CopyAction" is invoked first, but with other copy provider
                        PlatformDataKeys.COPY_PROVIDER.name -> object : TextCopyProvider() {
                            override fun getTextLinesToCopy() = listOfNotNull(selectedMessage?.emotji).nullize()
                        }
                        else -> null
                    }
                }
            }
    }

    private fun preview(project: Project,
                        commitMessage: CommitMessage,
                        gitmoji: GitmojiData,
                        groupId: Any) =
        CommandProcessor.getInstance().executeCommand(project, {
            var message = commitMessage.editorField.text ?: "";
            if (codeRegex.containsMatchIn(message)) {
                message = codeRegex.replace(message, gitmoji.emotji)
            }
            else {
                message = gitmoji.emotji + " " + message
            }
            commitMessage.setCommitMessage(message)
            commitMessage.editorField.selectAll()
            commitMessage.editorField.caretModel.removeSecondaryCarets();
            commitMessage.editorField.caretModel.primaryCaret.setSelection(gitmoji.emotji.length, commitMessage.editorField.getDocument().getTextLength(), false);
        }, "", groupId, commitMessage.editorField.document)

    private fun cancelPreview(project: Project, commitMessage: CommitMessage) {
        val manager = UndoManager.getInstance(project)
        val fileEditor = commitMessage.editorField.editor?.let { TextEditorProvider.getInstance().getTextEditor(it) }

        if (manager.isUndoAvailable(fileEditor)) manager.undo(fileEditor)
    }

    private fun getCommitMessage(e: AnActionEvent) = e.getData(VcsDataKeys.COMMIT_MESSAGE_CONTROL) as? CommitMessage
}
