import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;

// Like RunCellAction, but moves to next cell afterwards
public class RunCellMoveNextAction extends RunCellAction {
    static private String myText = "Run Cell And Move Next";

    public RunCellMoveNextAction() {
        super(myText, -1);
    }

    public RunCellMoveNextAction(int lineNumber) {
        super(myText, lineNumber);
    }

    @Override
    protected void postExecuteHook(Editor editor, Block block) {
        moveCaretToLineStart(editor, block.lineEnd + 1, prefs.getDelimiterInsert());
    }

    private static void moveCaretToLineStart(Editor editor, int line, String delimiterInsert) {
        if (line < 0)
            return;
        Document doc = editor.getDocument();
        if (line != 0 && line < doc.getLineCount()) {
            int newOffset = doc.getLineStartOffset(line);
            editor.getCaretModel().moveToOffset(newOffset);
            editor.getScrollingModel().scrollToCaret(ScrollType.RELATIVE);
        } else {
            WriteCommandAction.runWriteCommandAction(editor.getProject(), () -> {
                int offset = doc.getLineEndOffset(doc.getLineCount() - 1);
                doc.insertString(offset, "\n" + delimiterInsert + "\n\n");

                int newOffset = doc.getLineStartOffset(doc.getLineCount() - 2);
                editor.getCaretModel().moveToOffset(newOffset);
            });
        }
    }
}
