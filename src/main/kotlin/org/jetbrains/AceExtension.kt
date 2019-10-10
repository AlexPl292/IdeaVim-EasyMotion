@file:Suppress("PrivatePropertyName")

package org.jetbrains

import com.intellij.openapi.editor.Editor
import com.maddyhome.idea.vim.command.MappingMode
import com.maddyhome.idea.vim.ex.vimscript.VimScriptGlobalEnvironment
import com.maddyhome.idea.vim.extension.VimExtensionFacade.putKeyMapping
import com.maddyhome.idea.vim.extension.VimNonDisposableExtension
import com.maddyhome.idea.vim.group.SearchGroup
import com.maddyhome.idea.vim.helper.EditorHelper
import com.maddyhome.idea.vim.helper.StringHelper.parseKeys
import org.acejump.control.Handler
import org.acejump.label.Pattern
import org.acejump.label.Pattern.*
import org.acejump.search.Finder
import org.acejump.view.Boundary
import org.acejump.view.Boundary.*
import org.acejump.view.Canvas
import org.acejump.view.Model
import org.intellij.lang.annotations.Language

class AceExtension : VimNonDisposableExtension() {
    override fun getName(): String = "acejump"

    companion object {
        const val pluginPrefix = "<Plug>(easymotion-prefix)"
        const val defaultPrefix = "<leader><leader>"

        @Language("RegExp")
        private const val wordEnd = "[a-zA-Z0-9_](?=[^a-zA-Z0-9_]|\\Z)"
        @Language("RegExp")
        private const val WORD = "(?<=\\s|\\A)[^\\s]"
        @Language("RegExp")
        private const val WORD_END = "[^\\s](?=\\s|\\Z)"

        private const val jumpAnywhere = "g:EasyMotion_re_anywhere"
        private const val lineJumpAnywhere = "g:EasyMotion_re_line_anywhere"
        private const val defaultRe = """\v(<.|^${'$'})|(.>|^${'$'})|(\l)\zs(\u)|(_\zs.)|(#\zs.)"""
    }

