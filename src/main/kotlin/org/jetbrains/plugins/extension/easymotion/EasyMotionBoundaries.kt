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

import com.intellij.openapi.editor.Editor
import org.acejump.boundaries.Boundaries
import org.acejump.boundaries.EditorOffsetCache

object FullLineBoundary : Boundaries {
    override fun getOffsetRange(editor: Editor, cache: EditorOffsetCache): IntRange {
        val currentLine = editor.caretModel.logicalPosition.line
        val lineStartOffset = editor.document.getLineStartOffset(currentLine)
        val lineEndOffset = editor.document.getLineEndOffset(currentLine)
        return lineStartOffset..lineEndOffset
    }

    override fun isOffsetInside(editor: Editor, offset: Int, cache: EditorOffsetCache): Boolean {
        return offset in getOffsetRange(editor, cache)
    }
}

object AfterCaretLineBoundary : Boundaries {
    override fun getOffsetRange(editor: Editor, cache: EditorOffsetCache): IntRange {
        val currentLine = editor.caretModel.logicalPosition.line
        val startOffset = editor.caretModel.offset
        val lineEndOffset = editor.document.getLineEndOffset(currentLine)
        return startOffset..lineEndOffset
    }

    override fun isOffsetInside(editor: Editor, offset: Int, cache: EditorOffsetCache): Boolean {
        return offset in getOffsetRange(editor, cache)
    }
}

object BeforeCaretLineBoundary : Boundaries {
    override fun getOffsetRange(editor: Editor, cache: EditorOffsetCache): IntRange {
        val currentLine = editor.caretModel.logicalPosition.line
        val lineStartOffset = editor.document.getLineStartOffset(currentLine)
        val endOffset = editor.caretModel.offset
        return lineStartOffset..endOffset
    }

    override fun isOffsetInside(editor: Editor, offset: Int, cache: EditorOffsetCache): Boolean {
        return offset in getOffsetRange(editor, cache)
    }
}
