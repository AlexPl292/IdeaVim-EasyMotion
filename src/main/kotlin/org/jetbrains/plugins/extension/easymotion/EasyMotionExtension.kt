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

@file:Suppress("PrivatePropertyName")

package org.jetbrains.plugins.extension.easymotion

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.VisualPosition
import com.maddyhome.idea.vim.command.MappingMode
import com.maddyhome.idea.vim.ex.vimscript.VimScriptGlobalEnvironment
import com.maddyhome.idea.vim.extension.VimExtension
import com.maddyhome.idea.vim.extension.VimExtensionFacade.putKeyMapping
import com.maddyhome.idea.vim.helper.EditorHelper
import com.maddyhome.idea.vim.helper.StringHelper.parseKeys
import com.maddyhome.idea.vim.helper.isEndAllowed
import com.maddyhome.idea.vim.helper.mode
import com.maddyhome.idea.vim.key.MappingOwner
import org.acejump.boundaries.Boundaries
import org.acejump.boundaries.StandardBoundaries
import org.acejump.input.JumpMode
import org.acejump.search.Pattern
import org.acejump.session.Session
import org.intellij.lang.annotations.Language
import org.jetbrains.plugins.extension.easymotion.MotionType.*

class EasyMotionExtension : VimExtension {
    override fun getName(): String = pluginName

    override fun getOwner() = mappingOwner

    companion object {
        const val pluginPrefix = "<Plug>(easymotion-prefix)"
        const val defaultPrefix = "<leader><leader>"

        @Language("RegExp")
        private const val wordEnd = "[a-zA-Z0-9_](?=[^a-zA-Z0-9_]|\\Z)"
        @Language("RegExp")
        private const val WORD = "(?<=\\s|\\A)[^\\s]"
        @Language("RegExp")
        private const val WORD_END = "[^\\s](?=\\s|\\Z)"
        @Language("RegExp")
        private const val LINE_END_NO_NEWLINE = "(.(?=\\n|\\Z))|(^$)"

        private const val jumpAnywhere = "g:EasyMotion_re_anywhere"
        private const val lineJumpAnywhere = "g:EasyMotion_re_line_anywhere"
        const val doMapping = "g:EasyMotion_do_mapping"
        const val startOfLine = "g:EasyMotion_startofline"
        const val overrideAcejump = "g:EasyMotion_override_acejump"

        private const val defaultRe = """\v(<.|^${'$'})|(.>|^${'$'})|(\l)\zs(\u)|(_\zs.)|(#\zs.)"""

        private const val pluginName = "easymotion"
        val mappingOwner = MappingOwner.Plugin.get(pluginName)
    }

