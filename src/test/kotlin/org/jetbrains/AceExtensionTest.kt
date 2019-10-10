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
            jumpToNthQuery = 0,
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
            assertTrue(editorText.lastIndexOf("it") in matches)
            assertTrue(editorText.indexOf("it") !in matches)
        }
    }

    fun `test backward mapping`() {
        doTest(
            command = parseKeysWithLeader("F"),
            putCaretAtWord = "lavender",
            searchQuery = "it"
        ) { editorText, matches ->
            assertEquals(1, matches.size)
            assertTrue(editorText.lastIndexOf("it") !in matches)
            assertTrue(editorText.indexOf("it") in matches)
        }
    }

    fun `test till forward mapping`() {
        doTest(
            command = parseKeysWithLeader("t"),
            putCaretAtWord = "lavender",
            searchQuery = "it",
            jumpToNthQuery = 0
        ) { editorText, _ ->
            assertEquals(editorText.lastIndexOf("it") - 1, myFixture.editor.caretModel.offset)
        }
    }

    fun `test till backward mapping`() {
        doTest(
            command = parseKeysWithLeader("T"),
            putCaretAtWord = "lavender",
            searchQuery = "it",
            jumpToNthQuery = 0
        ) { editorText, _ ->
            assertEquals(editorText.indexOf("it") + 2, myFixture.editor.caretModel.offset)
        }
    }

    fun `test line motions are linewise for op pending`() {
        doTest(
            command = parseKeys("d") + parseKeysWithLeader("j"),
            editorText = text.indentLineThatStartsWith("where"),
            putCaretAtWord = "all",
            jumpToNthQuery = 1
        )
        myFixture.checkResult(
            """
                A Discovery

                I found it in a legendary land
                <caret>hard by the torrent of a mountain pass.
        """.trimIndent()
        )
    }

    fun `test linewise s motion`() {
        doTest(
            command = parseKeys("d") + parseKeys(command("sl")),
            putCaretAtWord = "lavender",
            searchQuery = "it",
            jumpToNthQuery = 1
        )
        myFixture.checkResult(
            """
                A Discovery

                I found it in a legendary land
                hard by the torrent of a mountain pass.
        """.trimIndent()
        )
    }

    fun `test linewise f motion`() {
        doTest(
            command = parseKeys("d") + parseKeys(command("fl")),
            putCaretAtWord = "lavender",
            searchQuery = "it",
            jumpToNthQuery = 0
        )
        myFixture.checkResult(
            """
                A Discovery

                I found it in a legendary land
                hard by the torrent of a mountain pass.
        """.trimIndent()
        )
    }

    fun `test linewise F motion`() {
        doTest(
            command = parseKeys("d") + parseKeys(command("Fl")),
            putCaretAtWord = "lavender",
            searchQuery = "it",
            jumpToNthQuery = 0
        )
        myFixture.checkResult(
            """
                A Discovery

                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
        """.trimIndent()
        )
    }

    fun `test linewise bd-fl motion`() {
        doTest(
            command = parseKeys("d") + parseKeys(command("bd-fl")),
            putCaretAtWord = "lavender",
            searchQuery = "it",
            jumpToNthQuery = 1
        )
        myFixture.checkResult(
            """
                A Discovery

                I found it in a legendary land
                hard by the torrent of a mountain pass.
        """.trimIndent()
        )
    }

    fun `test linewise tl motion`() {
        doTest(
            command = parseKeys("d") + parseKeys(command("tl")),
            putCaretAtWord = "lavender",
            searchQuery = "it",
            jumpToNthQuery = 0
        )
        myFixture.checkResult(
            """
                A Discovery

                I found it in a legendary land
                hard by the torrent of a mountain pass.
        """.trimIndent()
        )
    }

    fun `test linewise T motion`() {
        doTest(
            command = parseKeys("d") + parseKeys(command("Tl")),
            putCaretAtWord = "lavender",
            searchQuery = "it",
            jumpToNthQuery = 0
        )
        myFixture.checkResult(
            """
                A Discovery

                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
        """.trimIndent()
        )
    }

    fun `test linewise bd-tl motion`() {
        doTest(
            command = parseKeys("d") + parseKeys(command("bd-tl")),
            putCaretAtWord = "lavender",
            searchQuery = "it",
            jumpToNthQuery = 1
        )
        myFixture.checkResult(
            """
                A Discovery

                I found it in a legendary land
                hard by the torrent of a mountain pass.
        """.trimIndent()
        )
    }

    fun `test linewise w motion`() {
        doTest(
            command = parseKeys("d") + parseKeys(command("wl")),
            putCaretAtWord = "lavender",
            caretShift = 2,
            jumpToNthQuery = 2
        )
        myFixture.checkResult(
            """
                A Discovery

                I found it in a legendary land
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
        """.trimIndent()
        )
    }

    fun `test linewise b motion`() {
        doTest(
            command = parseKeys("d") + parseKeys(command("bl")),
            putCaretAtWord = "lavender",
            caretShift = 2,
            jumpToNthQuery = 2
        )
        myFixture.checkResult(
            """
                A Discovery

                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
        """.trimIndent()
        )
    }

    fun `test linewise bd-wl motion`() {
        doTest(
            command = parseKeys("d") + parseKeys(command("bd-wl")),
            putCaretAtWord = "lavender",
            caretShift = 2,
            jumpToNthQuery = 2
        )
        myFixture.checkResult(
            """
                A Discovery

                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
        """.trimIndent()
        )
    }

    fun `test jump to word end`() {
        doTest(
            command = parseKeysWithLeader("e"),
            putCaretAtWord = "lavender"
        ) { editorText: String, matchResults: List<Int> ->
            assertEquals(20, matchResults.size)
            assertTrue(editorText.indexOf("tufted") + 5 in matchResults)
            assertTrue(editorText.indexOf("land") + 3 !in matchResults)
        }
    }

    fun `test jump to word end backward`() {
        doTest(
            command = parseKeysWithLeader("ge"),
            putCaretAtWord = "lavender"
        ) { editorText: String, matchResults: List<Int> ->
            assertEquals(12, matchResults.size)
            assertTrue(editorText.indexOf("tufted") + 5 !in matchResults)
            assertTrue(editorText.indexOf("land") + 3 in matchResults)
        }
    }

    fun `test jump to word end bd`() {
        doTest(
            command = parseKeys(command("bd-e")),
            putCaretAtWord = "lavender"
        ) { editorText: String, matchResults: List<Int> ->
            assertEquals(12 + 20, matchResults.size)
            assertTrue(editorText.indexOf("tufted") + 5 in matchResults)
            assertTrue(editorText.indexOf("land") + 3 in matchResults)
        }
    }

    fun `test linewise e motion`() {
        doTest(
            command = parseKeys("d") + parseKeys(command("el")),
            putCaretAtWord = "lavender",
            caretShift = 2,
            jumpToNthQuery = 2
        )
        myFixture.checkResult(
            """
                A Discovery

                I found it in a legendary land
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
        """.trimIndent()
        )
    }

    fun `test linewise ge motion`() {
        doTest(
            command = parseKeys("d") + parseKeys(command("gel")),
            putCaretAtWord = "lavender",
            caretShift = 2,
            jumpToNthQuery = 2
        )
        myFixture.checkResult(
            """
                A Discovery

                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
        """.trimIndent()
        )
    }

    fun `test linewise bd-ge motion`() {
        doTest(
            command = parseKeys("d") + parseKeys(command("bd-el")),
            putCaretAtWord = "lavender",
            caretShift = 2,
            jumpToNthQuery = 2
        )
        myFixture.checkResult(
            """
                A Discovery

                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
        """.trimIndent()
        )
    }

    fun `test forward big word motion`() {
        doTest(
            command = parseKeysWithLeader("W"),
            editorText = text.replace("tufted grass", "tufted.grass").replace("was settled", "was#settled"),
            putCaretAtWord = "lavender",
            caretShift = 2
        ) { _, jumpLocations ->
            assertEquals(17, jumpLocations.size)
        }
    }

    fun `test backward big word motion`() {
        doTest(
            command = parseKeysWithLeader("B"),
            editorText = text.replace("found it", "found.it").replace("legendary land", "legendary#land"),
            putCaretAtWord = "lavender",
            caretShift = 2
        ) { _, jumpLocations ->
            assertEquals(11, jumpLocations.size)
        }
    }

    fun `test bd big word motion`() {
        doTest(
            command = parseKeysWithLeader(command("bd-W")),
            editorText = text.replace("found it", "found.it")
                .replace("legendary land", "legendary#land")
                .replace("tufted grass", "tufted.grass")
                .replace("was settled", "was#settled"),
            putCaretAtWord = "lavender",
            caretShift = 2
        ) { _, jumpLocations ->
            assertEquals(11 + 17, jumpLocations.size)
        }
    }

    fun `test forward big word end motion`() {
        doTest(
            command = parseKeysWithLeader(command("E")),
            editorText = text.replace("found it", "found.it")
                .replace("legendary land", "legendary#land")
                .replace("tufted grass", "tufted.grass")
                .replace("was settled", "was#settled"),
            putCaretAtWord = "lavender",
            caretShift = 2
        ) { _: String, matchResults: List<Int> ->
            assertEquals(18, matchResults.size)
        }
    }

    fun `test backward big word end motion`() {
        doTest(
            command = parseKeysWithLeader(command("gE")),
            editorText = text.replace("found it", "found.it")
                .replace("legendary land", "legendary#land")
                .replace("tufted grass", "tufted.grass")
                .replace("was settled", "was#settled"),
            putCaretAtWord = "lavender",
            caretShift = 2
        ) { _: String, matchResults: List<Int> ->
            assertEquals(10, matchResults.size)
        }
    }

    fun `test bd big word end motion`() {
        doTest(
            command = parseKeys(command("bd-E")),
            editorText = text.replace("found it", "found.it")
                .replace("legendary land", "legendary#land")
                .replace("tufted grass", "tufted.grass")
                .replace("was settled", "was#settled"),
            putCaretAtWord = "lavender",
            caretShift = 2
        ) { _: String, matchResults: List<Int> ->
            assertEquals(10 + 18, matchResults.size)
        }
    }

    fun `test iskeyword w`() {
        doTest(
            command = parseKeys(command("iskeyword-w")),
            editorText = iskeywordText,
            putCaretAtWord = "middle",
            caretShift = 2,
            afterEditorSetup = {
                val value = "@,-"
                OptionsManager.parseOptionLine(it, "iskeyword=$value", false)
            }
        ) { editorText: String, matchResults: List<Int> ->
            assertEquals(9, matchResults.size)
            assertTrue(editorText.indexOf("case-twoB") in matchResults)
            assertTrue(editorText.indexOf("case-twoA") !in matchResults)
        }
    }

    fun `test iskeyword b`() {
        doTest(
            command = parseKeys(command("iskeyword-b")),
            editorText = iskeywordText,
            putCaretAtWord = "middle",
            caretShift = 2,
            afterEditorSetup = {
                val value = "@,-"
                OptionsManager.parseOptionLine(it, "iskeyword=$value", false)
            }
        ) { editorText: String, matchResults: List<Int> ->
            assertEquals(10, matchResults.size)
            assertTrue(editorText.indexOf("case-twoB") !in matchResults)
            assertTrue(editorText.indexOf("case-twoA") in matchResults)
        }
    }

    fun `test iskeyword bd-w`() {
        doTest(
            command = parseKeys(command("iskeyword-bd-w")),
            editorText = iskeywordText,
            putCaretAtWord = "middle",
            caretShift = 2,
            afterEditorSetup = {
                val value = "@,-"
                OptionsManager.parseOptionLine(it, "iskeyword=$value", false)
            }
        ) { editorText: String, matchResults: List<Int> ->
            assertEquals(10 + 9, matchResults.size)
            assertTrue(editorText.indexOf("case-twoB") in matchResults)
            assertTrue(editorText.indexOf("case-twoA") in matchResults)
        }
    }

    fun `test iskeyword e`() {
        doTest(
            command = parseKeys(command("iskeyword-e")),
            editorText = iskeywordText,
            putCaretAtWord = "middle",
            caretShift = 2,
            afterEditorSetup = {
                val value = "@,-"
                OptionsManager.parseOptionLine(it, "iskeyword=$value", false)
            }
        ) { editorText: String, matchResults: List<Int> ->
            assertEquals(10, matchResults.size)
            assertTrue(editorText.indexOf("case-twoB") + 8 in matchResults)
            assertTrue(editorText.indexOf("case-twoA") + 8 !in matchResults)
        }
    }

    fun `test iskeyword ge`() {
        doTest(
            command = parseKeys(command("iskeyword-ge")),
            editorText = iskeywordText,
            putCaretAtWord = "middle",
            caretShift = 2,
            afterEditorSetup = {
                val value = "@,-"
                OptionsManager.parseOptionLine(it, "iskeyword=$value", false)
            }
        ) { editorText: String, matchResults: List<Int> ->
            assertEquals(9, matchResults.size)
            assertTrue(editorText.indexOf("case-twoB") + 8 !in matchResults)
            assertTrue(editorText.indexOf("case-twoA") + 8 in matchResults)
        }
    }

    fun `test iskeyword bd-e`() {
        doTest(
            command = parseKeys(command("iskeyword-bd-e")),
            editorText = iskeywordText,
            putCaretAtWord = "middle",
            caretShift = 2,
            afterEditorSetup = {
                val value = "@,-"
                OptionsManager.parseOptionLine(it, "iskeyword=$value", false)
            }
        ) { editorText: String, matchResults: List<Int> ->
            assertEquals(9 + 10, matchResults.size)
            assertTrue(editorText.indexOf("case-twoB") + 8 in matchResults)
            assertTrue(editorText.indexOf("case-twoA") + 8 in matchResults)
        }
    }

    private fun doTest(
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

        TestProcessor.inputQuery = {
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

    private val iskeywordText = """
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

