package org.jetbrains

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
import com.maddyhome.idea.vim.ui.ExEntryPanel
import org.acejump.view.Canvas
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
            assertEquals(1, Canvas.jumpLocations.size)
        }

        typeText(command)
    }

    private fun parseKeysWithLeader(keys: String) = parseKeys("<leader><leader>$keys")

    private fun typeText(keys: List<KeyStroke>): Editor {
        val editor = myFixture.editor
        val keyHandler = KeyHandler.getInstance()
        val dataContext = EditorDataContext(editor)
        TestInputModel.getInstance(editor).setKeyStrokes(keys)

        val inputModel = TestInputModel.getInstance(editor)
        var key = inputModel.nextKeyStroke()
        while (key != null) {
            val exEntryPanel = ExEntryPanel.getInstance()
            if (exEntryPanel.isActive) {
                exEntryPanel.handleKey(key)
            } else {
                keyHandler.handleKey(editor, key, dataContext)
            }
            key = inputModel.nextKeyStroke()
        }

        return editor
    }

    private fun search(query: String) {
        myFixture.type(query).also { UIUtil.dispatchAllInvocationEvents() }
    }
}

