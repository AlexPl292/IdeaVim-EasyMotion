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

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.KeyboardShortcut
import com.intellij.openapi.keymap.KeymapUtil
import com.intellij.testFramework.PlatformTestUtil
import com.maddyhome.idea.vim.VimPlugin
import junit.framework.TestCase
import org.acejump.session.SessionManager
import java.awt.event.InputEvent
import java.awt.event.KeyEvent

class AceJumpSaveJumpListTest : EasyMotionTestCase() {
    fun `test acejump saves jumps to jumplist`() {
        setupEditor(text)

        jumpTo("found")
        jumpTo("rocks")

        val jumps = VimPlugin.getMark().jumps

        TestCase.assertEquals(2, jumps.size)

        TestCase.assertEquals(0, jumps[0].col)
        TestCase.assertEquals(0, jumps[0].logicalLine)

        TestCase.assertEquals(2, jumps[1].col)
        TestCase.assertEquals(2, jumps[1].logicalLine)
    }

    fun `test acejump saves jumps with cancel to jumplist`() {
        setupEditor(text)

        jumpTo("found")
        jumpCancel("legendary")
        jumpTo("rocks")
        jumpTo("settled")

        val jumps = VimPlugin.getMark().jumps

        TestCase.assertEquals(3, jumps.size)

        TestCase.assertEquals(0, jumps[0].col)
        TestCase.assertEquals(0, jumps[0].logicalLine)

        TestCase.assertEquals(2, jumps[1].col)
        TestCase.assertEquals(2, jumps[1].logicalLine)

        TestCase.assertEquals(4, jumps[2].col)
        TestCase.assertEquals(3, jumps[2].logicalLine)
    }

    private fun jumpTo(word: String) {
        val actionManager = ActionManager.getInstance()
        val action = actionManager.getAction("AceAction")!!
        actionManager.tryToExecute(action, getInputEvent(), null, null, true)

        val session = SessionManager[myFixture.editor]!!

        myFixture.type(word)
        PlatformTestUtil.dispatchAllEventsInIdeEventQueue()
        val locations = session.tags
        if (locations.isNotEmpty()) {
            val tag = locations[0].key
            if (tag != null) {
                myFixture.type(tag)
            }
        }
    }

    private fun jumpCancel(word: String) {
        val actionManager = ActionManager.getInstance()
        val action = actionManager.getAction("AceAction")!!
        actionManager.tryToExecute(action, getInputEvent(), null, null, true)

        val session = SessionManager[myFixture.editor]!!


        myFixture.type(word)
        PlatformTestUtil.dispatchAllEventsInIdeEventQueue()
        val locations = session.tags
        if (locations.isNotEmpty()) {
            myFixture.performEditorAction("EditorEscape")
        }
    }

    private fun getInputEvent(): InputEvent {
        val shortcuts = KeymapUtil.getActiveKeymapShortcuts("AceAction").shortcuts
        val keyStroke = shortcuts.filterIsInstance<KeyboardShortcut>().first().firstKeyStroke

        return KeyEvent(
            myFixture.editor.component,
            KeyEvent.KEY_PRESSED,
            System.currentTimeMillis(),
            keyStroke.modifiers,
            keyStroke.keyCode,
            keyStroke.keyChar,
            KeyEvent.KEY_LOCATION_STANDARD
        )
    }
}