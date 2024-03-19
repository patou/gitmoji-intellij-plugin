package com.github.patou.gitmoji

import com.github.patou.gitmoji.Gitmojis.Companion.insertAt
import com.google.gson.Gson
import com.intellij.ide.TextCopyProvider
import com.intellij.ide.util.PropertiesComponent
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
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.awt.RelativePoint
import com.intellij.ui.speedSearch.SpeedSearchUtil.applySpeedSearchHighlighting
import com.intellij.util.ObjectUtils.sentinel
import com.intellij.util.containers.nullize
import com.intellij.util.ui.JBUI.scale
import com.intellij.vcs.commit.message.CommitMessageInspectionProfile.getSubjectRightMargin
import okhttp3.*
import okhttp3.Request.Builder
import java.awt.Point
import java.io.IOException
import javax.swing.JList
import javax.swing.ListSelectionModel

class GitCommitAction : AnAction() {
    private val gitmojis = ArrayList<GitmojiData>()

    init {
        isEnabledInModalContext = true
        loadGitmojiFromHTTP()
    }

    private val regexPattern = ":[a-z0-9_]+:"

    override fun isDumbAware(): Boolean {
        return true
    }

    override fun actionPerformed(actionEvent: AnActionEvent) {
        val project = actionEvent.project
        val commitMessage = getCommitMessage(actionEvent)
        when {
            commitMessage != null && project != null -> {
                createPopup(project, commitMessage, gitmojis)
                    .showInBestPositionFor(actionEvent.dataContext)
            }
        }
    }

    private fun createPopup(
        project: Project,
        commitMessage: CommitMessage,
        listGitmoji: List<GitmojiData>
    ): JBPopup {
        var chosenMessage: GitmojiData? = null
        var selectedMessage: GitmojiData? = null
        val rightMargin = getSubjectRightMargin(project)
        val previewCommandGroup = sentinel("Preview Commit Message")
        val projectInstance = PropertiesComponent.getInstance(project)
        val displayEmoji =
            projectInstance.getValue(CONFIG_DISPLAY_ICON, Gitmojis.defaultDisplayType()) == "emoji"
        val currentCommitMessage = commitMessage.editorField.text
        val currentOffset = commitMessage.editorField.caretModel.offset

        return JBPopupFactory.getInstance().createPopupChooserBuilder(listGitmoji)
            .setFont(commitMessage.editorField.editor?.colorsScheme?.getFont(EditorFontType.PLAIN))
            .setVisibleRowCount(7)
            .setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
            .setItemSelectedCallback {
                selectedMessage = it
                it?.let { preview(project, commitMessage, it, currentCommitMessage, currentOffset, previewCommandGroup) }
            }
            .setItemChosenCallback { chosenMessage = it }
            .setRenderer(object : ColoredListCellRenderer<GitmojiData>() {
                override fun customizeCellRenderer(
                    list: JList<out GitmojiData>,
                    value: GitmojiData,
                    index: Int,
                    selected: Boolean,
                    hasFocus: Boolean
                ) {
                    if (displayEmoji) {
                        append(" ${value.emoji}")
                    } else {
                        icon = value.getIcon()
                    }
                    append("\t${value.code} ", SimpleTextAttributes.GRAY_ITALIC_ATTRIBUTES)
                    appendTextPadding(5)
                    append(
                        first(
                            convertLineSeparators(value.description, RETURN_SYMBOL),
                            rightMargin,
                            false
                        )
                    )
                    applySpeedSearchHighlighting(list, this, true, selected)
                }
            })
            .addListener(object : JBPopupListener {
                override fun beforeShown(event: LightweightWindowEvent) {
                    val popup = event.asPopup()
                    val relativePoint =
                        RelativePoint(commitMessage.editorField, Point(0, -scale(3)))
                    val screenPoint =
                        Point(relativePoint.screenPoint).apply { translate(0, -popup.size.height) }

                    popup.setLocation(screenPoint)
                }

                override fun onClosed(event: LightweightWindowEvent) {
                    // IDEA-195094 Regression: New CTRL-E in "commit changes" breaks keyboard shortcuts
                    commitMessage.editorField.requestFocusInWindow()
                    // Use invokeLater() as onClosed() is called before callback from setItemChosenCallback
                    getApplication().invokeLater {
                        chosenMessage ?: cancelPreview(
                            project,
                            commitMessage
                        )
                    }
                }
            })
            .setNamerForFiltering { "${it.code} ${it.description}" }
            .setAutoPackHeightOnFiltering(false)
            .createPopup()
            .apply {
                setDataProvider { dataId ->
                    when (dataId) {
                        // default list action does not work as "CopyAction" is invoked first, but with other copy provider
                        PlatformDataKeys.COPY_PROVIDER.name -> object : TextCopyProvider() {
                            override fun getTextLinesToCopy() =
                                listOfNotNull(selectedMessage?.code).nullize()
                        }
                        else -> null
                    }
                }
            }
    }

