import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// Run currently selected text
public class RunSelectionAction extends AbstractRunAction {

    protected Block findBlock(Editor editor) {
        if (editor.getSelectionModel().hasSelection()) {
            Document doc = editor.getDocument();
            SelectionModel model = editor.getSelectionModel();
            String selectedText = model.getSelectedText();
            int selectStartLine = doc.getLineNumber(model.getSelectionStart());
            int selectEndLine = doc.getLineNumber(model.getSelectionEnd());
            return new Block(selectedText, selectStartLine, selectEndLine);
        }
        else {
            return null;
        }
    }
}
