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

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.ex.AnActionListener
import com.maddyhome.idea.vim.VimPlugin
import org.acejump.session.AceJumpListener
import org.acejump.session.SessionManager

class EasyMotionActionListener : AnActionListener {
    override fun beforeActionPerformed(action: AnAction, dataContext: DataContext, event: AnActionEvent) {
        val editor = dataContext.getData(CommonDataKeys.EDITOR) ?: return
        val actionId = ActionManager.getInstance().getId(action)
        if (actionId !in MappingConfigurator.aceJumpAlternatives.keys) return

        // Add position to jump list
        VimPlugin.getMark().saveJumpLocation(editor)
        val offsetBeforeJump = editor.caretModel.offset

        SessionManager.get(editor)?.let { session ->
            session.addAceJumpListener(object : AceJumpListener {
                override fun finished() {
                    // Remove position from jumps list if caret haven't moved
                    if (offsetBeforeJump == editor.caretModel.offset) {
                        VimPlugin.getMark().jumps.dropLast(1)
                    }

                    session.removeAceJumpListener(this)
                }
            })
        }
    }
}