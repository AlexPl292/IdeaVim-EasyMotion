/*
 * IdeaVim-EasyMotion. Easymotion emulator plugin for IdeaVim.
 * Copyright (C) 2019-2022  Alex Plate
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.jetbrains.plugins.extension.easymotion

import com.intellij.openapi.application.ApplicationManager
import com.maddyhome.idea.vim.api.injector
import com.maddyhome.idea.vim.command.MappingMode
import com.maddyhome.idea.vim.ex.vimscript.VimScriptGlobalEnvironment
import com.maddyhome.idea.vim.extension.VimExtensionFacade
import com.maddyhome.idea.vim.options.helpers.KeywordOptionHelper
import org.jetbrains.plugins.extension.easymotion.EasyMotionExtension.Companion.doMapping
import org.jetbrains.plugins.extension.easymotion.EasyMotionExtension.Companion.pluginPrefix

/** Map some <Plug>(easymotion-[keys]) command to given handler */
fun mapToFunction(keys: String, handler: HandlerProcessor) {
    VimExtensionFacade.putExtensionHandlerMapping(
        MappingMode.NVO,
        injector.parser.parseKeys(command(keys)),
        EasyMotionExtension.mappingOwner,
        getHandler(handler),
        false
    )
}

fun getHandler(handler: HandlerProcessor): EasyHandlerBase {
    return if (ApplicationManager.getApplication().isUnitTestMode) {
        TestObject.TestHandler(handler)
    } else {
        StandardHandler(handler)
    }
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
        VimExtensionFacade.putKeyMappingIfMissing(
            MappingMode.NVO,
            injector.parser.parseKeys("${pluginPrefix}$keys"),
            EasyMotionExtension.mappingOwner,
            injector.parser.parseKeys(command(keys)),
            true
        )
    }
}

fun command(keys: String) = "<Plug>(easymotion-$keys)"

/** Create regex representation of isKeyword option */
fun keywordRegex(): String? {
    val regex = KeywordOptionHelper.toRegex()
    if (regex.isEmpty()) return null
    return regex.joinToString("") { if (it.startsWith("[")) it.substring(1, it.lastIndex) else it }
}