    override fun initOnce() {
        VimScriptGlobalEnvironment.getInstance().variables[jumpAnywhere] = defaultRe
        VimScriptGlobalEnvironment.getInstance().variables[lineJumpAnywhere] = defaultRe

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
        mapToFunctionAndProvideKeys("j", PredefinedPattern(CODE_INDENTS, AFTER_CARET_BOUNDARY, true))
        mapToFunctionAndProvideKeys("k", PredefinedPattern(CODE_INDENTS, BEFORE_CARET_BOUNDARY, true))
        mapToFunctionAndProvideKeys("s", MultiInput(SCREEN_BOUNDARY))  // Works as `sn`

        // ------------ Extended mapping table -------------------//
        mapToFunction("bd-f", MultiInput(SCREEN_BOUNDARY))
        mapToFunction("bd-t", BiDirectionalPreStop())
        mapToFunction("bd-w", PredefinedPattern(ALL_WORDS, SCREEN_BOUNDARY, false))
        mapToFunction("bd-W", CustomPattern(WORD, SCREEN_BOUNDARY))
        mapToFunction("bd-e", CustomPattern(wordEnd, SCREEN_BOUNDARY))
        mapToFunction("bd-E", CustomPattern(WORD_END, SCREEN_BOUNDARY))
        mapToFunction("bd-jk", PredefinedPattern(CODE_INDENTS, FULL_FILE_BOUNDARY, true))
        mapToFunction("jumptoanywhere", Jumptoanywhere())
        mapToFunction("sol-j", PredefinedPattern(START_OF_LINE, AFTER_CARET_BOUNDARY, true))
        mapToFunction("sol-k", PredefinedPattern(START_OF_LINE, BEFORE_CARET_BOUNDARY, true))
        mapToFunction("eol-j", PredefinedPattern(END_OF_LINE, AFTER_CARET_BOUNDARY, true))
        mapToFunction("eol-k", PredefinedPattern(END_OF_LINE, BEFORE_CARET_BOUNDARY, true))
        mapToFunction("iskeyword-w", KeyWordStart(AFTER_CARET_BOUNDARY))
        mapToFunction("iskeyword-b", KeyWordStart(BEFORE_CARET_BOUNDARY))
        mapToFunction("iskeyword-bd-w", KeyWordStart(SCREEN_BOUNDARY))
        mapToFunction("iskeyword-e", KeyWordEnd(AFTER_CARET_BOUNDARY))
        mapToFunction("iskeyword-ge", KeyWordEnd(BEFORE_CARET_BOUNDARY))
        mapToFunction("iskeyword-bd-e", KeyWordEnd(SCREEN_BOUNDARY))

        // ------------ Within Line Motion -----------------------//
        mapToFunction("sl", MultiInput(SCREEN_BOUNDARY, true))               // Works as `sln`
        mapToFunction("fl", MultiInput(AFTER_CARET_BOUNDARY, true))          // Works as `fln`
        mapToFunction("Fl", MultiInput(BEFORE_CARET_BOUNDARY, true))         // Works as `Fln`
        mapToFunction("bd-fl", MultiInput(SCREEN_BOUNDARY, true))            // Works as `sln`
        mapToFunction("tl", MultiInputPreStop(AFTER_CARET_BOUNDARY, true))   // Works as `tln`
        mapToFunction("Tl", MultiInputPreStop(BEFORE_CARET_BOUNDARY, true))  // Works as `Tln`
        mapToFunction("bd-tl", BiDirectionalPreStop(true))                    // Works as `bd-tln`
        mapToFunction("wl", PredefinedPattern(ALL_WORDS, AFTER_CARET_BOUNDARY, true))
        mapToFunction("bl", PredefinedPattern(ALL_WORDS, BEFORE_CARET_BOUNDARY, true))
        mapToFunction("bd-wl", PredefinedPattern(ALL_WORDS, SCREEN_BOUNDARY, true))
        mapToFunction("el", CustomPattern(wordEnd, AFTER_CARET_BOUNDARY, true))
        mapToFunction("gel", CustomPattern(wordEnd, BEFORE_CARET_BOUNDARY, true))
        mapToFunction("bd-el", CustomPattern(wordEnd, SCREEN_BOUNDARY, true))
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
        mapToFunction("bd-t2", BiDirectionalPreStop())

        mapToFunction("sl2", MultiInput(SCREEN_BOUNDARY, true))               // Works as `sln`
        mapToFunction("fl2", MultiInput(AFTER_CARET_BOUNDARY, true))          // Works as `fln`
        mapToFunction("Fl2", MultiInput(BEFORE_CARET_BOUNDARY, true))         // Works as `Fln`
        mapToFunction("tl2", MultiInputPreStop(AFTER_CARET_BOUNDARY, true))   // Works as `tln`
        mapToFunction("Tl2", MultiInputPreStop(BEFORE_CARET_BOUNDARY, true))  // Works as `Tln`

        mapToFunction("sn", MultiInput(SCREEN_BOUNDARY))
        mapToFunction("fn", MultiInput(AFTER_CARET_BOUNDARY))
        mapToFunction("Fn", MultiInput(BEFORE_CARET_BOUNDARY))
        mapToFunction("bd-fn", MultiInput(SCREEN_BOUNDARY))
        mapToFunction("tn", MultiInputPreStop(AFTER_CARET_BOUNDARY))
        mapToFunction("Tn", MultiInputPreStop(BEFORE_CARET_BOUNDARY))
        mapToFunction("bd-tn", BiDirectionalPreStop())

        mapToFunction("sln", MultiInput(SCREEN_BOUNDARY, true))
        mapToFunction("fln", MultiInput(AFTER_CARET_BOUNDARY, true))
        mapToFunction("Fln", MultiInput(BEFORE_CARET_BOUNDARY, true))
        mapToFunction("bd-fln", MultiInput(SCREEN_BOUNDARY, true))
        mapToFunction("tln", MultiInputPreStop(AFTER_CARET_BOUNDARY, true))
        mapToFunction("Tln", MultiInputPreStop(BEFORE_CARET_BOUNDARY, true))
        mapToFunction("bd-tln", BiDirectionalPreStop(true))

        putKeyMapping(MappingMode.NVO, parseKeys(defaultPrefix), parseKeys(pluginPrefix), true)
    }

