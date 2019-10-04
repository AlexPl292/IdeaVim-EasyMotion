@file:Suppress("PrivatePropertyName")

package org.jetbrains

import com.maddyhome.idea.vim.command.MappingMode
import com.maddyhome.idea.vim.extension.VimExtensionFacade.putKeyMapping
import com.maddyhome.idea.vim.extension.VimNonDisposableExtension
import com.maddyhome.idea.vim.helper.StringHelper.parseKeys
import org.acejump.control.Handler
import org.acejump.label.Pattern
import org.acejump.view.Boundary

class AceExtension : VimNonDisposableExtension() {
    override fun getName(): String = "acejump"

    private val prefix = "<Plug>(easymotion-prefix)"

    private val s by command()
    private val j by command()
    private val k by command()
    private val w by command()
    private val b by command()

    override fun initOnce() {
        putAceMapping(s, BidirectionalMultiInput())
        putAceMapping(command("s2"), BidirectionalMultiInput())
        putAceMapping(command("sn"), BidirectionalMultiInput())
        putAceMapping(command("bd-f2"), BidirectionalMultiInput())
        putAceMapping(command("bd-fn"), BidirectionalMultiInput())
        putAceMapping(command("bd-jk"), BidirectionalLine(Pattern.CODE_INDENTS, Boundary.FULL_FILE_BOUNDARY))
        putAceMapping(j, BidirectionalLine(Pattern.CODE_INDENTS, Boundary.AFTER_CARET_BOUNDARY))
        putAceMapping(k, BidirectionalLine(Pattern.CODE_INDENTS, Boundary.BEFORE_CARET_BOUNDARY))
        putAceMapping(command("eol-j"), BidirectionalLine(Pattern.END_OF_LINE, Boundary.AFTER_CARET_BOUNDARY))
        putAceMapping(command("eol-k"), BidirectionalLine(Pattern.END_OF_LINE, Boundary.BEFORE_CARET_BOUNDARY))
        putAceMapping(command("sol-j"), BidirectionalLine(Pattern.START_OF_LINE, Boundary.AFTER_CARET_BOUNDARY))
        putAceMapping(command("sol-k"), BidirectionalLine(Pattern.START_OF_LINE, Boundary.BEFORE_CARET_BOUNDARY))
        putAceMapping(w, BidirectionalLine(Pattern.ALL_WORDS, Boundary.AFTER_CARET_BOUNDARY))
        putAceMapping(b, BidirectionalLine(Pattern.ALL_WORDS, Boundary.BEFORE_CARET_BOUNDARY))
        putAceMapping(command("bd-w"), BidirectionalLine(Pattern.ALL_WORDS, Boundary.SCREEN_BOUNDARY))

        putKeyMapping(MappingMode.NVO, parseKeys("${prefix}s"), parseKeys(s), true)
        putKeyMapping(MappingMode.NVO, parseKeys("${prefix}j"), parseKeys(j), true)
        putKeyMapping(MappingMode.NVO, parseKeys("${prefix}k"), parseKeys(k), true)
        putKeyMapping(MappingMode.NVO, parseKeys("${prefix}w"), parseKeys(w), true)
        putKeyMapping(MappingMode.NVO, parseKeys("${prefix}b"), parseKeys(b), true)

        putKeyMapping(MappingMode.NVO, parseKeys("<leader><leader>"), parseKeys(prefix), true)
    }

    private class BidirectionalMultiInput : HandlerProcessor

    private class BidirectionalLine(val pattern: Pattern, val bounds: Boundary) : HandlerProcessor {
        override fun customization() {
            Handler.regexSearch(pattern, bounds)
        }
    }
}

/*

    <Plug> Mapping Table | Default
    ---------------------|----------------------------------------------
    <Plug>(easymotion-f) | <Leader>f{char}
    <Plug>(easymotion-F) | <Leader>F{char}
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
    <Plug>(easymotion-s2)             | See |<Plug>(easymotion-s2)|   +
    <Plug>(easymotion-f2)             | See |<Plug>(easymotion-f2)|
    <Plug>(easymotion-F2)             | See |<Plug>(easymotion-F2)|
    <Plug>(easymotion-bd-f2)          | See |<Plug>(easymotion-s2)|   +
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
    <Plug>(easymotion-fn)             | See |<Plug>(easymotion-fn)|
    <Plug>(easymotion-Fn)             | See |<Plug>(easymotion-Fn)|
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
