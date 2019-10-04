package org.jetbrains

import com.intellij.ide.IdeEventQueue
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.ui.UIUtil
import com.maddyhome.idea.vim.KeyHandler
import com.maddyhome.idea.vim.helper.EditorDataContext
import com.maddyhome.idea.vim.helper.StringHelper.parseKeys
import com.maddyhome.idea.vim.helper.TestInputModel
import com.maddyhome.idea.vim.option.OptionsManager
import com.maddyhome.idea.vim.option.ToggleOption
import org.acejump.label.Tagger
import java.awt.Dimension
import javax.swing.JViewport
import javax.swing.KeyStroke

class AceExtensionTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        (OptionsManager.getOption("acejump") as ToggleOption).set()
    }

    fun `test bidirectional mapping`() {
        val command = parseKeysWithLeader("s")
        setupEditor()

        TestProcessor.handler = { _, _, _ ->
            search("found")
            assertEquals(1, Tagger.textMatches.size)
        }

        typeText(command)
        assertTestHandlerWasCalled()
    }

    fun `test bidirectional line motion`() {
        val command = parseKeys(command("bd-jk"))
        val before = text.indentLineThatStartsWith("I found")
        setupEditor(before)
        myFixture.editor.moveCaretBefore("all")

        TestProcessor.handler = { _, _, _ ->
            val jumpLocations = Tagger.textMatches.sorted()
            assertEquals(6, jumpLocations.size)
            assertEquals(before.indexOf("I found"), jumpLocations[2])
        }

        typeText(command)
        assertTestHandlerWasCalled()
    }

    fun `test forward line motion`() {
        val command = parseKeysWithLeader("j")
        val before = text.indentLineThatStartsWith("where")
        setupEditor(before)
        myFixture.editor.moveCaretBefore("all")

        TestProcessor.handler = { _, _, _ ->
            val jumpLocations = Tagger.textMatches.sorted()

            // It should probably be one less jump location because currently AceJump includes the current line
            assertEquals(3, jumpLocations.size)
            assertEquals(before.indexOf("where"), jumpLocations[1])
        }

        typeText(command)
        assertTestHandlerWasCalled()
    }

    fun `test backward line motion`() {
        val command = parseKeysWithLeader("k")
        val before = text.indentLineThatStartsWith("I found")
        setupEditor(before)
        myFixture.editor.moveCaretBefore("lavender")

        TestProcessor.handler = { _, _, _ ->
            val jumpLocations = Tagger.textMatches.sorted()

            // It should probably be one less jump location because currently AceJump includes the current line
            assertEquals(4, jumpLocations.size)
            assertEquals(before.indexOf("I found"), jumpLocations[2])
        }

        typeText(command)
        assertTestHandlerWasCalled()
    }

    fun `test backward line motion sol`() {
        val command = parseKeys(command("sol-k"))
        val before = text.indentLineThatStartsWith("I found")
        setupEditor(before)
        myFixture.editor.moveCaretBefore("lavender")

        TestProcessor.handler = { _, _, _ ->
            val jumpLocations = Tagger.textMatches.sorted()

            // It should probably be one less jump location because currently AceJump includes the current line
            assertEquals(4, jumpLocations.size)
            assertEquals(before.indexOf("I found") - 4, jumpLocations[2])
        }

        typeText(command)
        assertTestHandlerWasCalled()
    }

    fun `test backward line motion eol`() {
        val command = parseKeys(command("eol-k"))
        val before = text.indentLineThatStartsWith("I found")
        setupEditor(before)
        myFixture.editor.moveCaretBefore("lavender")

        TestProcessor.handler = { _, _, _ ->
            val jumpLocations = Tagger.textMatches.sorted()

            // It should probably be one less jump location because currently AceJump includes the current line
            assertEquals(3, jumpLocations.size)
            assertEquals(before.indexOf("land") + 4, jumpLocations[2])
        }

        typeText(command)
        assertTestHandlerWasCalled()
    }

    fun `test forward line motion sol`() {
        val command = parseKeys(command("sol-j"))
        val before = text.indentLineThatStartsWith("where")
        setupEditor(before)
        myFixture.editor.moveCaretBefore("lavender")

        TestProcessor.handler = { _, _, _ ->
            val jumpLocations = Tagger.textMatches.sorted()

            // It should probably be one less jump location because currently AceJump includes the current line
            assertEquals(2, jumpLocations.size)
            assertEquals(before.indexOf("where") - 4, jumpLocations[0])
        }

        typeText(command)
        assertTestHandlerWasCalled()
    }

    fun `test forward line motion eol`() {
        val command = parseKeys(command("eol-j"))
        val before = text.indentLineThatStartsWith("where")
        setupEditor(before)
        myFixture.editor.moveCaretBefore("lavender")

        TestProcessor.handler = { _, _, _ ->
            val jumpLocations = Tagger.textMatches.sorted()

            // Bug in AceJump. Should be 3
            assertEquals(2, jumpLocations.size)
            assertEquals(before.indexOf("sand") + 4, jumpLocations[1])
        }

        typeText(command)
        assertTestHandlerWasCalled()
    }

    fun `test forward word motion`() {
        val command = parseKeysWithLeader("w")
        setupEditor()
        myFixture.editor.moveCaretBefore("lavender", 2)

        TestProcessor.handler = { _, _, _ ->
            val jumpLocations = Tagger.textMatches.sorted()

            assertEquals(19, jumpLocations.size)
            assertTrue(text.indexOf("settled") in jumpLocations)
            assertTrue(text.indexOf("found") !in jumpLocations)
        }

        typeText(command)
        assertTestHandlerWasCalled()
    }

    fun `test backward word motion`() {
        val command = parseKeysWithLeader("b")
        setupEditor()
        myFixture.editor.moveCaretBefore("lavender", 2)

        TestProcessor.handler = { _, _, _ ->
            val jumpLocations = Tagger.textMatches.sorted()

            assertEquals(13, jumpLocations.size)
            assertTrue(text.indexOf("settled") !in jumpLocations)
            assertTrue(text.indexOf("found") in jumpLocations)
        }

        typeText(command)
        assertTestHandlerWasCalled()
    }

    fun `test both directions word motion`() {
        val command = parseKeys(command("bd-w"))
        setupEditor()
        myFixture.editor.moveCaretBefore("lavender", 2)

        TestProcessor.handler = { _, _, _ ->
            val jumpLocations = Tagger.textMatches.sorted()

            assertEquals(13 + 19, jumpLocations.size)
            assertTrue(text.indexOf("settled") in jumpLocations)
            assertTrue(text.indexOf("found") in jumpLocations)
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


    private fun parseKeysWithLeader(keys: String) = parseKeys("<leader><leader>$keys")

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

    private fun search(query: String) {
        myFixture.type(query).also { PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue() }
    }

    private fun Editor.moveCaretBefore(str: String, addition: Int = 0) {
        this.caretModel.moveToOffset(this.document.text.indexOf(str) + addition)
    }

    private fun assertTestHandlerWasCalled() {
        waitAndAssert { TestProcessor.handlerWasCalled }
    }

    private inline fun waitAndAssert(timeInMillis: Int = 1000, condition: () -> Boolean) {
        val end = System.currentTimeMillis() + timeInMillis
        while (end > System.currentTimeMillis()) {
            Thread.sleep(10)
            IdeEventQueue.getInstance().flushQueue()
            if (condition()) return
        }
        kotlin.test.fail()
    }

    override fun tearDown() {
        myFixture.performEditorAction(IdeActions.ACTION_EDITOR_ESCAPE)
        UIUtil.dispatchAllInvocationEvents()
        assertEmpty(myFixture.editor.markupModel.allHighlighters)
        super.tearDown()
    }
}

