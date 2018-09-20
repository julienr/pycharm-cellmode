import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;

// Runs the current line under the caret
public class RunLineAction extends AbstractRunAction {
    static private String myText = "Run Line";

    RunLineAction() {
        super(myText);
    }

    @Override
    protected Block findBlock(Editor editor) {
        Document document = editor.getDocument();
        int docCaretOffset = editor.getCaretModel().getOffset();
        int caretLineNumber = document.getLineNumber(docCaretOffset);

        int start = document.getLineStartOffset(caretLineNumber);
        int end = document.getLineEndOffset(caretLineNumber);
        CharSequence blockText = document.getCharsSequence().subSequence(start, end);
        return new Block(blockText.toString(), caretLineNumber, caretLineNumber);
    }
}
