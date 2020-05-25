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

import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.editor.actionSystem.EditorActionManager
import com.maddyhome.idea.vim.VimPlugin
import com.maddyhome.idea.vim.option.OptionsManager
import com.maddyhome.idea.vim.option.ToggleOption
import junit.framework.Assert
import junit.framework.TestCase

class EasyMotionJumpTest : EasyMotionTestCase() {

    override fun setUp() {
        super.setUp()
        (OptionsManager.getOption("easymotion") as ToggleOption).set()
    }

    fun `test jumps`() {
        doTest(
            command = parseKeysWithLeader("s"),
            searchQuery = "found",
            jumpToNthQuery = 0
        )
        doTest(
            command = parseKeysWithLeader("s"),
            searchQuery = "rocks",
            jumpToNthQuery = 0
        )
        val jumps = VimPlugin.getMark().jumps

        TestCase.assertEquals(2, jumps.size)

        TestCase.assertEquals(0, jumps[0].col)
        TestCase.assertEquals(0, jumps[0].logicalLine)

        TestCase.assertEquals(2, jumps[1].col)
        TestCase.assertEquals(2, jumps[1].logicalLine)
    }

    fun `test jump with skipping`() {
        doTest(
            command = parseKeysWithLeader("s"),
            searchQuery = "found",
            jumpToNthQuery = 0
        )

        // Cancel jump
        doTest(
            command = parseKeysWithLeader("s<ESC>"),
            searchQuery = "legendary"
        )

        doTest(
            command = parseKeysWithLeader("s"),
            searchQuery = "rocks",
            jumpToNthQuery = 0
        )

        doTest(
            command = parseKeysWithLeader("s"),
            searchQuery = "settled",
            jumpToNthQuery = 0
        )

        val jumps = VimPlugin.getMark().jumps

        TestCase.assertEquals(3, jumps.size)

        TestCase.assertEquals(0, jumps[0].col)
        TestCase.assertEquals(0, jumps[0].logicalLine)

        TestCase.assertEquals(2, jumps[1].col)
        TestCase.assertEquals(2, jumps[1].logicalLine)

        TestCase.assertEquals(4, jumps[2].col)
        TestCase.assertEquals(3, jumps[2].logicalLine)
    }

    // TODO: 25.05.2020 These tests should be enabled when there would be a possibility to send a key event to queue
/*
    fun `test cancel search by esc`() {
        doTest(
            command = parseKeysWithLeader("s<ESC>"),
            afterEditorSetup = {
                Assert.assertFalse(enabled())
                Assert.assertFalse(ResetAction.registered(it))
            }
        )
        Assert.assertFalse(enabled())
        Assert.assertFalse(ResetAction.registered(myFixture.editor))
    }

    fun `test cancel search by c-bracket`() {
        doTest(
            command = parseKeysWithLeader("s<C-[>"),
            afterEditorSetup = {
                Assert.assertFalse(enabled())
                Assert.assertFalse(ResetAction.registered(it))
            }
        )
        Assert.assertFalse(enabled())
        Assert.assertFalse(ResetAction.registered(myFixture.editor))
    }

    fun `test cancel search by c-c`() {
        doTest(
            command = parseKeysWithLeader("s<C-c>"),
            afterEditorSetup = {
                Assert.assertFalse(enabled())
                Assert.assertFalse(ResetAction.registered(it))
            }
        )
        Assert.assertFalse(enabled())
        Assert.assertFalse(ResetAction.registered(myFixture.editor))
    }

    private fun enabled(): Boolean {
        return "acejump" in EditorActionManager.getInstance().getActionHandler(IdeActions.ACTION_EDITOR_ESCAPE)
            .toString()
    }
*/
}