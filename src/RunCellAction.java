import com.intellij.execution.ExecutionHelper;
import com.intellij.execution.console.LanguageConsoleView;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.util.Consumer;
import com.intellij.util.NotNullFunction;
import com.jetbrains.python.console.PyCodeExecutor;
import com.jetbrains.python.console.PydevConsoleRunner;
import com.jetbrains.python.console.RunPythonConsoleAction;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Collection;


// grepcode.com is a good source to search the intellij API
// Document API :
// http://grepcode.com/file/repository.grepcode.com/java/ext/com.jetbrains/intellij-idea/13.0.0/com/intellij/openapi/editor/Document.java#Document.getText%28com.intellij.openapi.util.TextRange%29

// Heavily inspired by the PyExecuteSelectionAction :
// https://github.com/JetBrains/intellij-community/blob/135/python/src/com/jetbrains/python/actions/ExecuteInConsoleAction.java

// Note that the console function will change in the next pycharm release (tagged 139 on git)
public class RunCellAction extends AnAction {
    public static class Block {
        public final String content;
        public final int lineStart;
        public final int lineEnd;

        public Block(String content, int lineStart, int lineEnd) {
            this.content = content;
            this.lineStart = lineStart;
            this.lineEnd = lineEnd;
        }
    }

    private final Preferences prefs = new Preferences();

    @Override
    public void actionPerformed(AnActionEvent e) {
        //System.out.println("RunCellAction");
        Editor editor = CommonDataKeys.EDITOR.getData(e.getDataContext());
        Block block = findBlock(editor);
        if (block != null) {
            if (prefs.getTargetConsole() == Preferences.TARGET_INTERNAL_CONSOLE) {
                execute(e, block.content);
            } else {
                executeInTmux(prefs.getTmuxSessionName(), block.content);
            }
            postExecuteHook(editor, block);
        }
    }

    private void executeInTmux(String sessionName, String codeText) {
        String tmuxExec = prefs.getTmuxExecutable();
        try {
            File temp = File.createTempFile("pycharm_cellmode", ".py");

            try {
                writeToFile(temp, codeText);
                // Use the ipython %load magic
                runCommand(tmuxExec, "set-buffer", "%load -y " + temp.getAbsolutePath() + "\n");
                runCommand(tmuxExec, "paste-buffer", "-t", sessionName);
                // Simulate double enter to scroll through and run loaded code
                runCommand(tmuxExec, "send-keys", "Enter", "Enter");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                temp.delete();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeToFile(File tempFile, String codeText) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
        writer.write(codeText);
        // If the file is empty, it seems like tmux load-buffer keep the current
        // buffer and this cause the last command to be repeated. We do not want that
        // to happen, so add a dummy string
        writer.write(" ");
        writer.close();
    }

    private String readFully(InputStream stream) {
        StringBuilder sb = new StringBuilder();
        try {
            byte[] buffer = new byte[1024];
            while (true) {
                int numRead = stream.read(buffer);
                if (numRead == -1) {
                    break;
                }
                for (int i = 0; i < numRead; ++i) {
                    sb.append(buffer[i]);
                }
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return sb.toString();
        }
    }

    private void runCommand(String... args) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(args);
        pb.redirectErrorStream();
        Process p = pb.start();
        try {
            p.waitFor();
            if (p.exitValue() != 0) {
                // TODO: Should report as an error to the user. How do we do that ?
                System.out.println("Error executing " + StringUtils.join(args, " "));
                System.out.println("Error : " + readFully(p.getInputStream()));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected void postExecuteHook(Editor editor, Block block) {}

    @Override
    public void update(@NotNull AnActionEvent e) {
        // Always visible
        Presentation presentation = e.getPresentation();
        presentation.setEnabled(true);
        presentation.setVisible(true);
    }

    /**
     * Finds the current python block (delimited by ##) in which the caret is.
     * @param editor The editor in which to find the block
     * @return A Block containing the block text or null if no block was found
     */
    private Block findBlock(Editor editor) {
        Document document = editor.getDocument();
        int docCaretOffset = editor.getCaretModel().getOffset();
        int caretLineNumber = document.getLineNumber(docCaretOffset);

        int lineUp = searchForDoubleHash(document, caretLineNumber, -1);
        int lineDown = searchForDoubleHash(document, caretLineNumber, 1);

        //System.out.println("lineUp : " + lineUp + ", lineDown : " + lineDown);
        if (lineUp != -1 && lineDown != -1) {
            int start = document.getLineStartOffset(lineUp + 1);
            int end = document.getLineEndOffset(lineDown - 1);
            if (end - start > 0) {
                CharSequence blockText = document.getCharsSequence().subSequence(start, end);
                //System.out.println("blockText : " + blockText);
                return new Block(blockText.toString(), lineUp, lineDown);
            }
        }
        return null;
    }

    /**
     * Search for ## in the direction given (1 to search down, -1 to search up)
     * @param startLine the line where to start the search
     * @return the line on which ## was found or -1 if none is found
     */
    private int searchForDoubleHash(Document document, int startLine, int direction) {
        int lineCount = document.getLineCount();
        CharSequence text = document.getCharsSequence();
        for (int line = startLine; line >= 0 && line < lineCount; line += direction) {
            int start = document.getLineStartOffset(line);
            int end = document.getLineEndOffset(line);
            if (end - start < 2) {
                continue;
            }

            if (startsWithDoubleHash(text, start, end)) {
                return line;
            }
        }
        return -1;
    }

    // Check if the first two non-space characters in the subseq delimited by (start, end) are ##
    private boolean startsWithDoubleHash(CharSequence seq, int start, int end) {
        for (int ci = start; ci < end; ++ci) {
            if (!Character.isWhitespace(seq.charAt(ci))) {
                if (ci < end - 1) {
                    return seq.charAt(ci) == '#' && seq.charAt(ci + 1) == '#';
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    // --- Start Copied from PyExecuteSelectionAction
    private static void execute(final AnActionEvent e, final String selectionText) {
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

    // -- End Copied from PyExecuteSelectionAction
}
