import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;


// grepcode.com is a good source to search the intellij API
// Document API :
// http://grepcode.com/file/repository.grepcode.com/java/ext/com.jetbrains/intellij-idea/13.0.0/com/intellij/openapi/editor/Document.java#Document.getText%28com.intellij.openapi.util.TextRange%29

// Heavily inspired by the PyExecuteSelectionAction :
// https://github.com/JetBrains/intellij-community/blob/135/python/src/com/jetbrains/python/actions/ExecuteInConsoleAction.java

// Note that the console function will change in the next pycharm release (tagged 139 on git)
public class RunCellAction extends AbstractRunAction {
    static private String myText = "Run Cell";
    private int lineNumber;

    public RunCellAction(String text, int lineNumber) {
        super(text);
        this.lineNumber = lineNumber;
    }

    public RunCellAction() {
        super(myText);
        this.lineNumber = -1;
    }

    public RunCellAction(int lineNumber) {
        super(myText);
        this.lineNumber = lineNumber;
    }

    /**
     * Finds the current python block (delimited by ##) in which the caret is.
     * @param editor The editor in which to find the block
     * @return A Block containing the block text or null if no block was found
     */
    protected Block findBlock(Editor editor) {
        Document document = editor.getDocument();
        int caretLineNumber = lineNumber;
        if (lineNumber == -1) {
            int docCaretOffset = editor.getCaretModel().getOffset();
            caretLineNumber = document.getLineNumber(docCaretOffset);
        }

        int lineUp = searchForDelimiter(document, caretLineNumber, -1);
        int lineDown = searchForDelimiter(document, caretLineNumber + 1, 1);

        //System.out.println("lineUp : " + lineUp + ", lineDown : " + lineDown);
        int start, end;
        if (lineUp == -1) {
            // from top
            start = 0;
        } else {
            // from '##'
            start = document.getLineStartOffset(lineUp + 1);
        }

        if (lineDown == -1) {
            // to bottom
            lineDown = document.getLineCount();
        }
        if (lineDown == 0) {
            end = 0;
        } else {
            // to '##'
            end = document.getLineEndOffset(lineDown - 1);
        }

        if (end - start > 0) {
            CharSequence blockText = document.getCharsSequence().subSequence(start, end);
            //System.out.println("blockText : " + blockText);
            return new Block(blockText.toString(), lineUp, lineDown);
        }

        return null;
    }


}
