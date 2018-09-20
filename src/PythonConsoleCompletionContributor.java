import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.patterns.PlatformPatterns;

public class PythonConsoleCompletionContributor extends CompletionContributor {
    public PythonConsoleCompletionContributor() {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(), PythonConsoleCompletion.getInstance());
    }
}
