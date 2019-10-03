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

    override fun initOnce() {
        putExtensionHandlerMapping(MappingMode.NO, parseKeys("<Plug>I"), Move(), false)

        putKeyMapping(MappingMode.NO, parseKeys("I"), parseKeys("<Plug>I"), true)
    }

    private class Move : VimExtensionHandler {
        override fun execute(editor: Editor, context: DataContext) {
            val systemQueue = Toolkit.getDefaultToolkit().systemEventQueue
            val loop = systemQueue.createSecondaryLoop()
            val listener = object : Handler.AceJumpListener {
                override fun finished() {
                    Handler.removeAceJumpListener(this)
                    loop.exit()
                }
            }
            Handler.addAceJumpListener(listener)

            Handler.activate()
            loop.enter()
        }
    }
}
