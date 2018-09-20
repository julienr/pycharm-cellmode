import com.intellij.execution.lineMarker.RunLineMarkerContributor;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PythonCellRunLineMarker extends RunLineMarkerContributor {
    @Nullable
    @Override
    public Info getInfo(@NotNull PsiElement element) {
        if (element instanceof PsiComment) {
            PsiComment comment = (PsiComment) element;
            String value = comment.getText();
            if (value != null && value.startsWith("##")) {
                int lineNumber = getLineNumber(comment);
                if (lineNumber >= 0) {
                    return new Info(
                            AllIcons.RunConfigurations.TestState.Run,
                            getActions(lineNumber),
                            (PsiElement e) -> "Run Cell");
                }
            }
        }
        return null;
    }

    private int getLineNumber(PsiElement element) {
        Document document = PsiDocumentManager.getInstance(element.getProject()).getDocument(element.getContainingFile());
        if (document != null) {
            return document.getLineNumber(element.getTextRange().getStartOffset());
        }
        return -1;
    }

    private AnAction[] getActions(int lineNumber) {
        return new RunCellAction[]{new RunCellAction(lineNumber), new RunCellMoveNextAction(lineNumber)};
    }
}
