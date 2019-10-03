package org.jetbrains

import com.intellij.ide.IdeEventQueue
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.ui.UIUtil
import com.maddyhome.idea.vim.KeyHandler
import com.maddyhome.idea.vim.helper.EditorDataContext
import com.maddyhome.idea.vim.helper.StringHelper.parseKeys
import com.maddyhome.idea.vim.helper.TestInputModel
import com.maddyhome.idea.vim.option.OptionsManager
import com.maddyhome.idea.vim.option.ToggleOption
import org.acejump.label.Tagger
import javax.swing.KeyStroke

class AceExtensionTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        (OptionsManager.getOption("acejump") as ToggleOption).set()
    }

    fun `test bidirectional mapping`() {
        val command = parseKeysWithLeader("s")
        val before = """
                A Discovery

                I found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
        """.trimIndent()
        myFixture.configureByText(PlainTextFileType.INSTANCE, before)

        TestProcessor.handler = { _, _, _ ->
            search("found")
            assertEquals(1, Tagger.textMatches.size)
        }

        typeText(command)
        assertTestHandlerWasCalled()
    }

    fun `test bidirectional line motion`() {
        val command = parseKeys(command("bd-jk"))
        val before = """
                A Discovery

                    I found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
        """.trimIndent()
        myFixture.configureByText(PlainTextFileType.INSTANCE, before)
        myFixture.editor.moveCaretBefore("all")

        TestProcessor.handler = { _, _, _ ->
            val jumpLocations = Tagger.textMatches.sorted()
            assertEquals(6, jumpLocations.size)
            assertEquals(before.indexOf("I found"), jumpLocations[2])
        }

        typeText(command)
        assertTestHandlerWasCalled()
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
    }

    private fun search(query: String) {
        myFixture.type(query).also { UIUtil.dispatchAllInvocationEvents() }
    }

    private fun Editor.moveCaretBefore(str: String) {
        this.caretModel.moveToOffset(this.document.text.indexOf(str))
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
}

