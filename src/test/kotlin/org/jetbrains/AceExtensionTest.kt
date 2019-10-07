package org.jetbrains

import com.intellij.ide.IdeEventQueue
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.ui.UIUtil
import com.maddyhome.idea.vim.KeyHandler
import com.maddyhome.idea.vim.VimPlugin
import com.maddyhome.idea.vim.command.CommandState
import com.maddyhome.idea.vim.group.visual.VimVisualTimer
import com.maddyhome.idea.vim.group.visual.vimSetSelection
import com.maddyhome.idea.vim.helper.EditorDataContext
import com.maddyhome.idea.vim.helper.StringHelper.parseKeys
import com.maddyhome.idea.vim.helper.TestInputModel
import com.maddyhome.idea.vim.helper.mode
import com.maddyhome.idea.vim.option.OptionsManager
import com.maddyhome.idea.vim.option.ToggleOption
import org.acejump.control.Handler
import org.acejump.view.Canvas
import org.jetbrains.AceExtension.Companion.defaultPrefix
import java.awt.Dimension
import javax.swing.JViewport
import javax.swing.KeyStroke

class AceExtensionTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        (OptionsManager.getOption("acejump") as ToggleOption).set()
    }

    fun `test save selection`() {
        doTest(
            command = parseKeysWithLeader("s"),
            searchQuery = "found",
            jumpToQuery = true,
            afterEditorSetup = {
                VimPlugin.getVisualMotion().enterVisualMode(it)
                it.caretModel.currentCaret.vimSetSelection(0, 2)
            }
        ) { editorText, _ ->
            assertEquals(CommandState.Mode.VISUAL, myFixture.editor.mode)
            myFixture.editor.caretModel.currentCaret.let { caret ->
                assertEquals(0, caret.selectionStart)
                assertEquals(editorText.indexOf("found") + 1, caret.selectionEnd)
            }
        }
    }

    fun `test bidirectional mapping`() {
        doTest(
            command = parseKeysWithLeader("s"),
            searchQuery = "found"
        ) { _, matches ->
            assertEquals(1, matches.size)
        }
    }

    fun `test bidirectional line motion`() {
        doTest(
            command = parseKeys(command("bd-jk")),
            editorText = text.indentLineThatStartsWith("I found"),
            putCaretAtWord = "all"
        ) { editorText, jumpLocations ->
            assertEquals(6, jumpLocations.size)
            assertEquals(editorText.indexOf("I found"), jumpLocations[2])
        }
    }

    fun `test forward line motion`() {
        doTest(
            command = parseKeysWithLeader("j"),
            editorText = text.indentLineThatStartsWith("where"),
            putCaretAtWord = "all"
        ) { editorText, jumpLocations ->
            // It should probably be one less jump location because currently AceJump includes the current line
            assertEquals(3, jumpLocations.size)
            assertEquals(editorText.indexOf("where"), jumpLocations[1])
        }
    }

    fun `test backward line motion`() {
        doTest(
            command = parseKeysWithLeader("k"),
            editorText = text.indentLineThatStartsWith("I found"),
            putCaretAtWord = "lavender"
        ) { editorText, jumpLocations ->
            // It should probably be one less jump location because currently AceJump includes the current line
            assertEquals(4, jumpLocations.size)
            assertEquals(editorText.indexOf("I found"), jumpLocations[2])
        }
    }

    fun `test backward line motion sol`() {
        doTest(
            command = parseKeys(command("sol-k")),
            editorText = text.indentLineThatStartsWith("I found"),
            putCaretAtWord = "lavender"
        ) { editorText, jumpLocations ->
            // It should probably be one less jump location because currently AceJump includes the current line
            assertEquals(4, jumpLocations.size)
            assertEquals(editorText.indexOf("I found") - 4, jumpLocations[2])
        }
    }

    fun `test backward line motion eol`() {
        doTest(
            command = parseKeys(command("eol-k")),
            editorText = text.indentLineThatStartsWith("I found"),
            putCaretAtWord = "lavender"
        ) { editorText, jumpLocations ->
            // It should probably be one less jump location because currently AceJump includes the current line
            assertEquals(3, jumpLocations.size)
            assertEquals(editorText.indexOf("land") + 4, jumpLocations[2])
        }
    }

    fun `test forward line motion sol`() {
        doTest(
            command = parseKeys(command("sol-j")),
            editorText = text.indentLineThatStartsWith("where"),
            putCaretAtWord = "lavender"
        ) { editorText, jumpLocations ->
            // It should probably be one less jump location because currently AceJump includes the current line
            assertEquals(2, jumpLocations.size)
            assertEquals(editorText.indexOf("where") - 4, jumpLocations[0])
        }
    }

    fun `test forward line motion eol`() {
        doTest(
            command = parseKeys(command("eol-j")),
            editorText = text.indentLineThatStartsWith("where"),
            putCaretAtWord = "lavender"
        ) { editorText, jumpLocations ->
            // Bug in AceJump. Should be 3
            assertEquals(2, jumpLocations.size)
            assertEquals(editorText.indexOf("sand") + 4, jumpLocations[1])
        }
    }

    fun `test forward word motion`() {
        doTest(
            command = parseKeysWithLeader("w"),
            putCaretAtWord = "lavender",
            caretShift = 2
        ) { _, jumpLocations ->
            assertEquals(19, jumpLocations.size)
            assertTrue(text.indexOf("settled") in jumpLocations)
            assertTrue(text.indexOf("found") !in jumpLocations)
        }
    }

    fun `test backward word motion`() {
        doTest(
            command = parseKeysWithLeader("b"),
            putCaretAtWord = "lavender",
            caretShift = 2
        ) { _, jumpLocations ->
            assertEquals(13, jumpLocations.size)
            assertTrue(text.indexOf("settled") !in jumpLocations)
            assertTrue(text.indexOf("found") in jumpLocations)
        }
    }

    fun `test both directions word motion`() {
        doTest(
            command = parseKeys(command("bd-w")),
            putCaretAtWord = "lavender",
            caretShift = 2
        ) { _, jumpLocations ->
            assertEquals(13 + 19, jumpLocations.size)
            assertTrue(text.indexOf("settled") in jumpLocations)
            assertTrue(text.indexOf("found") in jumpLocations)
        }
    }

    fun `test forward mapping`() {
        doTest(
            command = parseKeysWithLeader("f"),
            putCaretAtWord = "lavender",
            searchQuery = "it"
        ) { editorText, matches ->
            assertEquals(1, matches.size)
            assertEquals(editorText.lastIndexOf("it"), matches[0])
        }
    }

    private fun doTest(
        command: MutableList<KeyStroke>,
        editorText: String = text,
        searchQuery: String? = null,
        jumpToQuery: Boolean = false,
        putCaretAtWord: String = "",
        caretShift: Int = 0,
        afterEditorSetup: (editor: Editor) -> Unit = {},
        test: (String, List<Int>) -> Unit
    ) {
        setupEditor(editorText)
        afterEditorSetup(myFixture.editor)
        if (putCaretAtWord.isNotEmpty()) {
            myFixture.editor.moveCaretBefore(putCaretAtWord, caretShift)
        }

        TestProcessor.inputQuery = {
            searchQuery?.let {
                myFixture.type(it)
                PlatformTestUtil.dispatchAllEventsInIdeEventQueue()
                if (jumpToQuery) {
                    val locations = Canvas.jumpLocations
                    if (locations.isNotEmpty()) {
                        val tag = locations.toList()[0].tag ?: return@let
                        myFixture.type(tag)
                    }
                }
            }
        }

        TestProcessor.handler = { str, offsets ->
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

    private val text: String =
        """
                A Discovery

                I found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
        """.trimIndent()

    private fun String.indentLineThatStartsWith(str: String): String {
        val index = this.indexOf(str)
        if (index < 0) throw RuntimeException("Wrong line number")

        return this.take(index) + " ".repeat(4) + this.substring(index)
    }


    private fun parseKeysWithLeader(keys: String) = parseKeys("$defaultPrefix$keys")

    private fun typeText(keys: List<KeyStroke>) {
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
        waitAndAssert(message = "Command was not executed") { TestProcessor.handlerWasCalled }
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
        TestProcessor.handlerWasCalled = false
        VimVisualTimer.swingTimer?.stop()
        super.tearDown()
    }
}

