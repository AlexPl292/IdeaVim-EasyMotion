package org.jetbrains

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.maddyhome.idea.vim.command.MappingMode
import com.maddyhome.idea.vim.extension.VimExtensionFacade.putExtensionHandlerMapping
import com.maddyhome.idea.vim.extension.VimExtensionFacade.putKeyMapping
import com.maddyhome.idea.vim.extension.VimExtensionHandler
import com.maddyhome.idea.vim.extension.VimNonDisposableExtension
import com.maddyhome.idea.vim.helper.StringHelper.parseKeys
import org.acejump.control.Handler
import java.awt.Toolkit

class AceExtension : VimNonDisposableExtension() {
    override fun getName(): String = "acejump"

    private val prefix = "<Plug>(easymotion-prefix)"

    private val sn = "<Plug>(easymotion-sn)"

    override fun initOnce() {
        putExtensionHandlerMapping(MappingMode.NVO, parseKeys(sn), BidirectionalMultiInput(), false)

        putKeyMapping(MappingMode.NVO, parseKeys("${prefix}s"), parseKeys(sn), true)

        putKeyMapping(MappingMode.NVO, parseKeys("<leader><leader>"), parseKeys(prefix), true)
    }

    private class BidirectionalMultiInput : VimExtensionHandler {
        override fun execute(editor: Editor, context: DataContext) {
            val systemQueue = Toolkit.getDefaultToolkit().systemEventQueue
            val loop = systemQueue.createSecondaryLoop()

            Handler.addAceJumpListener(object : Handler.AceJumpListener {
                override fun finished() {
                    Handler.removeAceJumpListener(this)
                    loop.exit()
                }
            })

            Handler.activate()
            loop.enter()
        }
    }
}
