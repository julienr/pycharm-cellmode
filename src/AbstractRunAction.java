import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractRunAction extends AnAction {
    public static class Block {
        public final String content;
        public final int lineStart;
        public final int lineEnd;

        public Block(String content, int lineStart, int lineEnd) {
            this.content = content;
            this.lineStart = lineStart;
            this.lineEnd = lineEnd;
        }
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        //System.out.println("RunCellAction");
        Editor editor = CommonDataKeys.EDITOR.getData(e.getDataContext());
        Block block = findBlock(editor);
        if (block != null) {
            if (prefs.getTargetConsole() == Preferences.TARGET_INTERNAL_CONSOLE) {
                PythonConsoleUtils.execute(e, block.content);
            } else {
                Tmux.executeInTmux(prefs, block.content);
            }
            postExecuteHook(editor, block);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        // Always visible
        Presentation presentation = e.getPresentation();
        presentation.setEnabled(true);
        presentation.setVisible(true);
    }

    protected final Preferences prefs = new Preferences();

    protected void postExecuteHook(Editor editor, Block block) {}

    protected abstract Block findBlock(Editor editor);

    /**
     * Search for ## in the direction given (1 to search down, -1 to search up)
     * @param startLine the line where to start the search
     * @return the line on which ## was found or -1 if none is found
     */
    protected static int searchForDoubleHash(Document document, int startLine, int direction) {
        int lineCount = document.getLineCount();
        CharSequence text = document.getCharsSequence();
        for (int line = startLine; line >= 0 && line < lineCount; line += direction) {
            int start = document.getLineStartOffset(line);
            int end = document.getLineEndOffset(line);
            if (end - start < 2) {
                continue;
            }

            if (startsWithDoubleHash(text, start, end)) {
                return line;
            }
        }
        return -1;
    }

    // Check if the first two non-space characters in the subseq delimited by (start, end) are ##
    protected static boolean startsWithDoubleHash(CharSequence seq, int start, int end) {
        for (int ci = start; ci < end; ++ci) {
            if (!Character.isWhitespace(seq.charAt(ci))) {
                if (ci < end - 1) {
                    return seq.charAt(ci) == '#' && seq.charAt(ci + 1) == '#';
                } else {
                    return false;
                }
            }
        }
        return false;
    }
}
