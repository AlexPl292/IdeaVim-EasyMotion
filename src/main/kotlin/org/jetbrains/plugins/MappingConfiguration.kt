package org.jetbrains.plugins

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.KeyboardShortcut
import com.maddyhome.idea.vim.VimPlugin
import com.maddyhome.idea.vim.command.MappingMode
import com.maddyhome.idea.vim.extension.VimExtensionFacade
import com.maddyhome.idea.vim.helper.StringHelper
import com.maddyhome.idea.vim.key.ShortcutOwner

object MappingConfigurator {
    private val aceJumpAlternatives = mapOf(
        "AceAction" to "sn",
        "AceLineAction" to "linemarks",
        "AceWordAction" to "bd-w",
        "AceWordForwardAction" to "w",
        "AceWordBackwardsAction" to "b"
    )

    fun configureMappings() {
        for ((actionId, alternative) in aceJumpAlternatives) {
            val action = ActionManager.getInstance().getAction(actionId) ?: continue
            val shortcuts = action.shortcutSet.shortcuts
            for (shortcut in shortcuts) {
                if (!shortcut.isKeyboard || shortcut !is KeyboardShortcut) continue
                val keyStroke = shortcut.firstKeyStroke

                VimExtensionFacade.putKeyMapping(
                    MappingMode.NVO,
                    listOf(keyStroke),
                    StringHelper.parseKeys(command(alternative)),
                    true
                )
                VimPlugin.getKey().savedShortcutConflicts[keyStroke] = ShortcutOwner.VIM
            }
        }
    }
}
