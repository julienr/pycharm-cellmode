import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.*;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.colors.CodeInsightColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.editor.markup.SeparatorPlacement;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

public class PythonCellLineSeparatorProvider implements LineMarkerProvider {
    private final EditorColorsManager colorsManager;

    protected final Preferences prefs = new Preferences();

    private final DaemonCodeAnalyzerSettings daemonSettings;

    public PythonCellLineSeparatorProvider() {
        this.colorsManager = EditorColorsManager.getInstance();
        this.daemonSettings = DaemonCodeAnalyzerSettings.getInstance();
    }

    @Override
    public LineMarkerInfo<PsiElement> getLineMarkerInfo(@NotNull PsiElement element) {
        if (element instanceof PsiComment) {
            Pattern pattern = Pattern.compile(prefs.getDelimiterRegexp());
            if (getLineNumber(pattern, element) >= 0) {
                return createLineSeparatorByElement(element);
            }
        }
        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<? extends PsiElement> list, @NotNull Collection<? super LineMarkerInfo<?>> collection) {
    }

    public static int getLineNumber(Pattern pattern, PsiElement element) {
        Document document = PsiDocumentManager.getInstance(element.getProject()).getDocument(element.getContainingFile());
        if (document != null) {
            int lineNumber = document.getLineNumber(element.getTextRange().getStartOffset());

            int start = document.getLineStartOffset(lineNumber);
            int end = document.getLineEndOffset(lineNumber);
            CharSequence text = document.getCharsSequence().subSequence(start, end);
            if (pattern.matcher(text).matches()) {
                return lineNumber;
            }
        }
        return -1;
    }

    private LineMarkerInfo<PsiElement> createLineSeparatorByElement(PsiElement element) {
        PsiElement anchor = PsiTreeUtil.getDeepestFirst(element);
        LineMarkerInfo<PsiElement> lineMarkerInfo = new LineMarkerInfo<PsiElement>(anchor, anchor.getTextRange(), null, Pass.LINE_MARKERS, null, null, GutterIconRenderer.Alignment.RIGHT);
        lineMarkerInfo.separatorColor = colorsManager.getGlobalScheme().getColor(CodeInsightColors.METHOD_SEPARATORS_COLOR);
        lineMarkerInfo.separatorPlacement = SeparatorPlacement.TOP;
        return lineMarkerInfo;
    }
}