    override fun init() {
        VimScriptGlobalEnvironment.getInstance().variables.let { vars ->
            vars[jumpAnywhere] = defaultRe
            vars[lineJumpAnywhere] = defaultRe
            if (doMapping !in vars) vars[doMapping] = 1
            if (startOfLine !in vars) vars[startOfLine] = 1
            if (overrideAcejump !in vars) vars[overrideAcejump] = 1
        }

        // -----------  Default mapping table ---------------------//

        //AFTER_CARET_BOUNDARY
        mapToFunctionAndProvideKeys("f", MultiInput(INCLUSIVE))         // Works as `fn`

        //BEFORE_CARET_BOUNDARY
        mapToFunctionAndProvideKeys("F", MultiInput(EXCLUSIVE))        // Works as `Fn`
        // AFTER_CARET_BOUNDARY
        mapToFunctionAndProvideKeys("t", MultiInputPreStop(INCLUSIVE))  // Works as `tn`
        // BEFORE_CARET_BOUNDARY
        mapToFunctionAndProvideKeys("T", MultiInputPreStop(EXCLUSIVE)) // Works as `Tn`
        mapToFunctionAndProvideKeys("w", PredefinedPattern(Pattern.ALL_WORDS, StandardBoundaries.AFTER_CARET, EXCLUSIVE))
        mapToFunctionAndProvideKeys("W", CustomPattern(WORD, StandardBoundaries.AFTER_CARET, EXCLUSIVE))
        mapToFunctionAndProvideKeys("b", PredefinedPattern(Pattern.ALL_WORDS, StandardBoundaries.BEFORE_CARET, EXCLUSIVE))
        mapToFunctionAndProvideKeys("B", CustomPattern(WORD, StandardBoundaries.BEFORE_CARET, EXCLUSIVE))
        mapToFunctionAndProvideKeys("e", CustomPattern(wordEnd, StandardBoundaries.AFTER_CARET, INCLUSIVE))
        mapToFunctionAndProvideKeys("E", CustomPattern(WORD_END, StandardBoundaries.AFTER_CARET, EXCLUSIVE))
        mapToFunctionAndProvideKeys("ge", CustomPattern(wordEnd, StandardBoundaries.BEFORE_CARET, INCLUSIVE))
        mapToFunctionAndProvideKeys("gE", CustomPattern(WORD_END, StandardBoundaries.BEFORE_CARET, EXCLUSIVE))
        mapToFunctionAndProvideKeys("j", JkMotion(StandardBoundaries.AFTER_CARET))
        mapToFunctionAndProvideKeys("k", JkMotion(StandardBoundaries.BEFORE_CARET))

        //SCREEN_BOUNDARY
        mapToFunctionAndProvideKeys("s", MultiInput(BIDIRECTIONAL_INCLUSIVE))  // Works as `sn`
//        mapToFunctionAndProvideKeys("n", RepeatSearch(forward = true, respectVimDirection = false))
//        mapToFunctionAndProvideKeys("N", RepeatSearch(forward = false, respectVimDirection = false))

        // ------------ Extended mapping table -------------------//

        //SCREEN_BOUNDARY
        mapToFunction("bd-f", MultiInput(BIDIRECTIONAL_INCLUSIVE))
        mapToFunction("bd-t", BiDirectionalPreStop(false))
        mapToFunction("bd-w", PredefinedPattern(Pattern.ALL_WORDS, StandardBoundaries.VISIBLE_ON_SCREEN, EXCLUSIVE))
        mapToFunction("bd-W", CustomPattern(WORD, StandardBoundaries.VISIBLE_ON_SCREEN, EXCLUSIVE))
        mapToFunction("bd-e", CustomPattern(wordEnd, StandardBoundaries.VISIBLE_ON_SCREEN, BIDIRECTIONAL_INCLUSIVE))
        mapToFunction("bd-E", CustomPattern(WORD_END, StandardBoundaries.VISIBLE_ON_SCREEN, EXCLUSIVE))

        // FULL_FILE_BOUNDARY
        mapToFunction("bd-jk", PredefinedPattern(Pattern.LINE_INDENTS, StandardBoundaries.VISIBLE_ON_SCREEN, LINE))
//        mapToFunction("bd-n", RepeatSearch(forward = false, respectVimDirection = false, bidirect = true))
//        mapToFunction("jumptoanywhere", Jumptoanywhere())
        mapToFunction("sol-j", PredefinedPattern(Pattern.LINE_STARTS, StandardBoundaries.AFTER_CARET, LINE))
        mapToFunction("sol-k", PredefinedPattern(Pattern.LINE_STARTS, StandardBoundaries.BEFORE_CARET, LINE))
        mapToFunction("eol-j", EndOfLinePattern(StandardBoundaries.AFTER_CARET))
        mapToFunction("eol-k", EndOfLinePattern(StandardBoundaries.BEFORE_CARET))
        mapToFunction("iskeyword-w", KeyWordStart(StandardBoundaries.AFTER_CARET))
        mapToFunction("iskeyword-b", KeyWordStart(StandardBoundaries.BEFORE_CARET))
        mapToFunction("iskeyword-bd-w", KeyWordStart(StandardBoundaries.VISIBLE_ON_SCREEN))
        mapToFunction("iskeyword-e", KeyWordEnd(StandardBoundaries.AFTER_CARET, INCLUSIVE))
        mapToFunction("iskeyword-ge", KeyWordEnd(StandardBoundaries.BEFORE_CARET, INCLUSIVE))
        mapToFunction("iskeyword-bd-e", KeyWordEnd(StandardBoundaries.VISIBLE_ON_SCREEN, BIDIRECTIONAL_INCLUSIVE))
//        mapToFunction("vim-n", RepeatSearch(forward = true, respectVimDirection = true))
//        mapToFunction("vim-N", RepeatSearch(forward = false, respectVimDirection = true))

        // ------------ Within Line Motion -----------------------//

        // CURRENT_LINE_BOUNDARY
        mapToFunction("sl", MultiInput(BIDIRECTIONAL_INCLUSIVE))        // Works as `sln`
        // CURRENT_LINE_AFTER_CARET_BOUNDARY
        mapToFunction("fl", MultiInput(INCLUSIVE))          // Works as `fln`
        // CURRENT_LINE_BEFORE_CARET_BOUNDARY
        mapToFunction("Fl", MultiInput(EXCLUSIVE))         // Works as `Fln`
        // CURRENT_LINE_BOUNDARY
        mapToFunction("bd-fl", MultiInput(BIDIRECTIONAL_INCLUSIVE))     // Works as `sln`
        // CURRENT_LINE_AFTER_CARET_BOUNDARY
        mapToFunction("tl", MultiInputPreStop(INCLUSIVE))   // Works as `tln`
        // CURRENT_LINE_BEFORE_CARET_BOUNDARY
        mapToFunction("Tl", MultiInputPreStop(EXCLUSIVE))  // Works as `Tln`
        mapToFunction("bd-tl", BiDirectionalPreStop(true))                              // Works as `bd-tln`
        // CURRENT_LINE_AFTER_CARET_BOUNDARY
        mapToFunction("wl", PredefinedPattern(Pattern.ALL_WORDS, StandardBoundaries.WHOLE_FILE, EXCLUSIVE))
        // CURRENT_LINE_BEFORE_CARET_BOUNDARY
        mapToFunction("bl", PredefinedPattern(Pattern.ALL_WORDS, StandardBoundaries.WHOLE_FILE, EXCLUSIVE))
        // CURRENT_LINE_BOUNDARY
        mapToFunction("bd-wl", PredefinedPattern(Pattern.ALL_WORDS, StandardBoundaries.WHOLE_FILE, EXCLUSIVE))
        // CURRENT_LINE_AFTER_CARET_BOUNDARY
        mapToFunction("el", CustomPattern(wordEnd, StandardBoundaries.WHOLE_FILE, INCLUSIVE))
        // CURRENT_LINE_BEFORE_CARET_BOUNDARY
        mapToFunction("gel", CustomPattern(wordEnd, StandardBoundaries.WHOLE_FILE, INCLUSIVE))
        // CURRENT_LINE_BOUNDARY
        mapToFunction("bd-el", CustomPattern(wordEnd, StandardBoundaries.WHOLE_FILE, BIDIRECTIONAL_INCLUSIVE))
//        mapToFunction("lineforward", JumptoanywhereInLine(0))
//        mapToFunction("linebackward", JumptoanywhereInLine(1))
//        mapToFunction("lineanywhere", JumptoanywhereInLine(2))

        // ------------ Multi input mapping table ----------------//
        // SCREEN_BOUNDARY
        mapToFunction("s2", MultiInput(BIDIRECTIONAL_INCLUSIVE))                // Works as `sn`
        // AFTER_CARET_BOUNDARY
        mapToFunction("f2", MultiInput(INCLUSIVE))                         // Works as `fn`
        // BEFORE_CARET_BOUNDARY
        mapToFunction("F2", MultiInput(EXCLUSIVE))                        // Works as `Fn`
        // SCREEN_BOUNDARY
        mapToFunction("bd-f2", MultiInput(BIDIRECTIONAL_INCLUSIVE))             // Works as `sn`
        // AFTER_CARET_BOUNDARY
        mapToFunction("t2", MultiInputPreStop(INCLUSIVE))                  // Works as `tn`
        // BEFORE_CARET_BOUNDARY
        mapToFunction("T2", MultiInputPreStop(EXCLUSIVE))                 // Works as `Tn`
        mapToFunction("bd-t2", BiDirectionalPreStop(false))

        // CURRENT_LINE_BOUNDARY
        mapToFunction("sl2", MultiInput(BIDIRECTIONAL_INCLUSIVE))        // Works as `sln`
        // CURRENT_LINE_AFTER_CARET_BOUNDARY
        mapToFunction("fl2", MultiInput(INCLUSIVE))          // Works as `fln`
        // CURRENT_LINE_BEFORE_CARET_BOUNDARY
        mapToFunction("Fl2", MultiInput(EXCLUSIVE))         // Works as `Fln`
        // CURRENT_LINE_AFTER_CARET_BOUNDARY
        mapToFunction("tl2", MultiInputPreStop(INCLUSIVE))   // Works as `tln`
        // CURRENT_LINE_BEFORE_CARET_BOUNDARY
        mapToFunction("Tl2", MultiInputPreStop(EXCLUSIVE))  // Works as `Tln`

        // SCREEN_BOUNDARY
        mapToFunction("sn", MultiInput(BIDIRECTIONAL_INCLUSIVE))
        // AFTER_CARET_BOUNDARY
        mapToFunction("fn", MultiInput(INCLUSIVE))
        // BEFORE_CARET_BOUNDARY
        mapToFunction("Fn", MultiInput(EXCLUSIVE))
        // SCREEN_BOUNDARY
        mapToFunction("bd-fn", MultiInput(BIDIRECTIONAL_INCLUSIVE))
        // AFTER_CARET_BOUNDARY
        mapToFunction("tn", MultiInputPreStop(INCLUSIVE))
        // BEFORE_CARET_BOUNDARY
        mapToFunction("Tn", MultiInputPreStop(EXCLUSIVE))
        mapToFunction("bd-tn", BiDirectionalPreStop(false))

        // CURRENT_LINE_BOUNDARY
        mapToFunction("sln", MultiInput(BIDIRECTIONAL_INCLUSIVE))
        // CURRENT_LINE_AFTER_CARET_BOUNDARY
        mapToFunction("fln", MultiInput(INCLUSIVE))
        // CURRENT_LINE_BEFORE_CARET_BOUNDARY
        mapToFunction("Fln", MultiInput(EXCLUSIVE))
        // CURRENT_LINE_BOUNDARY
        mapToFunction("bd-fln", MultiInput(BIDIRECTIONAL_INCLUSIVE))
        // CURRENT_LINE_AFTER_CARET_BOUNDARY
        mapToFunction("tln", MultiInputPreStop(INCLUSIVE))
        // CURRENT_LINE_BEFORE_CARET_BOUNDARY
        mapToFunction("Tln", MultiInputPreStop(EXCLUSIVE))
        mapToFunction("bd-tln", BiDirectionalPreStop(true))

/*
        VimExtensionFacade.putExtensionHandlerMapping(
            MappingMode.NVO,
            parseKeys("<Plug>(acejump-linemarks)"),
            owner,
            getHandler(LineMarks),
            false
        )
*/

        putKeyMapping(MappingMode.NVO, parseKeys(defaultPrefix), owner, parseKeys(pluginPrefix), true)

        if (VimScriptGlobalEnvironment.getInstance().variables[overrideAcejump] == 1) {
            MappingConfigurator.configureMappings()
        }
    }

/*
    private object LineMarks : HandlerProcessor(EXCLUSIVE) {
        override fun customization(editor: Editor, session: Session) {
            if (editor.mode.isEndAllowed) {
                Handler.regexSearch(LINE_MARK, SCREEN_BOUNDARY)
            } else {
                Handler.customRegexSearch(
                    "$LINE_END_NO_NEWLINE|${START_OF_LINE.string}",
                    SCREEN_BOUNDARY
                )
            }
        }
    }
*/

/*
    private class RepeatSearch(
        val forward: Boolean,
        val respectVimDirection: Boolean,
        val bidirect: Boolean = false
    ) : HandlerProcessor(EXCLUSIVE) {
        override fun customization(editor: Editor, session: Session) {
            val lastSearch = VimPlugin.getSearch().lastSearch ?: run {
                Handler.reset()
                return
            }
            val lastDirection = VimPlugin.getSearch().lastDir

            val currentOffset = editor.caretModel.offset
            val currentLine = editor.caretModel.logicalPosition.line

            val lineRange = if (bidirect) {
                0 to -1
            } else {
                if (respectVimDirection) {
                    if (forward) {
                        if (lastDirection > 0) currentLine to -1 else 0 to currentLine
                    } else {
                        if (lastDirection > 0) 0 to currentLine else currentLine to -1
                    }
                } else {
                    if (forward) currentLine to -1 else 0 to currentLine
                }
            }

            val startOffsets = SearchGroup.findAll(editor, lastSearch, lineRange.first, lineRange.second, false)
                .map { it.startOffset }
                .filter { if (bidirect) true else if (lineRange.second == -1) it > currentOffset else it < currentOffset }
                .toSortedSet()

            Finder.markResults(startOffsets)
        }
    }
*/

/*
    private class Jumptoanywhere : HandlerProcessor(EXCLUSIVE) {
        override fun customization(editor: Editor, session: Session) {
            val pattern = VimScriptGlobalEnvironment.getInstance().variables[jumpAnywhere] as? String ?: return

            val startOffsets = SearchGroup.findAll(editor, pattern, 0, -1, false)
                .map { it.startOffset }
                .toSortedSet()
            Finder.markResults(startOffsets)
        }
    }
*/

