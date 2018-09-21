import com.intellij.execution.lineMarker.RunLineMarkerContributor;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public class PythonCellRunLineMarker extends RunLineMarkerContributor {

    protected final Preferences prefs = new Preferences();

    @Nullable
    @Override
    public Info getInfo(@NotNull PsiElement element) {
        if (element instanceof PsiComment) {
            PsiComment comment = (PsiComment) element;
            String value = comment.getText();
            Pattern pattern = Pattern.compile(prefs.getDelimiterRegexp());
            int lineNumber = PythonCellLineSeparatorProvider.getLineNumber(pattern, comment);
            if (lineNumber >= 0) {
                return new Info(
                        AllIcons.RunConfigurations.TestState.Run,
                        getActions(lineNumber),
                        (PsiElement e) -> "Run Cell");
            }
        }
        return null;
    }

    private AnAction[] getActions(int lineNumber) {
        return new RunCellAction[]{new RunCellAction(lineNumber), new RunCellMoveNextAction(lineNumber)};
    }
}
