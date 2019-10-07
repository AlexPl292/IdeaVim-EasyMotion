@file:Suppress("PrivatePropertyName")

package org.jetbrains

import com.maddyhome.idea.vim.command.MappingMode
import com.maddyhome.idea.vim.extension.VimExtensionFacade.putKeyMapping
import com.maddyhome.idea.vim.extension.VimNonDisposableExtension
import com.maddyhome.idea.vim.helper.StringHelper.parseKeys
import org.acejump.control.Handler
import org.acejump.label.Pattern
import org.acejump.label.Pattern.*
import org.acejump.view.Boundary
import org.acejump.view.Boundary.*
import org.acejump.view.Model

class AceExtension : VimNonDisposableExtension() {
    override fun getName(): String = "acejump"

    companion object {
        const val pluginPrefix = "<Plug>(easymotion-prefix)"
        const val defaultPrefix = "<leader><leader>"
    }

    override fun initOnce() {
        // -----------  Default mapping table ---------------------//
        mapToFunctionAndProvideKeys("f", MultiInput(AFTER_CARET_BOUNDARY))   // Works as `fn`
        mapToFunctionAndProvideKeys("F", MultiInput(BEFORE_CARET_BOUNDARY))  // Works as `Fn`
        mapToFunctionAndProvideKeys("w", BidirectionalLine(ALL_WORDS, AFTER_CARET_BOUNDARY))
        mapToFunctionAndProvideKeys("b", BidirectionalLine(ALL_WORDS, BEFORE_CARET_BOUNDARY))
        mapToFunctionAndProvideKeys("j", BidirectionalLine(CODE_INDENTS, AFTER_CARET_BOUNDARY))
        mapToFunctionAndProvideKeys("k", BidirectionalLine(CODE_INDENTS, BEFORE_CARET_BOUNDARY))
        mapToFunctionAndProvideKeys("s", BidirectionalMultiInput)  // Works as `sn`

        // ------------ Extended mapping table -------------------//
        mapToFunction("bd-w", BidirectionalLine(ALL_WORDS, SCREEN_BOUNDARY))
        mapToFunction("bd-jk", BidirectionalLine(CODE_INDENTS, FULL_FILE_BOUNDARY))
        mapToFunction("sol-j", BidirectionalLine(START_OF_LINE, AFTER_CARET_BOUNDARY))
        mapToFunction("sol-k", BidirectionalLine(START_OF_LINE, BEFORE_CARET_BOUNDARY))
        mapToFunction("eol-j", BidirectionalLine(END_OF_LINE, AFTER_CARET_BOUNDARY))
        mapToFunction("eol-k", BidirectionalLine(END_OF_LINE, BEFORE_CARET_BOUNDARY))

        // ------------ Multi input mapping table ----------------//
        mapToFunction("s2", BidirectionalMultiInput)                              // Works as `sn`
        mapToFunction("f2", MultiInput(AFTER_CARET_BOUNDARY))             // Works as `fn`
        mapToFunction("F2", MultiInput(BEFORE_CARET_BOUNDARY))            // Works as `Fn`
        mapToFunction("bd-f2", BidirectionalMultiInput)                           // Works as `sn`
        mapToFunction("sn", BidirectionalMultiInput)
        mapToFunction("fn", MultiInput(AFTER_CARET_BOUNDARY))
        mapToFunction("Fn", MultiInput(BEFORE_CARET_BOUNDARY))
        mapToFunction("bd-fn", BidirectionalMultiInput)

        putKeyMapping(MappingMode.NVO, parseKeys(defaultPrefix), parseKeys(pluginPrefix), true)
    }

    private object BidirectionalMultiInput : HandlerProcessor

    private class BidirectionalLine(val pattern: Pattern, val bounds: Boundary) : HandlerProcessor {
        override fun customization() {
            Handler.regexSearch(pattern, bounds)
        }
    }

    private class MultiInput(val boundary: Boundary) : HandlerProcessor {
        override fun customization() {
            Model.boundaries = boundary
        }
    }
}

