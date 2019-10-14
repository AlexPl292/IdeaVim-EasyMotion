@file:Suppress("PrivatePropertyName")

package org.jetbrains.plugins

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.VisualPosition
import com.maddyhome.idea.vim.VimPlugin
import com.maddyhome.idea.vim.command.CommandState
import com.maddyhome.idea.vim.command.MappingMode
import com.maddyhome.idea.vim.ex.vimscript.VimScriptGlobalEnvironment
import com.maddyhome.idea.vim.extension.VimExtensionFacade.putKeyMapping
import com.maddyhome.idea.vim.extension.VimNonDisposableExtension
import com.maddyhome.idea.vim.group.SearchGroup
import com.maddyhome.idea.vim.group.visual.vimSetSelection
import com.maddyhome.idea.vim.helper.EditorHelper
import com.maddyhome.idea.vim.helper.StringHelper.parseKeys
import com.maddyhome.idea.vim.helper.isEndAllowed
import com.maddyhome.idea.vim.helper.mode
import org.acejump.control.Handler
import org.acejump.label.Pattern
import org.acejump.label.Pattern.*
import org.acejump.search.Finder
import org.acejump.view.Boundary
import org.acejump.view.Boundary.*
import org.acejump.view.Canvas
import org.acejump.view.Model
import org.intellij.lang.annotations.Language

class EasyMotionExtension : VimNonDisposableExtension() {
    override fun getName(): String = "easymotion"

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

