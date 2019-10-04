package org.jetbrains

import com.maddyhome.idea.vim.command.MappingMode
import com.maddyhome.idea.vim.extension.VimExtensionFacade
import com.maddyhome.idea.vim.helper.StringHelper
import org.jetbrains.AceExtension.Companion.prefix

fun mapToFunction(keys: String, handler: HandlerProcessor) {
    VimExtensionFacade.putExtensionHandlerMapping(
        MappingMode.NVO,
        StringHelper.parseKeys(command(keys)),
        makeHandler(handler),
        false
    )
}

fun mapToFunctionAndProvideKeys(keys: String, handler: HandlerProcessor) {
    mapToFunction(keys, handler)
    VimExtensionFacade.putKeyMapping(
        MappingMode.NVO,
        StringHelper.parseKeys("${prefix}$keys"),
        StringHelper.parseKeys(command(keys)),
        true
    )
}

fun command(keys: String) = "<Plug>(easymotion-$keys)"