    /** Directions as in vim */
/*
    private class JumptoanywhereInLine(private val direction: Int) : HandlerProcessor(EXCLUSIVE) {
        override fun customization(editor: Editor, session: Session) {
            val pattern = VimScriptGlobalEnvironment.getInstance().variables[lineJumpAnywhere] as? String ?: return
            val boundary = when (direction) {
                0 -> CURRENT_LINE_AFTER_CARET_BOUNDARY
                1 -> CURRENT_LINE_BEFORE_CARET_BOUNDARY
                else -> CURRENT_LINE_BOUNDARY
            }

            val currentLine = editor.caretModel.logicalPosition.line
            val startOffsets = SearchGroup.findAll(editor, pattern, currentLine, currentLine, false)
                .map { it.startOffset }
                .filter { it in boundary }
                .toSortedSet()
            Finder.markResults(startOffsets)
        }
    }
*/

    private class KeyWordStart(val boundary: Boundaries) : HandlerProcessor(EXCLUSIVE) {
        override fun customization(editor: Editor, session: Session) {
            val kw = keywordRegex()?.let {
                "((?<=\\s|\\A|[^$it])[$it])|" +  // Take a char from keyword that is preceded by a not-keyword char, or
                        "((?<=[$it])[^$it\\s])|" // non-keyword char that is preceded by a keyword char.
            } ?: ""
            val regex = "$kw$WORD"
            session.startRegexSearch(regex, boundary)
        }
    }

