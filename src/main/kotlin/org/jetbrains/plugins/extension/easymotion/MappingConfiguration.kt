/*
 * IdeaVim-EasyMotion. Easymotion emulator plugin for IdeaVim.
 * Copyright (C) 2019-2020  Alex Plate
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

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.KeyboardShortcut
import com.intellij.openapi.application.ApplicationManager
import com.maddyhome.idea.vim.VimPlugin
import com.maddyhome.idea.vim.command.MappingMode
import com.maddyhome.idea.vim.extension.VimExtensionFacade
import com.maddyhome.idea.vim.helper.StringHelper
import com.maddyhome.idea.vim.key.ShortcutOwner

object MappingConfigurator {
    val aceJumpAlternatives = mapOf(
        "AceAction" to "sn",
        "AceLineAction" to "linemarks",
        "AceWordAction" to "bd-w",
        "AceWordForwardAction" to "w",
        "AceWordBackwardsAction" to "b"
    )

    fun configureMappings() {
        ApplicationManager.getApplication().invokeLater {
            for ((actionId, alternative) in aceJumpAlternatives) {
                val action = ActionManager.getInstance().getAction(actionId) ?: continue
                val shortcuts = action.shortcutSet.shortcuts
                for (shortcut in shortcuts) {
                    if (!shortcut.isKeyboard || shortcut !is KeyboardShortcut) continue
                    val keyStroke = shortcut.firstKeyStroke

                    VimExtensionFacade.putKeyMapping(
                        MappingMode.NVO,
                        listOf(keyStroke),
                        EasyMotionExtension.mappingOwner,
                        StringHelper.parseKeys(command(alternative)),
                        true
                    )
                    VimPlugin.getKey().savedShortcutConflicts[keyStroke] = ShortcutOwner.VIM
                }
            }
        }
    }
}
