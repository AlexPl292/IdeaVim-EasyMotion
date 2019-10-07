package org.jetbrains

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.testFramework.PlatformTestUtil
import com.maddyhome.idea.vim.KeyHandler
import com.maddyhome.idea.vim.extension.VimExtensionHandler
import com.maddyhome.idea.vim.group.visual.vimSetSelection
import com.maddyhome.idea.vim.helper.vimSelectionStart
import org.acejump.control.AceAction
import org.acejump.control.Handler
import org.acejump.label.Tagger
import java.awt.Toolkit

interface HandlerProcessor {
    fun customization() {}
    fun onFinish(editor: Editor) {}
}

fun makeHandler(processor: HandlerProcessor): VimExtensionHandler {
    return if (ApplicationManager.getApplication().isUnitTestMode) {
        TestProcessor.TestHandler(processor)
    } else {
        StandardHandler(processor)
    }
}

class StandardHandler(processor: HandlerProcessor) : EasyHandler(processor) {
    override fun execute(editor: Editor, context: DataContext) {
        val systemQueue = Toolkit.getDefaultToolkit().systemEventQueue
        val loop = systemQueue.createSecondaryLoop()
        beforeAction(editor)

        Handler.addAceJumpListener(object : Handler.AceJumpListener {
            override fun finished() {
                finish(editor)
                Handler.removeAceJumpListener(this)
                loop.exit()
            }
        })

        KeyHandler.executeAction(AceAction(), context)

        rightAfterAction()

        loop.enter()
    }
}

object TestProcessor {
    var handlerWasCalled = false

    var handler: (editorText: String, jumpLocations: List<Int>) -> Unit = { _, _ -> }
    var inputQuery: () -> Unit = {}

    class TestHandler(processor: HandlerProcessor) : EasyHandler(processor) {
        override fun execute(editor: Editor, context: DataContext) {
            handlerWasCalled = true

            beforeAction(editor)

            KeyHandler.executeAction(AceAction(), context)
            PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue()

            rightAfterAction()

            PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue()
            inputQuery()
            PlatformTestUtil.dispatchAllEventsInIdeEventQueue()
            finish(editor)

            handler(editor.document.text, Tagger.textMatches.sorted())
        }
    }
}

abstract class EasyHandler(private val processor: HandlerProcessor) : VimExtensionHandler {

    private var startSelection: Int? = null

    fun beforeAction(editor: Editor) {
        startSelection = if (editor.selectionModel.hasSelection()) {
            editor.caretModel.currentCaret.vimSelectionStart
        } else null
    }

    fun rightAfterAction() {
        processor.customization()
    }

    fun finish(editor: Editor) {
        processor.onFinish(editor)
        startSelection?.let {
            editor.caretModel.currentCaret.vimSetSelection(it, editor.caretModel.offset, false)
        }
    }
}