    private class KeyWordEnd(val boundary: Boundaries, motionType: MotionType) : HandlerProcessor(motionType) {
        override fun customization(editor: Editor, session: Session) {
            val kw = keywordRegex()?.let {
                "([$it](?=\\s|\\Z|[^$it]))|" +  // Take a char from keyword that is preceded by a not-keyword char, or
                        "([^$it\\s](?=[$it]))|" // non-keyword char that is preceded by a keyword char.
            } ?: ""
            val regex = "$kw$WORD_END"
            session.startRegexSearch(regex, boundary)
        }
    }

    private class CustomPattern(
        val pattern: String,
        val boundary: Boundaries,
        motionType: MotionType
    ) : HandlerProcessor(motionType) {
        override fun customization(editor: Editor, session: Session) {
            session.startRegexSearch(pattern, boundary)
//            Handler.customRegexSearch(pattern, boundary)
        }
    }

    private class JkMotion(val boundary: Boundaries) : HandlerProcessor(LINE) {
        override fun customization(editor: Editor, session: Session) {
            if (VimScriptGlobalEnvironment.getInstance().variables[startOfLine] != 0) {
                session.startRegexSearch(Pattern.LINE_INDENTS, boundary)
            } else {
                val vp = editor.caretModel.visualPosition
                val res = when (boundary) {
                    StandardBoundaries.AFTER_CARET -> generateLineOffsets(editor, vp, true)
                    StandardBoundaries.BEFORE_CARET -> generateLineOffsets(editor, vp, false)
                    else -> throw UnsupportedOperationException("This boundary is not supported: $boundary")
                }
                session.markResults(res.toSortedSet())
            }
        }