/*

    <Plug> Mapping Table | Default
    ---------------------|----------------------------------------------
    <Plug>(easymotion-f) | <Leader>f{char} +  mapped to fn
    <Plug>(easymotion-F) | <Leader>F{char} +  mapped to Fn
    <Plug>(easymotion-t) | <Leader>t{char}
    <Plug>(easymotion-T) | <Leader>T{char}
    <Plug>(easymotion-w) | <Leader>w      +
    <Plug>(easymotion-W) | <Leader>W
    <Plug>(easymotion-b) | <Leader>b      +
    <Plug>(easymotion-B) | <Leader>B
    <Plug>(easymotion-e) | <Leader>e
    <Plug>(easymotion-E) | <Leader>E
    <Plug>(easymotion-ge)| <Leader>ge
    <Plug>(easymotion-gE)| <Leader>gE
    <Plug>(easymotion-j) | <Leader>j      +
    <Plug>(easymotion-k) | <Leader>k      +
    <Plug>(easymotion-n) | <Leader>n
    <Plug>(easymotion-N) | <Leader>N
    <Plug>(easymotion-s) | <Leader>s      + mapped to sn

    More <Plug> Mapping Table         | (No assignment by default)
    ----------------------------------|---------------------------------
    <Plug>(easymotion-bd-f)           | See |<Plug>(easymotion-s)|
    <Plug>(easymotion-bd-t)           | See |<Plug>(easymotion-bd-t)|
    <Plug>(easymotion-bd-w)           | See |<Plug>(easymotion-bd-w)|      +
    <Plug>(easymotion-bd-W)           | See |<Plug>(easymotion-bd-W)|
    <Plug>(easymotion-bd-e)           | See |<Plug>(easymotion-bd-e)|
    <Plug>(easymotion-bd-E)           | See |<Plug>(easymotion-bd-E)|
    <Plug>(easymotion-bd-jk)          | See |<Plug>(easymotion-bd-jk)|     +
    <Plug>(easymotion-bd-n)           | See |<Plug>(easymotion-bd-n)|
    <Plug>(easymotion-jumptoanywhere) | See |<Plug>(easymotion-jumptoanywhere)|
    <Plug>(easymotion-repeat)         | See |<Plug>(easymotion-repeat)|
    <Plug>(easymotion-next)           | See |<Plug>(easymotion-next)|
    <Plug>(easymotion-prev)           | See |<Plug>(easymotion-prev)|
    <Plug>(easymotion-sol-j)          | See |<Plug>(easymotion-sol-j)|     +
    <Plug>(easymotion-sol-k)          | See |<Plug>(easymotion-sol-k)|     +
    <Plug>(easymotion-eol-j)          | See |<Plug>(easymotion-eol-j)|     +
    <Plug>(easymotion-eol-k)          | See |<Plug>(easymotion-eol-k)|     +
    <Plug>(easymotion-iskeyword-w)    | See |<Plug>(easymotion-iskeyword-w)|
    <Plug>(easymotion-iskeyword-b)    | See |<Plug>(easymotion-iskeyword-b)|
    <Plug>(easymotion-iskeyword-bd-w) | See |<Plug>(easymotion-iskeyword-bd-w)|
    <Plug>(easymotion-iskeyword-e)    | See |<Plug>(easymotion-iskeyword-e)|
    <Plug>(easymotion-iskeyword-ge)   | See |<Plug>(easymotion-iskeyword-ge)|
    <Plug>(easymotion-iskeyword-bd-e) | See |<Plug>(easymotion-iskeyword-bd-e)|
    <Plug>(easymotion-vim-n)          | See |<Plug>(easymotion-vim-n)|
    <Plug>(easymotion-vim-N)          | See |<Plug>(easymotion-vim-N)|
                                      |
    Within Line Motion                | See |easymotion-within-line|
    ----------------------------------|---------------------------------
    <Plug>(easymotion-sl)             | See |<Plug>(easymotion-sl)|
    <Plug>(easymotion-fl)             | See |<Plug>(easymotion-fl)|
    <Plug>(easymotion-Fl)             | See |<Plug>(easymotion-Fl)|
    <Plug>(easymotion-bd-fl)          | See |<Plug>(easymotion-sl)|
    <Plug>(easymotion-tl)             | See |<Plug>(easymotion-tl)|
    <Plug>(easymotion-Tl)             | See |<Plug>(easymotion-Tl)|
    <Plug>(easymotion-bd-tl)          | See |<Plug>(easymotion-bd-tl)|
    <Plug>(easymotion-wl)             | See |<Plug>(easymotion-wl)|
    <Plug>(easymotion-bl)             | See |<Plug>(easymotion-bl)|
    <Plug>(easymotion-bd-wl)          | See |<Plug>(easymotion-bd-wl)|
    <Plug>(easymotion-el)             | See |<Plug>(easymotion-el)|
    <Plug>(easymotion-gel)            | See |<Plug>(easymotion-gel)|
    <Plug>(easymotion-bd-el)          | See |<Plug>(easymotion-bd-el)|
    <Plug>(easymotion-lineforward)    | See |<Plug>(easymotion-lineforward)|
    <Plug>(easymotion-linebackward)   | See |<Plug>(easymotion-linebackward)|
    <Plug>(easymotion-lineanywhere)   | See |<Plug>(easymotion-lineanywhere)|
                                      |
    Multi Input Find Motion           | See |easymotion-multi-input|
    ----------------------------------|---------------------------------
    <Plug>(easymotion-s2)             | See |<Plug>(easymotion-s2)|   +  mapped to sn
    <Plug>(easymotion-f2)             | See |<Plug>(easymotion-f2)|   +  mapped to fn
    <Plug>(easymotion-F2)             | See |<Plug>(easymotion-F2)|   +  mapped to Fn
    <Plug>(easymotion-bd-f2)          | See |<Plug>(easymotion-s2)|   +  mapped to sn
    <Plug>(easymotion-t2)             | See |<Plug>(easymotion-t2)|
    <Plug>(easymotion-T2)             | See |<Plug>(easymotion-T2)|
    <Plug>(easymotion-bd-t2)          | See |<Plug>(easymotion-bd-t2)|
                                      |
    <Plug>(easymotion-sl2)            | See |<Plug>(easymotion-sl2)|
    <Plug>(easymotion-fl2)            | See |<Plug>(easymotion-fl2)|
    <Plug>(easymotion-Fl2)            | See |<Plug>(easymotion-Fl2)|
    <Plug>(easymotion-tl2)            | See |<Plug>(easymotion-tl2)|
    <Plug>(easymotion-Tl2)            | See |<Plug>(easymotion-Tl2)|
                                      |
    <Plug>(easymotion-sn)             | See |<Plug>(easymotion-sn)|   +
    <Plug>(easymotion-fn)             | See |<Plug>(easymotion-fn)|   +
    <Plug>(easymotion-Fn)             | See |<Plug>(easymotion-Fn)|   +
    <Plug>(easymotion-bd-fn)          | See |<Plug>(easymotion-sn)|   +
    <Plug>(easymotion-tn)             | See |<Plug>(easymotion-tn)|
    <Plug>(easymotion-Tn)             | See |<Plug>(easymotion-Tn)|
    <Plug>(easymotion-bd-tn)          | See |<Plug>(easymotion-bd-tn)|
                                      |
    <Plug>(easymotion-sln)            | See |<Plug>(easymotion-sln)|
    <Plug>(easymotion-fln)            | See |<Plug>(easymotion-fln)|
    <Plug>(easymotion-Fln)            | See |<Plug>(easymotion-Fln)|
    <Plug>(easymotion-bd-fln)         | See |<Plug>(easymotion-sln)|
    <Plug>(easymotion-tln)            | See |<Plug>(easymotion-tln)|
    <Plug>(easymotion-Tln)            | See |<Plug>(easymotion-Tln)|
    <Plug>(easymotion-bd-tln)         | See |<Plug>(easymotion-bd-tln)|

    Over Window Motion                | (No assignment by default)
    ----------------------------------|---------------------------------
    <Plug>(easymotion-overwin-f)      | See |<Plug>(easymotion-overwin-f)|
    <Plug>(easymotion-overwin-f2)     | See |<Plug>(easymotion-overwin-f2)|
    <Plug>(easymotion-overwin-line)   | See |<Plug>(easymotion-overwin-line)|
    <Plug>(easymotion-overwin-w)      | See |<Plug>(easymotion-overwin-w)|
 */
