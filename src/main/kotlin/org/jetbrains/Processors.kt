package org.jetbrains

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.maddyhome.idea.vim.extension.VimExtensionHandler
import org.acejump.control.Handler
import java.awt.Toolkit

interface HandlerProcessor {
    fun customization() {}
    fun onFinish() {}
}

fun makeHandler(processor: HandlerProcessor): VimExtensionHandler {
    return if (ApplicationManager.getApplication().isUnitTestMode) {
        TestProcessor.TestHandler(processor)
    } else {
        StandardHandler(processor)
    }
}

class StandardHandler(private val processor: HandlerProcessor) : VimExtensionHandler {
    override fun execute(editor: Editor, context: DataContext) {
        val systemQueue = Toolkit.getDefaultToolkit().systemEventQueue
        val loop = systemQueue.createSecondaryLoop()

        Handler.addAceJumpListener(object : Handler.AceJumpListener {
            override fun finished() {
                processor.onFinish()
                Handler.removeAceJumpListener(this)
                loop.exit()
            }
        })

        Handler.activate()
        processor.customization()
        loop.enter()
    }
}

object TestProcessor {
    var handler: (processor: HandlerProcessor, editor: Editor, context: DataContext) -> Unit = { _, _, _ -> }

    class TestHandler(private val processor: HandlerProcessor) : VimExtensionHandler {
        override fun execute(editor: Editor, context: DataContext) {
            Handler.activate()
            processor.customization()
            handler(processor, editor, context)
            processor.onFinish()
        }
    }
}
