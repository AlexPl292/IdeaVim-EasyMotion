package org.jetbrains

import com.maddyhome.idea.vim.command.MappingMode
import com.maddyhome.idea.vim.ex.vimscript.VimScriptGlobalEnvironment
import com.maddyhome.idea.vim.extension.VimExtensionFacade
import com.maddyhome.idea.vim.helper.StringHelper
import com.maddyhome.idea.vim.option.OptionsManager
import org.jetbrains.AceExtension.Companion.doMapping
import org.jetbrains.AceExtension.Companion.pluginPrefix

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
    if (VimScriptGlobalEnvironment.getInstance().variables[doMapping] == 1) {
        VimExtensionFacade.putKeyMapping(
            MappingMode.NVO,
            StringHelper.parseKeys("${pluginPrefix}$keys"),
            StringHelper.parseKeys(command(keys)),
            true
        )
    }
}

fun command(keys: String) = "<Plug>(easymotion-$keys)"

fun keywordRegex(): String? {
    val regex = OptionsManager.iskeyword.toRegex()
    if (regex.isEmpty()) return null
    return regex.joinToString("") { if (it.startsWith("[")) it.substring(1, it.lastIndex) else it }
}