        private const val defaultRe = """\v(<.|^${'$'})|(.>|^${'$'})|(\l)\zs(\u)|(_\zs.)|(#\zs.)"""
    }

    override fun initOnce() {
        VimScriptGlobalEnvironment.getInstance().variables.let { vars ->
            vars[jumpAnywhere] = defaultRe
            vars[lineJumpAnywhere] = defaultRe
            if (doMapping !in vars) vars[doMapping] = 1
            if (startOfLine !in vars) vars[startOfLine] = 1
        }

        // -----------  Default mapping table ---------------------//
        mapToFunctionAndProvideKeys("f", MultiInput(AFTER_CARET_BOUNDARY))          // Works as `fn`
        mapToFunctionAndProvideKeys("F", MultiInput(BEFORE_CARET_BOUNDARY))         // Works as `Fn`
        mapToFunctionAndProvideKeys("t", MultiInputPreStop(AFTER_CARET_BOUNDARY))   // Works as `tn`
        mapToFunctionAndProvideKeys("T", MultiInputPreStop(BEFORE_CARET_BOUNDARY))   // Works as `Tn`
        mapToFunctionAndProvideKeys("w", PredefinedPattern(ALL_WORDS, AFTER_CARET_BOUNDARY, false))
        mapToFunctionAndProvideKeys("W", CustomPattern(WORD, AFTER_CARET_BOUNDARY))
        mapToFunctionAndProvideKeys("b", PredefinedPattern(ALL_WORDS, BEFORE_CARET_BOUNDARY, false))
        mapToFunctionAndProvideKeys("B", CustomPattern(WORD, BEFORE_CARET_BOUNDARY))
        mapToFunctionAndProvideKeys("e", CustomPattern(wordEnd, AFTER_CARET_BOUNDARY))
        mapToFunctionAndProvideKeys("E", CustomPattern(WORD_END, AFTER_CARET_BOUNDARY))
        mapToFunctionAndProvideKeys("ge", CustomPattern(wordEnd, BEFORE_CARET_BOUNDARY))
        mapToFunctionAndProvideKeys("gE", CustomPattern(WORD_END, BEFORE_CARET_BOUNDARY))
        mapToFunctionAndProvideKeys("j", JkMotion(AFTER_CARET_BOUNDARY))
        mapToFunctionAndProvideKeys("k", JkMotion(BEFORE_CARET_BOUNDARY))
        mapToFunctionAndProvideKeys("s", MultiInput(SCREEN_BOUNDARY))  // Works as `sn`
        mapToFunctionAndProvideKeys("n", RepeatSearch(forward = true, respectVimDirection = false))
        mapToFunctionAndProvideKeys("N", RepeatSearch(forward = false, respectVimDirection = false))

        // ------------ Extended mapping table -------------------//
        mapToFunction("bd-f", MultiInput(SCREEN_BOUNDARY))
        mapToFunction("bd-t", BiDirectionalPreStop(false))
        mapToFunction("bd-w", PredefinedPattern(ALL_WORDS, SCREEN_BOUNDARY, false))
        mapToFunction("bd-W", CustomPattern(WORD, SCREEN_BOUNDARY))
        mapToFunction("bd-e", CustomPattern(wordEnd, SCREEN_BOUNDARY))
        mapToFunction("bd-E", CustomPattern(WORD_END, SCREEN_BOUNDARY))
        mapToFunction("bd-jk", PredefinedPattern(CODE_INDENTS, FULL_FILE_BOUNDARY, true))
        mapToFunction("bd-n", RepeatSearch(forward = false, respectVimDirection = false, bidirect = true))
        mapToFunction("jumptoanywhere", Jumptoanywhere())
        mapToFunction("sol-j", PredefinedPattern(START_OF_LINE, AFTER_CARET_BOUNDARY, true))
        mapToFunction("sol-k", PredefinedPattern(START_OF_LINE, BEFORE_CARET_BOUNDARY, true))
        mapToFunction("eol-j", EndOfLinePattern(AFTER_CARET_BOUNDARY))
        mapToFunction("eol-k",EndOfLinePattern (BEFORE_CARET_BOUNDARY))
        mapToFunction("iskeyword-w", KeyWordStart(AFTER_CARET_BOUNDARY))
        mapToFunction("iskeyword-b", KeyWordStart(BEFORE_CARET_BOUNDARY))
        mapToFunction("iskeyword-bd-w", KeyWordStart(SCREEN_BOUNDARY))
        mapToFunction("iskeyword-e", KeyWordEnd(AFTER_CARET_BOUNDARY))
        mapToFunction("iskeyword-ge", KeyWordEnd(BEFORE_CARET_BOUNDARY))
        mapToFunction("iskeyword-bd-e", KeyWordEnd(SCREEN_BOUNDARY))
        mapToFunction("vim-n", RepeatSearch(forward = true, respectVimDirection = true))
        mapToFunction("vim-N", RepeatSearch(forward = false, respectVimDirection = true))

        // ------------ Within Line Motion -----------------------//
        mapToFunction("sl", MultiInput(CURRENT_LINE_BOUNDARY))             // Works as `sln`
        mapToFunction("fl", MultiInput(CURRENT_LINE_AFTER_CARET))          // Works as `fln`
        mapToFunction("Fl", MultiInput(CURRENT_LINE_BEFORE_CARET))         // Works as `Fln`
        mapToFunction("bd-fl", MultiInput(CURRENT_LINE_BOUNDARY))          // Works as `sln`
        mapToFunction("tl", MultiInputPreStop(CURRENT_LINE_AFTER_CARET))   // Works as `tln`
        mapToFunction("Tl", MultiInputPreStop(CURRENT_LINE_BEFORE_CARET))  // Works as `Tln`
        mapToFunction("bd-tl", BiDirectionalPreStop(true))            // Works as `bd-tln`
        mapToFunction("wl", PredefinedPattern(ALL_WORDS, CURRENT_LINE_AFTER_CARET))
        mapToFunction("bl", PredefinedPattern(ALL_WORDS, CURRENT_LINE_BEFORE_CARET))
        mapToFunction("bd-wl", PredefinedPattern(ALL_WORDS, CURRENT_LINE_BOUNDARY))
        mapToFunction("el", CustomPattern(wordEnd, CURRENT_LINE_AFTER_CARET))
        mapToFunction("gel", CustomPattern(wordEnd, CURRENT_LINE_BEFORE_CARET))
        mapToFunction("bd-el", CustomPattern(wordEnd, CURRENT_LINE_BOUNDARY))
        mapToFunction("lineforward", JumptoanywhereInLine(0))
        mapToFunction("linebackward", JumptoanywhereInLine(1))
        mapToFunction("lineanywhere", JumptoanywhereInLine(2))

        // ------------ Multi input mapping table ----------------//
        mapToFunction("s2", MultiInput(SCREEN_BOUNDARY))                              // Works as `sn`
        mapToFunction("f2", MultiInput(AFTER_CARET_BOUNDARY))                         // Works as `fn`
        mapToFunction("F2", MultiInput(BEFORE_CARET_BOUNDARY))                        // Works as `Fn`
        mapToFunction("bd-f2", MultiInput(SCREEN_BOUNDARY))                           // Works as `sn`
        mapToFunction("t2", MultiInputPreStop(AFTER_CARET_BOUNDARY))                  // Works as `tn`
        mapToFunction("T2", MultiInputPreStop(BEFORE_CARET_BOUNDARY))                 // Works as `Tn`
        mapToFunction("bd-t2", BiDirectionalPreStop(false))

        mapToFunction("sl2", MultiInput(CURRENT_LINE_BOUNDARY))             // Works as `sln`
        mapToFunction("fl2", MultiInput(CURRENT_LINE_AFTER_CARET))          // Works as `fln`
        mapToFunction("Fl2", MultiInput(CURRENT_LINE_BEFORE_CARET))         // Works as `Fln`
        mapToFunction("tl2", MultiInputPreStop(CURRENT_LINE_AFTER_CARET))   // Works as `tln`
        mapToFunction("Tl2", MultiInputPreStop(CURRENT_LINE_BEFORE_CARET))  // Works as `Tln`

        mapToFunction("sn", MultiInput(SCREEN_BOUNDARY))
        mapToFunction("fn", MultiInput(AFTER_CARET_BOUNDARY))
        mapToFunction("Fn", MultiInput(BEFORE_CARET_BOUNDARY))
        mapToFunction("bd-fn", MultiInput(SCREEN_BOUNDARY))
        mapToFunction("tn", MultiInputPreStop(AFTER_CARET_BOUNDARY))
        mapToFunction("Tn", MultiInputPreStop(BEFORE_CARET_BOUNDARY))
        mapToFunction("bd-tn", BiDirectionalPreStop(false))

        mapToFunction("sln", MultiInput(CURRENT_LINE_BOUNDARY))
        mapToFunction("fln", MultiInput(CURRENT_LINE_AFTER_CARET))
        mapToFunction("Fln", MultiInput(CURRENT_LINE_BEFORE_CARET))
        mapToFunction("bd-fln", MultiInput(CURRENT_LINE_BOUNDARY))
        mapToFunction("tln", MultiInputPreStop(CURRENT_LINE_AFTER_CARET))
        mapToFunction("Tln", MultiInputPreStop(CURRENT_LINE_BEFORE_CARET))
        mapToFunction("bd-tln", BiDirectionalPreStop(true))

        putKeyMapping(MappingMode.NVO, parseKeys(defaultPrefix), parseKeys(pluginPrefix), true)
    }

    private class RepeatSearch(
        val forward: Boolean,
        val respectVimDirection: Boolean,
        val bidirect: Boolean = false
    ) : HandlerProcessor {
        override fun customization(editor: Editor) {
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

    private class Jumptoanywhere : HandlerProcessor {
        override fun customization(editor: Editor) {
            val pattern = VimScriptGlobalEnvironment.getInstance().variables[jumpAnywhere] as? String ?: return

            val startOffsets = SearchGroup.findAll(editor, pattern, 0, -1, false)
                .map { it.startOffset }
                .toSortedSet()
            Finder.markResults(startOffsets)
        }
    }

    /** Directions as in vim */
    private class JumptoanywhereInLine(private val direction: Int) : HandlerProcessor {
        override fun customization(editor: Editor) {
            val pattern = VimScriptGlobalEnvironment.getInstance().variables[lineJumpAnywhere] as? String ?: return
            val boundary = when (direction) {
                0 -> CURRENT_LINE_AFTER_CARET
                1 -> CURRENT_LINE_BEFORE_CARET
                else -> CURRENT_LINE_BOUNDARY
            }

            val currentLine = editor.caretModel.logicalPosition.line
            val currentOffset = editor.caretModel.offset
            val startOffsets = SearchGroup.findAll(editor, pattern, currentLine, currentLine, false)
                .map { it.startOffset }
                .filter { it in boundary }
                .toSortedSet()
            Finder.markResults(startOffsets)
        }
    }

    private class KeyWordStart(val boundary: Boundary) : HandlerProcessor {
        override fun customization(editor: Editor) {
            val kw = keywordRegex()?.let {
                "((?<=\\s|\\A|[^$it])[$it])|" +  // Take a char from keyword that is preceded by a not-keyword char, or
                        "((?<=[$it])[^$it\\s])|" // non-keyword char that is preceded by a keyword char.
            } ?: ""
            val regex = "$kw$WORD"
            Handler.cutsomRegexSearch(regex, boundary)
        }
    }

    private class KeyWordEnd(val boundary: Boundary) : HandlerProcessor {
        override fun customization(editor: Editor) {
            val kw = keywordRegex()?.let {
                "([$it](?=\\s|\\Z|[^$it]))|" +  // Take a char from keyword that is preceded by a not-keyword char, or
                        "([^$it\\s](?=[$it]))|" // non-keyword char that is preceded by a keyword char.
            } ?: ""
            val regex = "$kw$WORD_END"
            Handler.cutsomRegexSearch(regex, boundary)
        }
    }

    private class CustomPattern(
        val pattern: String,
        val boundary: Boundary
    ) : HandlerProcessor {
        override fun customization(editor: Editor) {
            Handler.cutsomRegexSearch(pattern, boundary)
        }
    }

    private class JkMotion(val boundary: Boundary) : HandlerProcessor {
        private var initialOffset: Int? = null

        override fun customization(editor: Editor) {
            initialOffset = editor.caretModel.currentCaret.offset
            if (VimScriptGlobalEnvironment.getInstance().variables[startOfLine] != 0) {
                Handler.regexSearch(CODE_INDENTS, boundary)
            } else {
                val vp = editor.caretModel.visualPosition
                val res = when (boundary) {
                    AFTER_CARET_BOUNDARY -> generateLineOffsets(editor, vp, true)
                    BEFORE_CARET_BOUNDARY -> generateLineOffsets(editor, vp, false)
                    else -> throw UnsupportedOperationException("This boundary is not supported: $boundary")
                }
                Finder.markResults(res.toSortedSet())
            }
        }

        override fun onFinish(editor: Editor, queryWithSuffix: String) {
            val myInitialOffset = initialOffset
            if (myInitialOffset != null && CommandState.getInstance(editor).mappingMode == MappingMode.OP_PENDING) {
                VimPlugin.getVisualMotion().enterVisualMode(editor, CommandState.SubMode.VISUAL_LINE)
                editor.caretModel.currentCaret.vimSetSelection(myInitialOffset, editor.caretModel.currentCaret.offset)
            }
            initialOffset = null
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
                if (offset in boundary) {
                    res += offset
                } else {
                    counter = -1
                }
            }
            return res
        }
    }

    private class EndOfLinePattern(val boundary: Boundary) : HandlerProcessor {

        private var initialOffset: Int? = null

        override fun customization(editor: Editor) {
            initialOffset = editor.caretModel.currentCaret.offset
            if (editor.mode.isEndAllowed) {
                Handler.regexSearch(END_OF_LINE, boundary)
            } else {
                Handler.cutsomRegexSearch(LINE_END_NO_NEWLINE, boundary)
            }
        }

        override fun onFinish(editor: Editor, queryWithSuffix: String) {
            val myInitialOffset = initialOffset
            if (myInitialOffset != null && CommandState.getInstance(editor).mappingMode == MappingMode.OP_PENDING) {
                VimPlugin.getVisualMotion().enterVisualMode(editor, CommandState.SubMode.VISUAL_LINE)
                editor.caretModel.currentCaret.vimSetSelection(myInitialOffset, editor.caretModel.currentCaret.offset)
            }
            initialOffset = null
        }
    }

    private class PredefinedPattern(
        val pattern: Pattern,
        val boundary: Boundary,
        val linewise: Boolean = false
    ) : HandlerProcessor {

        private var initialOffset: Int? = null

        override fun customization(editor: Editor) {
            initialOffset = editor.caretModel.currentCaret.offset
            Handler.regexSearch(pattern, boundary)
        }

        override fun onFinish(editor: Editor, queryWithSuffix: String) {
            val myInitialOffset = initialOffset
            if (myInitialOffset != null && linewise && CommandState.getInstance(editor).mappingMode == MappingMode.OP_PENDING) {
                VimPlugin.getVisualMotion().enterVisualMode(editor, CommandState.SubMode.VISUAL_LINE)
                editor.caretModel.currentCaret.vimSetSelection(myInitialOffset, editor.caretModel.currentCaret.offset)
            }
            initialOffset = null
        }
    }

    private class MultiInput(val boundary: Boundary) : HandlerProcessor {
        override fun customization(editor: Editor) {
            Model.boundaries = boundary
        }
    }

    private class MultiInputPreStop(val boundary: Boundary) : HandlerProcessor {
        override fun customization(editor: Editor) {
            Model.boundaries = boundary
        }

        override fun onFinish(editor: Editor, queryWithSuffix: String) {
            if (boundary == AFTER_CARET_BOUNDARY || boundary == CURRENT_LINE_AFTER_CARET) {
                editor.caretModel.moveToOffset(editor.caretModel.offset - 1)
            } else if (boundary == BEFORE_CARET_BOUNDARY || boundary == CURRENT_LINE_BEFORE_CARET) {
                val fileSize = EditorHelper.getFileSize(editor, true)

                // FIXME: 07/10/2019 Well, there should be a better way to find  pure query length
                val suffixSize =
                    Canvas.jumpLocations.find {
                        it.tag?.let { tag -> queryWithSuffix.endsWith(tag) } ?: false
                    }?.tag?.length ?: 1

                val newOffset =
                    (editor.caretModel.offset + (queryWithSuffix.length - suffixSize)).coerceAtMost(fileSize)
                editor.caretModel.moveToOffset(newOffset)
            }
        }
    }

    private class BiDirectionalPreStop(val inLine: Boolean) : HandlerProcessor {
        var caretPosition: Int? = null

        override fun customization(editor: Editor) {
            Model.boundaries = if (inLine) CURRENT_LINE_BOUNDARY else SCREEN_BOUNDARY
            caretPosition = editor.caretModel.offset
        }

        override fun onFinish(editor: Editor, queryWithSuffix: String) {
            val oldCaretOffset = caretPosition.also { caretPosition = null } ?: return
            val newCaretOffset = editor.caretModel.offset
            if (newCaretOffset > oldCaretOffset) {
                editor.caretModel.moveToOffset(editor.caretModel.offset - 1)
            } else if (newCaretOffset < oldCaretOffset) {
                val fileSize = EditorHelper.getFileSize(editor, true)

                val suffixSize =
                    Canvas.jumpLocations.find {
                        it.tag?.let { tag -> queryWithSuffix.endsWith(tag) } ?: false
                    }?.tag?.length ?: 1

                val newOffset =
                    (editor.caretModel.offset + (queryWithSuffix.length - suffixSize)).coerceAtMost(fileSize)
                editor.caretModel.moveToOffset(newOffset)
            }
        }
    }
}
