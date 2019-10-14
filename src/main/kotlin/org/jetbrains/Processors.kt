package org.jetbrains

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.testFramework.PlatformTestUtil
import com.maddyhome.idea.vim.KeyHandler
import com.maddyhome.idea.vim.extension.VimExtensionHandler
import com.maddyhome.idea.vim.group.visual.vimSetSelection
import com.maddyhome.idea.vim.helper.inVisualMode
import com.maddyhome.idea.vim.helper.vimSelectionStart
import org.acejump.control.AceAction
import org.acejump.control.Handler
import org.acejump.label.Tagger
import org.acejump.search.Finder
import java.awt.Toolkit

/**
 * In order to implement an easymotion command you should implement [HandlerProcessor] and pass it to one of
 *   implementations of [EasyHandlerBase]
 */
interface HandlerProcessor {
    /** This function is called right after [AceAction] execution */
    fun customization(editor: Editor) {}

    /** This function is called right after user finished to work with AceJump/EasyMotion */
    fun onFinish(editor: Editor, queryWithSuffix: String) {}
}

/** Standard handled that is used in real work. For tests [TestObject.TestHandler] is used */
class StandardHandler(processor: HandlerProcessor) : EasyHandlerBase(processor) {
    override fun execute(editor: Editor, context: DataContext) {
        val systemQueue = Toolkit.getDefaultToolkit().systemEventQueue
        val loop = systemQueue.createSecondaryLoop()
        beforeAction(editor)

        Handler.addAceJumpListener(object : Handler.AceJumpListener {
            override fun finished() {
                finish(editor, Finder.query)
                Handler.removeAceJumpListener(this)
                loop.exit()
            }
        })

        KeyHandler.executeAction(AceAction(), context)

        rightAfterAction(editor)

        loop.enter()
    }
}

/** Object that contains test related staff */
object TestObject {
    var handlerWasCalled = false

    var handler: (editorText: String, jumpLocations: List<Int>) -> Unit = { _, _ -> }
    var inputQuery: () -> String = { "" }

    /** Handler that is used during unit tests */
    class TestHandler(processor: HandlerProcessor) : EasyHandlerBase(processor) {
        override fun execute(editor: Editor, context: DataContext) {
            handlerWasCalled = true

            beforeAction(editor)

            KeyHandler.executeAction(AceAction(), context)
            PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue()

            rightAfterAction(editor)

            PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue()
            val query = inputQuery()
            PlatformTestUtil.dispatchAllEventsInIdeEventQueue()
            finish(editor, query)

            handler(editor.document.text, Tagger.textMatches.sorted())
        }
    }
}

abstract class EasyHandlerBase(private val processor: HandlerProcessor) : VimExtensionHandler {

    private var startSelection: Int? = null

    protected fun beforeAction(editor: Editor) {
        startSelection = if (editor.inVisualMode) editor.caretModel.currentCaret.vimSelectionStart else null
    }

    protected fun rightAfterAction(editor: Editor) {
        processor.customization(editor)
    }

    protected fun finish(editor: Editor, queryWithSuffix: String) {
        processor.onFinish(editor, queryWithSuffix)
        startSelection?.let {
            editor.caretModel.currentCaret.vimSetSelection(it, editor.caretModel.offset, false)
        }
    }
}
