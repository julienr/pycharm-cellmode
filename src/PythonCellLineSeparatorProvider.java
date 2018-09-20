import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.*;
import com.intellij.openapi.editor.colors.CodeInsightColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.editor.markup.SeparatorPlacement;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class PythonCellLineSeparatorProvider implements LineMarkerProvider {
    private EditorColorsManager colorsManager;

    public PythonCellLineSeparatorProvider(DaemonCodeAnalyzerSettings daemonSettings, EditorColorsManager colorsManager) {
        this.colorsManager = colorsManager;
    }

    @Override
    public LineMarkerInfo<PsiElement> getLineMarkerInfo(@NotNull PsiElement element) {
        if (element instanceof PsiComment) {
            PsiComment comment = (PsiComment) element;
            String value = comment.getText();
            if (value != null && value.startsWith("##")) {
                return createLineSeparatorByElement(element);
            }
        }
        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<PsiElement> list, @NotNull Collection<LineMarkerInfo> collection) {
    }

    private LineMarkerInfo<PsiElement> createLineSeparatorByElement(PsiElement element) {
        PsiElement anchor = PsiTreeUtil.getDeepestFirst(element);
        LineMarkerInfo<PsiElement> lineMarkerInfo = new LineMarkerInfo<PsiElement>(anchor, anchor.getTextRange(), null, Pass.LINE_MARKERS, null, null, GutterIconRenderer.Alignment.RIGHT);
        lineMarkerInfo.separatorColor = colorsManager.getGlobalScheme().getColor(CodeInsightColors.METHOD_SEPARATORS_COLOR);
        lineMarkerInfo.separatorPlacement = SeparatorPlacement.TOP;
        return lineMarkerInfo;
    }
}
