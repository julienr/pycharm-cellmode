import com.intellij.icons.AllIcons;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.python.PythonDialectsTokenSetProvider;
import com.jetbrains.python.psi.PyElement;
import com.jetbrains.python.psi.PyExpression;
import com.jetbrains.python.psi.PyStatement;
import org.jetbrains.annotations.Nullable;

public class CompleteAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Editor editor = CommonDataKeys.EDITOR.getData(e.getDataContext());
        PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
        if (editor == null || psiFile == null) {
            return;
        }
        Document document = editor.getDocument();
        int docCaretOffset = editor.getCaretModel().getOffset();
        int caretLineNumber = document.getLineNumber(docCaretOffset);
        // String selectedText = editor.getSelectionModel().getSelectedText();

        PsiElement leaf = psiFile.findElementAt(docCaretOffset - 1);
        PyExpression element = PsiTreeUtil.getParentOfType(leaf, PyExpression.class);
        PyStatement statement = PsiTreeUtil.getParentOfType(leaf, PyStatement.class);
        String text = "";
        if (statement != null) {
            text = statement.getText();
            text = text.substring(0, docCaretOffset - statement.getTextOffset());
        }
        String prefix = element != null ? element.getText() : "";
        PyExpression qualifier = getQualifier(element);
        System.out.println("statement: " + text + ", prefix: " + prefix + (qualifier != null ? ", qualifier: " + qualifier.getText() : ""));
        PythonConsoleUtils.complete(e, text, prefix);
    }

    @Nullable
    public PyExpression getQualifier(PsiElement element) {
        final ASTNode[] nodes = element.getNode().getChildren(PythonDialectsTokenSetProvider.INSTANCE.getExpressionTokens());
        return (PyExpression)(nodes.length == 1 ? nodes[0].getPsi() : null);
    }
}
