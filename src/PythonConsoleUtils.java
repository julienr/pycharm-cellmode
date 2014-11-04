import com.intellij.execution.ExecutionHelper;
import com.intellij.execution.console.LanguageConsoleView;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.util.Consumer;
import com.intellij.util.NotNullFunction;
import com.jetbrains.python.console.PyCodeExecutor;
import com.jetbrains.python.console.PydevConsoleRunner;
import com.jetbrains.python.console.RunPythonConsoleAction;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Collection of utilities to interact with the internal python console.
 * Mostly copied from PyExecuteSelectionAction from pycharm's codebase
 */
public class PythonConsoleUtils {
    public static void execute(final AnActionEvent e, final String selectionText) {
        final Editor editor = CommonDataKeys.EDITOR.getData(e.getDataContext());
        Project project = CommonDataKeys.PROJECT.getData(e.getDataContext());
        Module module = e.getData(LangDataKeys.MODULE);

        findCodeExecutor(e, new Consumer<PyCodeExecutor>() {
            @Override
            public void consume(PyCodeExecutor codeExecutor) {
                executeInConsole(codeExecutor, selectionText, editor);
            }
        }, editor, project, module);
    }

    private static void selectConsole(@NotNull DataContext dataContext, @NotNull Project project,
                                      final Consumer<PyCodeExecutor> consumer) {
        Collection<RunContentDescriptor> consoles = getConsoles(project);

        ExecutionHelper
                .selectContentDescriptor(dataContext, project, consoles, "Select console to execute in", new Consumer<RunContentDescriptor>() {
                    @Override
                    public void consume(RunContentDescriptor descriptor) {
                        if (descriptor != null && descriptor.getExecutionConsole() instanceof PyCodeExecutor) {
                            consumer.consume((PyCodeExecutor) descriptor.getExecutionConsole());
                        }
                    }
                });
    }

    private static Collection<RunContentDescriptor> getConsoles(Project project) {
        return ExecutionHelper.findRunningConsole(project, new NotNullFunction<RunContentDescriptor, Boolean>() {
            @NotNull
            @Override
            public Boolean fun(RunContentDescriptor dom) {
                return dom.getExecutionConsole() instanceof PyCodeExecutor && isAlive(dom);
            }
        });
    }

    private static void findCodeExecutor(AnActionEvent e, Consumer<PyCodeExecutor> consumer, Editor editor, Project project, Module module) {
        if (project != null && editor != null) {
            if (canFindConsole(e)) {
                selectConsole(e.getDataContext(), project, consumer);
            }
            else {
                startConsole(project, consumer, module);
            }
        }
    }

    private static boolean isAlive(RunContentDescriptor dom) {
        ProcessHandler processHandler = dom.getProcessHandler();
        return processHandler != null && !processHandler.isProcessTerminated();
    }

    private static void startConsole(final Project project,
                                     final Consumer<PyCodeExecutor> consumer,
                                     Module context) {
        PydevConsoleRunner runner = RunPythonConsoleAction.runPythonConsole(project, context);
        runner.addConsoleListener(new PydevConsoleRunner.ConsoleListener() {
            @Override
            public void handleConsoleInitialized(LanguageConsoleView consoleView) {
                if (consoleView instanceof PyCodeExecutor) {
                    consumer.consume((PyCodeExecutor)consoleView);
                }
            }
        });
    }

    private static boolean canFindConsole(AnActionEvent e) {
        Project project = CommonDataKeys.PROJECT.getData(e.getDataContext());
        if (project != null) {
            Collection<RunContentDescriptor> descriptors = getConsoles(project);
            return descriptors.size() > 0;
        }
        else {
            return false;
        }
    }

    private static void executeInConsole(@NotNull PyCodeExecutor codeExecutor, @NotNull String text, Editor editor) {
        codeExecutor.executeCode(text, editor);
    }
}
