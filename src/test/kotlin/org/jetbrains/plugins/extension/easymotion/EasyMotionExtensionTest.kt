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

import com.maddyhome.idea.vim.VimPlugin
import com.maddyhome.idea.vim.command.CommandState
import com.maddyhome.idea.vim.ex.vimscript.VimScriptGlobalEnvironment
import com.maddyhome.idea.vim.group.visual.vimSetSelection
import com.maddyhome.idea.vim.helper.Direction
import com.maddyhome.idea.vim.helper.StringHelper.parseKeys
import com.maddyhome.idea.vim.helper.mode
import com.maddyhome.idea.vim.option.OptionsManager
import com.maddyhome.idea.vim.option.ToggleOption
import org.jetbrains.plugins.extension.easymotion.EasyMotionExtension.Companion.startOfLine

class EasyMotionExtensionTest : EasyMotionTestCase() {

    override fun setUp() {
        super.setUp()
        (OptionsManager.getOption("easymotion") as ToggleOption).set()
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

    // Inclusive motion
    fun `test delete bidirectional forward`() {
        doTest(
            command = parseKeys("d") + parseKeysWithLeader("s"),
            putCaretAtWord = "lavender",
            searchQuery = "tufted",
            jumpToNthQuery = 0
        )
        myFixture.checkResult("""
                A Discovery

                I found it in a legendary land
                all rocks and <caret>ufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
        """.trimIndent())
    }

    // Exclusive motion
    fun `test delete bidirectional backward`() {
        doTest(
            command = parseKeys("d") + parseKeysWithLeader("s"),
            putCaretAtWord = "lavender",
            searchQuery = "found",
            jumpToNthQuery = 0
        )
        myFixture.checkResult("""
                A Discovery

                I <caret>lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
        """.trimIndent())
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

    fun `test forward line motion with offset save`() {
        doTest(
            command = parseKeysWithLeader("j"),
            editorText = """
                First long line
                
                sh
                Second long line
            """.trimIndent(),
            afterEditorSetup = { VimScriptGlobalEnvironment.getInstance().variables[startOfLine] = 0 },
            putCaretAtWord = "long"
        ) { editorText, jumpLocations ->
            // It should probably be one less jump location because currently AceJump includes the current line
            assertEquals(3, jumpLocations.size)
            assertEquals(editorText.lastIndexOf("long") - 1, jumpLocations[2])
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

    fun `test backward line motion with offset save`() {
        doTest(
            command = parseKeysWithLeader("k"),
            editorText = """
                First long line
                
                sh
                Second very long line
            """.trimIndent(),
            afterEditorSetup = { VimScriptGlobalEnvironment.getInstance().variables[startOfLine] = 0 },
            putCaretAtWord = "very"
        ) { editorText, jumpLocations ->
            // It should probably be one less jump location because currently AceJump includes the current line
            assertEquals(3, jumpLocations.size)
            assertEquals(editorText.indexOf("long") + 1, jumpLocations[0])
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
            assertEquals(editorText.indexOf("land") + 3, jumpLocations[2])
        }
    }

    fun `test backward line motion eol visual mode`() {
        doTest(
            command = parseKeys(command("eol-k")),
            editorText = text.indentLineThatStartsWith("I found"),
            afterEditorSetup = { VimPlugin.getVisualMotion().enterVisualMode(it) },
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
            assertEquals(3, jumpLocations.size)
            assertEquals(editorText.indexOf("sand") + 3, jumpLocations[1])
        }
    }

    fun `test forward line motion eol visual mode`() {
        doTest(
            command = parseKeys(command("eol-j")),
            editorText = text.indentLineThatStartsWith("where"),
            afterEditorSetup = { VimPlugin.getVisualMotion().enterVisualMode(it) },
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

    // Exclusive motion
    fun `test delete till word backward`() {
        doTest(
            command = parseKeys("d") + parseKeysWithLeader("b"),
            putCaretAtWord = "lavender",
            jumpToNthQuery = 11
        )
        myFixture.checkResult("""
                A Discovery

                I found it in a legendary land
                all rocks <caret>lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
        """.trimIndent())
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

    // Inclusive motion
    fun `test delete found`() {
        doTest(
            command = parseKeys("d") + parseKeysWithLeader("f"),
            putCaretAtWord = "lavender",
            searchQuery = "and",
            jumpToNthQuery = 0
        )
        myFixture.checkResult("""
                A Discovery

                I found it in a legendary land
                all rocks and <caret>nd tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
        """.trimIndent())
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

    // Exclusive motion
    fun `test delete found backward`() {
        doTest(
            command = parseKeys("d") + parseKeysWithLeader("F"),
            putCaretAtWord = "lavender",
            searchQuery = "and",
            jumpToNthQuery = 1
        )
        myFixture.checkResult("""
                A Discovery

                I found it in a legendary land
                all rocks <caret>lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
        """.trimIndent())
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

    // Inclusive motion
    fun `test delete till found`() {
        doTest(
            command = parseKeys("d") + parseKeysWithLeader("t"),
            putCaretAtWord = "lavender",
            searchQuery = "and",
            jumpToNthQuery = 0
        )
        myFixture.checkResult("""
                A Discovery

                I found it in a legendary land
                all rocks and <caret>and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
        """.trimIndent())
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

    // Exclusive motion
    fun `test delete till found backward`() {
        doTest(
            command = parseKeys("d") + parseKeysWithLeader("T"),
            putCaretAtWord = "lavender",
            searchQuery = "and",
            jumpToNthQuery = 1
        )
        myFixture.checkResult("""
                A Discovery

                I found it in a legendary land
                all rocks and<caret>lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
        """.trimIndent())
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

    fun `test within line s motion`() {
        doTest(
            command = parseKeys(command("sl")),
            putCaretAtWord = "in",
            searchQuery = "nd"
        ) { editorText, matches ->
            assertEquals(3, matches.size)
            assertTrue(editorText.lastIndexOf("land") + 2 in matches)
            assertTrue(editorText.indexOf("found") + 3 in matches)
        }
    }

    fun `test within line f motion`() {
        doTest(
            command = parseKeys(command("fl")),
            putCaretAtWord = "in",
            searchQuery = "nd"
        ) { editorText, matches ->
            assertEquals(2, matches.size)
            assertTrue(editorText.lastIndexOf("land") + 2 in matches)
            assertTrue(editorText.indexOf("found") + 3 !in matches)
        }
    }

    fun `test within line big f motion`() {
        doTest(
            command = parseKeys(command("Fl")),
            putCaretAtWord = "in",
            searchQuery = "nd"
        ) { editorText, matches ->
            assertEquals(1, matches.size)
            assertTrue(editorText.lastIndexOf("land") + 2 !in matches)
            assertTrue(editorText.indexOf("found") + 3 in matches)
        }
    }

    fun `test within line bd-fl motion`() {
        doTest(
            command = parseKeys(command("bd-fl")),
            putCaretAtWord = "in",
            searchQuery = "nd"
        ) { editorText, matches ->
            assertEquals(3, matches.size)
            assertTrue(editorText.lastIndexOf("land") + 2 in matches)
            assertTrue(editorText.indexOf("found") + 3 in matches)
        }
    }

    fun `test within line tl motion`() {
        doTest(
            command = parseKeys(command("tl")),
            putCaretAtWord = "in",
            searchQuery = "nd",
            jumpToNthQuery = 1
        )
        myFixture.checkResult("""
                A Discovery

                I found it in a legendary l<caret>and
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
        """.trimIndent())
    }

    fun `test within line big T motion`() {
        doTest(
            command = parseKeys(command("Tl")),
            putCaretAtWord = "in",
            searchQuery = "nd",
            jumpToNthQuery = 0
        )
        myFixture.checkResult("""
                A Discovery

                I found<caret> it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
        """.trimIndent())
    }

    fun `test within line bd-tl motion`() {
        doTest(
            command = parseKeys(command("bd-tl")),
            putCaretAtWord = "in",
            searchQuery = "nd",
            jumpToNthQuery = 2
        )
        myFixture.checkResult("""
                A Discovery

                I found it in a legendary l<caret>and
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
        """.trimIndent())
    }

    fun `test within line w motion`() {
        doTest(
            command = parseKeys(command("wl")),
            putCaretAtWord = "in",
            caretShift = 1
        ) { editorText, matches ->
            assertEquals(3, matches.size)
            assertTrue(editorText.indexOf("land") in matches)
            assertTrue(editorText.indexOf("found") !in matches)
        }
    }

    fun `test within line b motion`() {
        doTest(
            command = parseKeys(command("bl")),
            putCaretAtWord = "in",
            caretShift = 1
        ) { editorText, matches ->
            assertEquals(4, matches.size)
            assertTrue(editorText.indexOf("land") !in matches)
            assertTrue(editorText.indexOf("found") in matches)
        }
    }

    fun `test within line bd-wl motion`() {
        doTest(
            command = parseKeys(command("bd-wl")),
            putCaretAtWord = "in",
            caretShift = 1
        ) { editorText, matches ->
            assertEquals(7, matches.size)
            assertTrue(editorText.indexOf("land") in matches)
            assertTrue(editorText.indexOf("found") in matches)
        }
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

    // Inclusive motion
    fun `test delete word end`() {
        doTest(
            command = parseKeys("d") + parseKeysWithLeader("e"),
            putCaretAtWord = "lavender",
            jumpToNthQuery = 1
        )
        myFixture.checkResult("""
                A Discovery

                I found it in a legendary land
                all rocks and <caret> tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
        """.trimIndent())
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

    fun `test within line e motion`() {
        doTest(
            command = parseKeys(command("el")),
            putCaretAtWord = "in",
            caretShift = 1
        ) { editorText, matches ->
            assertEquals(4, matches.size)
            assertTrue(editorText.indexOf("land") + 3 in matches)
            assertTrue(editorText.indexOf("found") + 4 !in matches)
        }
    }

    fun `test within line ge motion`() {
        doTest(
            command = parseKeys(command("gel")),
            putCaretAtWord = "in",
            caretShift = 1
        ) { editorText, matches ->
            assertEquals(4, matches.size)
            assertTrue(editorText.indexOf("land") + 3 !in matches)
            assertTrue(editorText.indexOf("found") + 4 in matches)
        }
    }

    fun `test within line bd-ge motion`() {
        doTest(
            command = parseKeys(command("bd-el")),
            putCaretAtWord = "in",
            caretShift = 1
        ) { editorText, matches ->
            assertEquals(7, matches.size)
            assertTrue(editorText.indexOf("land") + 3 in matches)
            assertTrue(editorText.indexOf("found") + 4 in matches)
        }
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

    // Exclusive motion
    fun `test delete big word`() {
        doTest(
            command = parseKeys("d") + parseKeysWithLeader("W"),
            editorText = text.replace("tufted grass", "tufted.grass").replace("was settled", "was#settled"),
            putCaretAtWord = "lavender",
            caretShift = 2,
            jumpToNthQuery = 1
        )
        myFixture.checkResult("""
                A Discovery

                I found it in a legendary land
                all rocks and la<caret>tufted.grass,
                where it was#settled on some sodden sand
                hard by the torrent of a mountain pass.
        """.trimIndent())
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

    // Exclusive motion
    fun `test delete big word backward`() {
        doTest(
            command = parseKeys("d") + parseKeysWithLeader("B"),
            editorText = text.replace("tufted grass", "tufted.grass").replace("was settled", "was#settled"),
            putCaretAtWord = "lavender",
            caretShift = 2,
            jumpToNthQuery = 10
        )
        myFixture.checkResult("""
                A Discovery

                I found it in a legendary land
                all <caret>vender and tufted.grass,
                where it was#settled on some sodden sand
                hard by the torrent of a mountain pass.
        """.trimIndent())
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

    // NB: This is strange that `e` is inclusive and `E` is exclusive, but this is how easymotion works.
    // Exclusive motion
    fun `test end of big word`() {
        doTest(
            command = parseKeys("d") + parseKeysWithLeader("E"),
            editorText = text.replace("found it", "found.it")
                .replace("legendary land", "legendary#land")
                .replace("tufted grass", "tufted.grass")
                .replace("was settled", "was#settled"),
            putCaretAtWord = "lavender",
            caretShift = 2,
            jumpToNthQuery = 5
        )
        myFixture.checkResult("""
                A Discovery

                I found.it in a legendary#land
                all rocks and la<caret>d on some sodden sand
                hard by the torrent of a mountain pass.
        """.trimIndent())
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

    fun `test jumptoanywhere`() {
        doTest(
            editorText = "Hello\nWorld",
            command = parseKeys(command("jumptoanywhere"))
        ) { _: String, matchResults: List<Int> ->
            assertEquals(4, matchResults.size)
        }
    }

    fun `test lineforward`() {
        doTest(
            editorText = "This is a text for test",
            command = parseKeys(command("lineforward")),
            putCaretAtWord = "text",
            caretShift = 2
        ) { editorText: String, matchResults: List<Int> ->
            assertEquals(5, matchResults.size)
            assertTrue(editorText.indexOf("test") in matchResults)
            assertTrue(editorText.indexOf("text") !in matchResults)
        }
    }

    fun `test linebackward`() {
        doTest(
            editorText = "This is a text for test",
            command = parseKeys(command("linebackward")),
            putCaretAtWord = "text",
            caretShift = 2
        ) { editorText: String, matchResults: List<Int> ->
            assertEquals(6, matchResults.size)
            assertTrue(editorText.indexOf("test") !in matchResults)
            assertTrue(editorText.indexOf("text") in matchResults)
        }
    }

    fun `test lineanywhere`() {
        doTest(
            editorText = "This is a text for test",
            command = parseKeys(command("lineanywhere")),
            putCaretAtWord = "text",
            caretShift = 2
        ) { editorText: String, matchResults: List<Int> ->
            assertEquals(6 + 5, matchResults.size)
            assertTrue(editorText.indexOf("test") in matchResults)
            assertTrue(editorText.indexOf("text") in matchResults)
        }
    }

    fun `test repeat search forward norespect`() {
        doTest(
            editorText = "Hello middle Hello",
            command = parseKeysWithLeader("n"),
            putCaretAtWord = "middle",
            afterEditorSetup = {
                VimPlugin.getSearch().processSearchCommand(it, "Hello", 0, Direction.BACKWARDS)
            }
        ) { editorText: String, matchResults: List<Int> ->
            assertEquals(1, matchResults.size)
            assertTrue(editorText.lastIndexOf("Hello") in matchResults)
        }
    }

    fun `test repeat search backward norespect`() {
        doTest(
            editorText = "Hello middle Hello",
            command = parseKeysWithLeader("N"),
            putCaretAtWord = "middle",
            afterEditorSetup = {
                VimPlugin.getSearch().processSearchCommand(it, "Hello", 0, Direction.BACKWARDS)
            }
        ) { editorText: String, matchResults: List<Int> ->
            assertEquals(1, matchResults.size)
            assertTrue(editorText.indexOf("Hello") in matchResults)
        }
    }

    fun `test repeat search forward respect`() {
        doTest(
            editorText = "Hello middle Hello",
            command = parseKeys(command("vim-n")),
            putCaretAtWord = "middle",
            afterEditorSetup = {
                VimPlugin.getSearch().processSearchCommand(it, "Hello", 0, Direction.BACKWARDS)
            }
        ) { editorText: String, matchResults: List<Int> ->
            assertEquals(1, matchResults.size)
            assertTrue(editorText.indexOf("Hello") in matchResults)
        }
    }

    fun `test repeat search backward respect`() {
        doTest(
            editorText = "Hello middle Hello",
            command = parseKeys(command("vim-N")),
            putCaretAtWord = "middle",
            afterEditorSetup = {
                VimPlugin.getSearch().processSearchCommand(it, "Hello", 0, Direction.BACKWARDS)
            }
        ) { editorText: String, matchResults: List<Int> ->
            assertEquals(1, matchResults.size)
            assertTrue(editorText.lastIndexOf("Hello") in matchResults)
        }
    }

    fun `test repeat search bd`() {
        doTest(
            editorText = "Hello middle Hello",
            command = parseKeys(command("bd-n")),
            putCaretAtWord = "middle",
            afterEditorSetup = {
                VimPlugin.getSearch().processSearchCommand(it, "Hello", 0, Direction.BACKWARDS)
            }
        ) { editorText: String, matchResults: List<Int> ->
            assertEquals(2, matchResults.size)
            assertTrue(editorText.lastIndexOf("Hello") in matchResults)
            assertTrue(editorText.indexOf("Hello") in matchResults)
        }
    }

    fun `test delete till word`() {
        doTest(
            command = parseKeys("d") + parseKeysWithLeader("w"),
            putCaretAtWord = "lavender",
            jumpToNthQuery = 2
        )
        myFixture.checkResult("""
                A Discovery

                I found it in a legendary land
                all rocks and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
        """.trimIndent())
    }

    fun `test move visual`() {
        doTest(
            command = parseKeysWithLeader("w"),
            putCaretAtWord = "lavender",
            jumpToNthQuery = 2,
            afterEditorSetup = {
                VimPlugin.getVisualMotion().enterVisualMode(it)
            }
        )
        myFixture.checkResult("""
                A Discovery

                I found it in a legendary land
                all rocks and <selection>lavender and t</selection>ufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
        """.trimIndent())
    }
}
