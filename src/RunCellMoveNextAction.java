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
        moveCaretToLineStart(editor, block.lineEnd + 1);
    }

    private static void moveCaretToLineStart(Editor editor, int line) {
        if (line == 0)
            return ;
        Document doc = editor.getDocument();
        if (line < doc.getLineCount()) {
            int newOffset = doc.getLineStartOffset(line);
            editor.getCaretModel().moveToOffset(newOffset);
            editor.getScrollingModel().scrollToCaret(ScrollType.RELATIVE);
        }
    }
}