        private fun generateLineOffsets(
            editor: Editor,
            vp: VisualPosition,
            direction: Boolean
        ): MutableSet<Int> {
            val dir = if (direction) 1 else -1
            var counter = 0
            val res = mutableSetOf<Int>()
            val lastLine = EditorHelper.getVisualLineCount(editor)
            while (counter >= 0) {
                counter++
                val nextLine = vp.line + dir * counter
                if (nextLine > lastLine || nextLine < 0) break
                var offset =
                    EditorHelper.visualPositionToOffset(editor, VisualPosition(nextLine, vp.column))
                if (editor.offsetToVisualPosition(offset).column < vp.column) {
                    if (!EditorHelper.isLineEmpty(editor, editor.offsetToVisualPosition(offset).line, false)) {
                        if (!editor.mode.isEndAllowed) offset--
                    }
                }
                if (boundary.isOffsetInside(editor, offset)) {
                    res += offset
                } else {
                    counter = -1
                }
            }
            return res
        }
    }

    private class EndOfLinePattern(val boundary: Boundaries) : HandlerProcessor(LINE) {
        override fun customization(editor: Editor, session: Session) {
            if (editor.mode.isEndAllowed) {
                session.startRegexSearch(Pattern.LINE_ENDS, boundary)
            } else {
                session.startRegexSearch(LINE_END_NO_NEWLINE, boundary)
            }
        }
    }

