/*
 * IdeaVim-EasyMotion. Easymotion emulator plugin for IdeaVim.
 * Copyright (C) 2019-2020  Alex Plate
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.jetbrains.plugins.extension.easymotion

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.testFramework.PlatformTestUtil
import com.maddyhome.idea.vim.KeyHandler
import com.maddyhome.idea.vim.VimPlugin
import com.maddyhome.idea.vim.command.CommandState
import com.maddyhome.idea.vim.command.CommandState.SubMode.VISUAL_CHARACTER
import com.maddyhome.idea.vim.command.CommandState.SubMode.VISUAL_LINE
import com.maddyhome.idea.vim.command.MappingMode
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
abstract class HandlerProcessor(val motionType: MotionType) {
    /** This function is called right after [AceAction] execution */
    open fun customization(editor: Editor) {}

    /** This function is called right after user finished to work with AceJump/EasyMotion */
    open fun onFinish(editor: Editor, queryWithSuffix: String) {}
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
    private var initialOffset: Int? = null

    protected fun beforeAction(editor: Editor) {
        startSelection = if (editor.inVisualMode) editor.caretModel.currentCaret.vimSelectionStart else null
        initialOffset = editor.caretModel.currentCaret.offset
    }

    protected fun rightAfterAction(editor: Editor) {
        // Add position to jump list
        VimPlugin.getMark().saveJumpLocation(editor)
        processor.customization(editor)
    }

    protected fun finish(editor: Editor, queryWithSuffix: String) {
        processor.onFinish(editor, queryWithSuffix)
        startSelection?.let {
            editor.caretModel.currentCaret.vimSetSelection(it, editor.caretModel.offset, false)
        }

        // Inclusive / Exclusive / Linewise for op mode
        val myInitialOffset = initialOffset
        if (myInitialOffset != null && CommandState.getInstance(editor).mappingMode == MappingMode.OP_PENDING) {
            val selectionType = when (processor.motionType) {
                MotionType.LINE -> VISUAL_LINE
                MotionType.INCLUSIVE -> VISUAL_CHARACTER
                MotionType.BIDIRECTIONAL_INCLUSIVE -> {
                    if (myInitialOffset < editor.caretModel.currentCaret.offset) VISUAL_CHARACTER else null
                }
                else -> null
            }
            if (selectionType != null) {
                VimPlugin.getVisualMotion().enterVisualMode(editor, selectionType)
                editor.caretModel.currentCaret.vimSetSelection(myInitialOffset, editor.caretModel.currentCaret.offset)
            }
        }

        // Remove position from jumps list if caret haven't moved
        if (myInitialOffset == editor.caretModel.offset) {
            VimPlugin.getMark().jumps.dropLast(1)
        }

        initialOffset = null
    }
}

enum class MotionType {
    INCLUSIVE,
    EXCLUSIVE,
    BIDIRECTIONAL_INCLUSIVE,
    LINE
}
