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

import com.intellij.ide.IdeEventQueue
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.ui.UIUtil
import com.maddyhome.idea.vim.KeyHandler
import com.maddyhome.idea.vim.ex.vimscript.VimScriptGlobalEnvironment
import com.maddyhome.idea.vim.group.visual.VimVisualTimer
import com.maddyhome.idea.vim.helper.EditorDataContext
import com.maddyhome.idea.vim.helper.StringHelper
import com.maddyhome.idea.vim.helper.TestInputModel
import com.maddyhome.idea.vim.option.OptionsManager
import com.maddyhome.idea.vim.option.ToggleOption
import org.acejump.control.Handler
import org.acejump.view.Canvas
import java.awt.Dimension
import javax.swing.JViewport
import javax.swing.KeyStroke

abstract class EasyMotionTestCase : BasePlatformTestCase() {
    override fun setUp() {
        super.setUp()
        (OptionsManager.getOption("easymotion") as ToggleOption).set()
    }

    protected fun doTest(
        command: List<KeyStroke>,
        editorText: String = text,
        searchQuery: String? = null,
        jumpToNthQuery: Int? = null,
        putCaretAtWord: String = "",
        caretShift: Int = 0,
        afterEditorSetup: (editor: Editor) -> Unit = {},
        test: (String, List<Int>) -> Unit = { _, _ -> }
    ) {
        setupEditor(editorText)
        if (putCaretAtWord.isNotEmpty()) {
            myFixture.editor.moveCaretBefore(putCaretAtWord, caretShift)
        }
        afterEditorSetup(myFixture.editor)

        TestObject.inputQuery = {
            searchQuery?.also {
                myFixture.type(it)
                PlatformTestUtil.dispatchAllEventsInIdeEventQueue()
            }
            var tag: String? = null
            if (jumpToNthQuery != null) {
                val locations = Canvas.jumpLocations
                if (locations.isNotEmpty()) {
                    tag = locations.toList()[jumpToNthQuery].tag
                    if (tag != null) {
                        myFixture.type(tag)
                    }
                }
            }
            (searchQuery ?: "") + (tag ?: "")
        }

        TestObject.handler = { str, offsets ->
            test(str, offsets)
        }

        typeText(command)
        assertTestHandlerWasCalled()
    }

    private fun setupEditor(before: String = text) {
        myFixture.configureByText(PlainTextFileType.INSTANCE, before)
        val viewPort = JViewport()
        viewPort.size = Dimension(0, 10 * myFixture.editor.lineHeight)
        (myFixture.editor as EditorImpl).scrollPane.viewport = viewPort
    }

    protected val text: String =
        """
                A Discovery

                I found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
        """.trimIndent()

    protected val iskeywordText = """
                    caseA oneA
                    case-twoA
                    case#threeA
                    case1fourA
                    middle
                    caseB oneB
                    case-twoB
                    case#threeB
                    case1fourB
                """.trimIndent()

    protected fun String.indentLineThatStartsWith(str: String): String {
        val index = this.indexOf(str)
        if (index < 0) throw RuntimeException("Wrong line number")

        return this.take(index) + " ".repeat(4) + this.substring(index)
    }


    protected fun parseKeysWithLeader(keys: String) = StringHelper.parseKeys("${EasyMotionExtension.defaultPrefix}$keys")

    protected fun typeText(keys: List<KeyStroke>) {
        val editor = myFixture.editor
        val keyHandler = KeyHandler.getInstance()
        val dataContext = EditorDataContext(editor)
        TestInputModel.getInstance(editor).setKeyStrokes(keys)

        val inputModel = TestInputModel.getInstance(editor)
        var key = inputModel.nextKeyStroke()
        while (key != null) {
            keyHandler.handleKey(editor, key, dataContext)
            key = inputModel.nextKeyStroke()
        }
        PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue()
    }

    private fun Editor.moveCaretBefore(str: String, addition: Int) {
        this.caretModel.moveToOffset(this.document.text.indexOf(str) + addition)
    }

    private fun assertTestHandlerWasCalled() {
        waitAndAssert(message = "Command was not executed") { TestObject.handlerWasCalled }
    }

    private inline fun waitAndAssert(timeInMillis: Int = 1000, message: String = "", condition: () -> Boolean) {
        val end = System.currentTimeMillis() + timeInMillis
        while (end > System.currentTimeMillis()) {
            Thread.sleep(10)
            IdeEventQueue.getInstance().flushQueue()
            if (condition()) return
        }
        kotlin.test.fail(message)
    }

    override fun tearDown() {
        Handler.reset()
        UIUtil.dispatchAllInvocationEvents()
        assertEmpty(myFixture.editor.markupModel.allHighlighters)
        TestObject.handlerWasCalled = false
        VimVisualTimer.swingTimer?.stop()
        VimScriptGlobalEnvironment.getInstance().variables[EasyMotionExtension.startOfLine] = 1
        super.tearDown()
    }
}