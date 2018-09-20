import com.intellij.codeInsight.completion.CompletionInitializationContext;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.jetbrains.python.codeInsight.completion.PythonLookupElement;
import com.jetbrains.python.console.pydev.PydevCompletionVariant;
import com.jetbrains.python.psi.PyExpression;
import com.jetbrains.python.psi.PyStatement;
import icons.PythonIcons;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PythonConsoleCompletion extends CompletionProvider<CompletionParameters> {
    private static PythonConsoleCompletion ourInstance = new PythonConsoleCompletion();

    public static PythonConsoleCompletion getInstance() {
        return ourInstance;
    }

    private PythonConsoleCompletion() {

    }

    @Override
    public void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result) {
        PsiElement leaf = parameters.getPosition();
        PyExpression element = PsiTreeUtil.getParentOfType(leaf, PyExpression.class);
        PyStatement statement = PsiTreeUtil.getParentOfType(leaf, PyStatement.class);
        String text = "";
        if (statement != null) {
            text = statement.getText();
            text = text.substring(0, leaf.getTextRange().getEndOffset() - statement.getTextOffset());
            text = text.replace(CompletionInitializationContext.DUMMY_IDENTIFIER_TRIMMED, "");
        }
        String prefix = element != null ? element.getText().replace(CompletionInitializationContext.DUMMY_IDENTIFIER_TRIMMED, "") : "";
        // System.out.println("statement: " + text + ", prefix: " + prefix);
        List<PydevCompletionVariant> completionVariantList = PythonConsoleUtils.complete(leaf.getProject(), text, prefix);
        for (PydevCompletionVariant completionVariant : completionVariantList) {
            result.addElement(new PythonLookupElement(completionVariant.getName(), false, PythonIcons.Python.PythonConsole));
        }

    }
}
