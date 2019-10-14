package org.jetbrains.plugins

import com.intellij.openapi.application.ApplicationManager
import com.maddyhome.idea.vim.command.MappingMode
import com.maddyhome.idea.vim.ex.vimscript.VimScriptGlobalEnvironment
import com.maddyhome.idea.vim.extension.VimExtensionFacade
import com.maddyhome.idea.vim.helper.StringHelper
import com.maddyhome.idea.vim.option.OptionsManager
import org.jetbrains.plugins.EasyMotionExtension.Companion.doMapping
import org.jetbrains.plugins.EasyMotionExtension.Companion.pluginPrefix

/** Map some <Plug>(easymotion-[keys]) command to given handler */
fun mapToFunction(keys: String, handler: HandlerProcessor) {
    VimExtensionFacade.putExtensionHandlerMapping(
        MappingMode.NVO,
        StringHelper.parseKeys(command(keys)),
        if (ApplicationManager.getApplication().isUnitTestMode) {
            TestObject.TestHandler(handler)
        } else {
            StandardHandler(handler)
        },
        false
    )
}

/**
 * Map some <Plug>(easymotion-[keys]) command to given handler
 *  and create mapping to <Plug>(easymotion-prefix)[keys]
 *
 *  The mapping will not be created if g:EasyMotion_do_mapping variable is 0
 */
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

/** Create regex representation of isKeyword option */
fun keywordRegex(): String? {
    val regex = OptionsManager.iskeyword.toRegex()
    if (regex.isEmpty()) return null
    return regex.joinToString("") { if (it.startsWith("[")) it.substring(1, it.lastIndex) else it }
}