    private class Jumptoanywhere : HandlerProcessor(false) {
        override fun customization(editor: Editor) {
            val pattern = VimScriptGlobalEnvironment.getInstance().variables[jumpAnywhere] as? String ?: return

            val startOffsets = SearchGroup.findAll(editor, pattern, 0, -1, false)
                .map { it.startOffset }
                .toSortedSet()
            Finder.markResults(startOffsets)
        }
    }

    //** Directions as in vim */
    private class JumptoanywhereInLine(private val direction: Int) : HandlerProcessor(false) {
        override fun customization(editor: Editor) {
            val pattern = VimScriptGlobalEnvironment.getInstance().variables[lineJumpAnywhere] as? String ?: return

            val currentLine = editor.caretModel.logicalPosition.line
            val currentOffset = editor.caretModel.offset
            val startOffsets = SearchGroup.findAll(editor, pattern, currentLine, currentLine, false)
                .map { it.startOffset }

            val results = when (direction) {
                0 -> startOffsets.filter { it > currentOffset }   // forward
                1 -> startOffsets.filter { it < currentOffset }   // backward
                else -> startOffsets
            }
            Finder.markResults(results.toSortedSet())
        }
    }

    private class KeyWordStart(val boundary: Boundary) : HandlerProcessor(false) {
        override fun customization(editor: Editor) {
            val kw = keywordRegex()?.let {
                "((?<=\\s|\\A|[^$it])[$it])|" +  // Take a char from keyword that is preceded by a not-keyword char, or
                        "((?<=[$it])[^$it\\s])|" // non-keyword char that is preceded by a keyword char.
            } ?: ""
            val regex = "$kw$WORD"
            Handler.cutsomRegexSearch(regex, boundary)
        }
    }

    private class KeyWordEnd(val boundary: Boundary) : HandlerProcessor(false) {
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
        val boundary: Boundary,
        linewise: Boolean = false
    ) : HandlerProcessor(linewise) {
        override fun customization(editor: Editor) {
            Handler.cutsomRegexSearch(pattern, boundary)
        }
    }

    private class PredefinedPattern(
        val pattern: Pattern,
        val boundary: Boundary,
        linewise: Boolean
    ) : HandlerProcessor(linewise) {
        override fun customization(editor: Editor) {
            Handler.regexSearch(pattern, boundary)
        }
    }

    private class MultiInput(val boundary: Boundary, linewise: Boolean = false) : HandlerProcessor(linewise) {
        override fun customization(editor: Editor) {
            Model.boundaries = boundary
        }
    }

    private class MultiInputPreStop(val boundary: Boundary, linewise: Boolean = false) : HandlerProcessor(linewise) {
        override fun customization(editor: Editor) {
            Model.boundaries = boundary
        }