    private class PredefinedPattern(
        val pattern: Pattern,
        val boundary: Boundaries,
        motionType: MotionType
    ) : HandlerProcessor(motionType) {
        override fun customization(editor: Editor, session: Session) {
            session.startRegexSearch(pattern, boundary)
//            Handler.regexSearch(pattern, boundary)
        }
    }

    private class MultiInput(
        motionType: MotionType
    ) : HandlerProcessor(motionType) {
        override fun customization(editor: Editor, session: Session) {
            session.toggleJumpMode(JumpMode.JUMP)
//            Model.boundaries = boundary
        }
    }

    private class MultiInputPreStop(
        motionType: MotionType
    ) : HandlerProcessor(motionType) {
        override fun customization(editor: Editor, session: Session) {
            session.toggleJumpMode(JumpMode.JUMP)
//            Model.boundaries = boundary
        }

        override fun onFinish(editor: Editor/*, queryWithSuffix: String*/) {
/*
            if (boundary == AFTER_CARET_BOUNDARY || boundary == CURRENT_LINE_AFTER_CARET_BOUNDARY) {
                editor.caretModel.moveToOffset(editor.caretModel.offset - 1)
            } else if (boundary == BEFORE_CARET_BOUNDARY || boundary == CURRENT_LINE_BEFORE_CARET_BOUNDARY) {
                val fileSize = editor.document.textLength

                // FIXME: 07/10/2019 Well, there should be a better way to find  pure query length
                val suffixSize =
                    Canvas.jumpLocations.find {
                        it.tag?.let { tag -> queryWithSuffix.endsWith(tag) } ?: false
                    }?.tag?.length ?: 1

                val newOffset =
                    (editor.caretModel.offset + (queryWithSuffix.length - suffixSize)).coerceAtMost(fileSize)
                editor.caretModel.moveToOffset(newOffset)
            }
*/
        }
    }

    private class BiDirectionalPreStop(val inLine: Boolean) : HandlerProcessor(BIDIRECTIONAL_INCLUSIVE) {
        var caretPosition: Int? = null

        override fun customization(editor: Editor, session: Session) {
//            Model.boundaries = if (inLine) CURRENT_LINE_BOUNDARY else SCREEN_BOUNDARY
            caretPosition = editor.caretModel.offset
        }

        override fun onFinish(editor: Editor/*, queryWithSuffix: String*/) {
/*
            val oldCaretOffset = caretPosition.also { caretPosition = null } ?: return
            val newCaretOffset = editor.caretModel.offset
            if (newCaretOffset > oldCaretOffset) {
                editor.caretModel.moveToOffset(editor.caretModel.offset - 1)
            } else if (newCaretOffset < oldCaretOffset) {
                val fileSize = editor.document.textLength

                val suffixSize =
                    Canvas.jumpLocations.find {
                        it.tag?.let { tag -> queryWithSuffix.endsWith(tag) } ?: false
                    }?.tag?.length ?: 1

                val newOffset =
                    (editor.caretModel.offset + (queryWithSuffix.length - suffixSize)).coerceAtMost(fileSize)
                editor.caretModel.moveToOffset(newOffset)
            }
*/
        }
    }
}
