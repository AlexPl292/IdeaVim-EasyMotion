package org.jetbrains

import com.maddyhome.idea.vim.command.MappingMode
import com.maddyhome.idea.vim.extension.VimExtensionFacade
import com.maddyhome.idea.vim.extension.VimExtensionHandler
import com.maddyhome.idea.vim.helper.StringHelper
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty


fun putAceMapping(keys: String, handler: VimExtensionHandler) {
    VimExtensionFacade.putExtensionHandlerMapping(MappingMode.NVO, StringHelper.parseKeys(keys), handler, false)
}

fun command(keys: String) = "<Plug>(easymotion-$keys)"

fun command() = object : ReadOnlyProperty<Any?, String> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): String = command(property.name.replace('_', '-'))
}