        override fun onFinish(editor: Editor, queryWithSuffix: String) {
            if (boundary == AFTER_CARET_BOUNDARY) {
                editor.caretModel.moveToOffset(editor.caretModel.offset - 1)
            } else if (boundary == BEFORE_CARET_BOUNDARY) {
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

    private class BiDirectionalPreStop(linewise: Boolean = true) : HandlerProcessor(linewise) {
        var caretPosition: Int? = null

        override fun customization(editor: Editor) {
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

/*

    <Plug> Mapping Table | Default
    ---------------------|----------------------------------------------
    <Plug>(easymotion-f) | <Leader>f{char} +  mapped to fn
    <Plug>(easymotion-F) | <Leader>F{char} +  mapped to Fn
    <Plug>(easymotion-t) | <Leader>t{char} +  mapped to tn
    <Plug>(easymotion-T) | <Leader>T{char} +  mapped to Tn
    <Plug>(easymotion-w) | <Leader>w      +
    <Plug>(easymotion-W) | <Leader>W      +
    <Plug>(easymotion-b) | <Leader>b      +
    <Plug>(easymotion-B) | <Leader>B      +
    <Plug>(easymotion-e) | <Leader>e      +
    <Plug>(easymotion-E) | <Leader>E      +
    <Plug>(easymotion-ge)| <Leader>ge     +
    <Plug>(easymotion-gE)| <Leader>gE     +
    <Plug>(easymotion-j) | <Leader>j      +
    <Plug>(easymotion-k) | <Leader>k      +
    <Plug>(easymotion-n) | <Leader>n
    <Plug>(easymotion-N) | <Leader>N
    <Plug>(easymotion-s) | <Leader>s      + mapped to sn

    More <Plug> Mapping Table         | (No assignment by default)
    ----------------------------------|---------------------------------
    <Plug>(easymotion-bd-f)           | See |<Plug>(easymotion-s)|         +
    <Plug>(easymotion-bd-t)           | See |<Plug>(easymotion-bd-t)|      +  mapped to bd-tn
    <Plug>(easymotion-bd-w)           | See |<Plug>(easymotion-bd-w)|      +
    <Plug>(easymotion-bd-W)           | See |<Plug>(easymotion-bd-W)|      +
    <Plug>(easymotion-bd-e)           | See |<Plug>(easymotion-bd-e)|      +
    <Plug>(easymotion-bd-E)           | See |<Plug>(easymotion-bd-E)|      +
    <Plug>(easymotion-bd-jk)          | See |<Plug>(easymotion-bd-jk)|     +
    <Plug>(easymotion-bd-n)           | See |<Plug>(easymotion-bd-n)|
    <Plug>(easymotion-jumptoanywhere) | See |<Plug>(easymotion-jumptoanywhere)| +
    <Plug>(easymotion-repeat)         | See |<Plug>(easymotion-repeat)|
    <Plug>(easymotion-next)           | See |<Plug>(easymotion-next)|
    <Plug>(easymotion-prev)           | See |<Plug>(easymotion-prev)|
    <Plug>(easymotion-sol-j)          | See |<Plug>(easymotion-sol-j)|     +
    <Plug>(easymotion-sol-k)          | See |<Plug>(easymotion-sol-k)|     +
    <Plug>(easymotion-eol-j)          | See |<Plug>(easymotion-eol-j)|     +
    <Plug>(easymotion-eol-k)          | See |<Plug>(easymotion-eol-k)|     +
    <Plug>(easymotion-iskeyword-w)    | See |<Plug>(easymotion-iskeyword-w)|    +
    <Plug>(easymotion-iskeyword-b)    | See |<Plug>(easymotion-iskeyword-b)|    +
    <Plug>(easymotion-iskeyword-bd-w) | See |<Plug>(easymotion-iskeyword-bd-w)| +
    <Plug>(easymotion-iskeyword-e)    | See |<Plug>(easymotion-iskeyword-e)|    +
    <Plug>(easymotion-iskeyword-ge)   | See |<Plug>(easymotion-iskeyword-ge)|   +
    <Plug>(easymotion-iskeyword-bd-e) | See |<Plug>(easymotion-iskeyword-bd-e)| +
    <Plug>(easymotion-vim-n)          | See |<Plug>(easymotion-vim-n)|
    <Plug>(easymotion-vim-N)          | See |<Plug>(easymotion-vim-N)|
                                      |
    Within Line Motion                | See |easymotion-within-line|
    ----------------------------------|---------------------------------
    <Plug>(easymotion-sl)             | See |<Plug>(easymotion-sl)|        +   mapped to sln
    <Plug>(easymotion-fl)             | See |<Plug>(easymotion-fl)|        +   mapped to fln
    <Plug>(easymotion-Fl)             | See |<Plug>(easymotion-Fl)|        +   mapped to Fln
    <Plug>(easymotion-bd-fl)          | See |<Plug>(easymotion-sl)|        +   mapped to sln
    <Plug>(easymotion-tl)             | See |<Plug>(easymotion-tl)|        +   mapped to tln
    <Plug>(easymotion-Tl)             | See |<Plug>(easymotion-Tl)|        +   mapped to Tln
    <Plug>(easymotion-bd-tl)          | See |<Plug>(easymotion-bd-tl)|     +   mapped to bd-tln
    <Plug>(easymotion-wl)             | See |<Plug>(easymotion-wl)|        +
    <Plug>(easymotion-bl)             | See |<Plug>(easymotion-bl)|        +
    <Plug>(easymotion-bd-wl)          | See |<Plug>(easymotion-bd-wl)|     +
    <Plug>(easymotion-el)             | See |<Plug>(easymotion-el)|        +
    <Plug>(easymotion-gel)            | See |<Plug>(easymotion-gel)|       +
    <Plug>(easymotion-bd-el)          | See |<Plug>(easymotion-bd-el)|     +
    <Plug>(easymotion-lineforward)    | See |<Plug>(easymotion-lineforward)|   +
    <Plug>(easymotion-linebackward)   | See |<Plug>(easymotion-linebackward)|  +
    <Plug>(easymotion-lineanywhere)   | See |<Plug>(easymotion-lineanywhere)|  +
                                      |
    Multi Input Find Motion           | See |easymotion-multi-input|
    ----------------------------------|---------------------------------
    <Plug>(easymotion-s2)             | See |<Plug>(easymotion-s2)|   +  mapped to sn
    <Plug>(easymotion-f2)             | See |<Plug>(easymotion-f2)|   +  mapped to fn
    <Plug>(easymotion-F2)             | See |<Plug>(easymotion-F2)|   +  mapped to Fn
    <Plug>(easymotion-bd-f2)          | See |<Plug>(easymotion-s2)|   +  mapped to sn
    <Plug>(easymotion-t2)             | See |<Plug>(easymotion-t2)|   +  mapped to tn
    <Plug>(easymotion-T2)             | See |<Plug>(easymotion-T2)|   +  mapped to Tn
    <Plug>(easymotion-bd-t2)          | See |<Plug>(easymotion-bd-t2)|  +  mapped to bd-tn
                                      |
    <Plug>(easymotion-sl2)            | See |<Plug>(easymotion-sl2)|   +   mapped to sln
    <Plug>(easymotion-fl2)            | See |<Plug>(easymotion-fl2)|   +   mapped to fln
    <Plug>(easymotion-Fl2)            | See |<Plug>(easymotion-Fl2)|   +   mapped to Fln
    <Plug>(easymotion-tl2)            | See |<Plug>(easymotion-tl2)|   +   mapped to tln
    <Plug>(easymotion-Tl2)            | See |<Plug>(easymotion-Tl2)|   +   mapped to Tl2
                                      |
    <Plug>(easymotion-sn)             | See |<Plug>(easymotion-sn)|    +
    <Plug>(easymotion-fn)             | See |<Plug>(easymotion-fn)|    +
    <Plug>(easymotion-Fn)             | See |<Plug>(easymotion-Fn)|    +
    <Plug>(easymotion-bd-fn)          | See |<Plug>(easymotion-sn)|    +
    <Plug>(easymotion-tn)             | See |<Plug>(easymotion-tn)|    +
    <Plug>(easymotion-Tn)             | See |<Plug>(easymotion-Tn)|    +
    <Plug>(easymotion-bd-tn)          | See |<Plug>(easymotion-bd-tn)| +
                                      |
    <Plug>(easymotion-sln)            | See |<Plug>(easymotion-sln)|   +
    <Plug>(easymotion-fln)            | See |<Plug>(easymotion-fln)|   +
    <Plug>(easymotion-Fln)            | See |<Plug>(easymotion-Fln)|   +
    <Plug>(easymotion-bd-fln)         | See |<Plug>(easymotion-sln)|   +
    <Plug>(easymotion-tln)            | See |<Plug>(easymotion-tln)|   +
    <Plug>(easymotion-Tln)            | See |<Plug>(easymotion-Tln)|   +
    <Plug>(easymotion-bd-tln)         | See |<Plug>(easymotion-bd-tln)|+

    Over Window Motion                | (No assignment by default)
    ----------------------------------|---------------------------------
    <Plug>(easymotion-overwin-f)      | See |<Plug>(easymotion-overwin-f)|
    <Plug>(easymotion-overwin-f2)     | See |<Plug>(easymotion-overwin-f2)|
    <Plug>(easymotion-overwin-line)   | See |<Plug>(easymotion-overwin-line)|
    <Plug>(easymotion-overwin-w)      | See |<Plug>(easymotion-overwin-w)|
 */
