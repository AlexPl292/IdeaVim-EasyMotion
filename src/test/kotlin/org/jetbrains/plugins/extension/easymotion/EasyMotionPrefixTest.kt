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

import com.maddyhome.idea.vim.VimPlugin
import com.maddyhome.idea.vim.api.injector
import com.maddyhome.idea.vim.command.MappingMode
import com.maddyhome.idea.vim.ex.vimscript.VimScriptGlobalEnvironment
import com.maddyhome.idea.vim.helper.StringHelper
import com.maddyhome.idea.vim.helper.StringHelper.parseKeys
import com.maddyhome.idea.vim.key.MappingOwner
import com.maddyhome.idea.vim.option.OptionsManager
import com.maddyhome.idea.vim.option.ToggleOption
import com.maddyhome.idea.vim.options.OptionScope
import com.maddyhome.idea.vim.vimscript.model.datatypes.VimInt

class EasyMotionPrefixTest : EasyMotionTestCase() {

    override fun tearDown() {
        injector.optionService.setOptionValue(OptionScope.GLOBAL, "easymotion", VimInt(0))
        super.tearDown()
    }

    fun `test create prefix`() {
        setupEditor()
        injector.optionService.setOptionValue(OptionScope.GLOBAL, "easymotion", VimInt(1))
        val mapping = VimPlugin.getKey().getKeyMappingByOwner(EasyMotionExtension.mappingOwner)
        val prefixExists = VimPlugin.getKey().getMapTo(MappingMode.NORMAL, parseKeys(EasyMotionExtension.pluginPrefix))
        assertTrue(prefixExists.isNotEmpty())
    }

    fun `test do not create prefix`() {
        setupEditor()
        VimScriptGlobalEnvironment.getInstance().variables[EasyMotionExtension.doMapping] = 0
        injector.optionService.setOptionValue(OptionScope.GLOBAL, "easymotion", VimInt(1))
        val mapping = VimPlugin.getKey().getKeyMappingByOwner(EasyMotionExtension.mappingOwner)
        val prefixExists = mapping.filter { it.first.contains(StringHelper.parseKeys("\\").first()) }.any()
        assertFalse(prefixExists)
    }

    fun `test remap prefix`() {
        setupEditor()
        VimPlugin.getKey()
            .putKeyMapping(MappingMode.NVO, parseKeys(",s"), MappingOwner.IdeaVim.Other, parseKeys(command("s")), true)

        injector.optionService.setOptionValue(OptionScope.GLOBAL, "easymotion", VimInt(1))

        val mapping = VimPlugin.getKey().getMapTo(MappingMode.NORMAL, parseKeys(command("s")))
        assertEquals(1, mapping.size)
    }
}