    private fun preview(
        project: Project,
        commitMessage: CommitMessage,
        gitmoji: GitmojiData,
        currentCommitMessage: String,
        currentOffset: Int,
        groupId: Any
    ) =
        CommandProcessor.getInstance().executeCommand(project, {
            val projectInstance = PropertiesComponent.getInstance(project)
            val useUnicode = projectInstance.getBoolean(CONFIG_USE_UNICODE, false)
            val insertInCarretPosition = projectInstance.getBoolean(CONFIG_INSERT_IN_CURSOR_POSITION, false)
            val includeGitMojiDescription = projectInstance.getBoolean(CONFIG_INCLUDE_GITMOJI_DESCRIPTION, false)

            var message = currentCommitMessage // commitMessage.editorField.text
            val insertPosition = if (insertInCarretPosition) currentOffset else 0
            val textAfterUnicode = projectInstance.getValue(CONFIG_AFTER_UNICODE, " ")
            val selectedGitmoji = if (useUnicode) "${gitmoji.emoji}$textAfterUnicode" else "${gitmoji.code}$textAfterUnicode"
            var replaced = false
            if (!insertInCarretPosition) {
                if (useUnicode) {
                    for (moji in gitmojis) {
                        if (message.contains("${moji.emoji}$textAfterUnicode")) {
                            message = message.replaceFirst(
                                "${moji.emoji}$textAfterUnicode",
                                selectedGitmoji
                            )
                            replaced = true
                            break
                        }
                    }
                } else {
                    val actualRegex = Regex(regexPattern + Regex.escape(textAfterUnicode))
                    if (actualRegex.containsMatchIn(message)) {
                        message = actualRegex.replace(message, selectedGitmoji)
                        replaced = true
                    }
                }
            }
            if (!replaced) {
                message = insertAt(message, insertPosition, selectedGitmoji)
            }
            val startPosition = insertPosition + selectedGitmoji.length
            if (includeGitMojiDescription) {
                message = message.substring(0, startPosition) + gitmoji.description
            }
            commitMessage.setCommitMessage(message)

            if (!insertInCarretPosition) {
                commitMessage.editorField.selectAll()
                commitMessage.editorField.caretModel.removeSecondaryCarets()
                commitMessage.editorField.caretModel.primaryCaret.setSelection(
                    startPosition,
                    commitMessage.editorField.document.textLength,
                    false
                )
            }
            if (currentOffset < startPosition)
                commitMessage.editorField.caretModel.primaryCaret.moveToOffset(startPosition)
        }, "", groupId, commitMessage.editorField.document)

    private fun cancelPreview(project: Project, commitMessage: CommitMessage) {
        val manager = UndoManager.getInstance(project)
        val fileEditor = commitMessage.editorField.editor?.let {
            TextEditorProvider.getInstance().getTextEditor(it)
        }

        if (manager.isUndoAvailable(fileEditor)) manager.undo(fileEditor)
    }

    private fun getCommitMessage(e: AnActionEvent) =
        e.getData(VcsDataKeys.COMMIT_MESSAGE_CONTROL) as? CommitMessage

    private fun loadGitmojiFromHTTP() {
        val client = OkHttpClient().newBuilder().addInterceptor(SafeGuardInterceptor()).build()
        val request: Request = Builder()
                .url("https://gitmoji.dev/api/gitmojis")
                .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                loadDefaultGitmoji()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) loadDefaultGitmoji()
                    else {
                        loadGitmoji(response.body!!.string())
                    }
                }
            }
        })
    }

    private fun loadDefaultGitmoji() {
        javaClass.getResourceAsStream("/gitmojis.json").use { inputStream ->
            if (inputStream != null) {
                val text = inputStream.bufferedReader().readText()
                loadGitmoji(text)
            }
        }
    }

    private fun loadGitmoji(text: String) {
        val gitmojiLocale = GitmojiLocale()
        Gson().fromJson(text, Gitmojis::class.java).also {
            it.gitmojis.forEach { gitmoji ->
                gitmojis.add(GitmojiData(gitmoji.code, gitmoji.emoji, gitmojiLocale.t(gitmoji.name, gitmoji.description)))
            }
        }
    }

}
