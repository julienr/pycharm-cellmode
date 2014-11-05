import com.intellij.notification.EventLog;
import com.intellij.notification.Notification;
import com.intellij.openapi.ui.Messages;
import org.apache.commons.lang.StringUtils;

import java.io.*;

public class Tmux {
    private static Object monitor = new Object();

    public static void executeInTmux(Preferences prefs, String codeText) {
        String sessionName = prefs.getTmuxSessionName();
        String tmuxExec = prefs.getTmuxExecutable();
        String fname = prefs.getTmuxTempFilename();
        try {
            File temp = new File(fname);

            // We don't want to have multiple command interleaved
            synchronized(monitor) {
                writeToFile(temp, codeText);
                // TODO: Check if tmux session exists

                // Use the ipython %load magic
                runCommand(tmuxExec, "set-buffer", "%load -y " + temp.getAbsolutePath() + "\n");
                runCommand(tmuxExec, "paste-buffer", "-t", sessionName);
                // Simulate double enter to scroll through and run loaded code
                runCommand(tmuxExec, "send-keys", "Enter", "Enter");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeToFile(File tempFile, String codeText) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
        writer.write(codeText);
        // If the file is empty, it seems like tmux load-buffer keep the current
        // buffer and this cause the last command to be repeated. We do not want that
        // to happen, so add a dummy string
        writer.write(" ");
        writer.close();
    }

    private static String readFully(InputStream stream) {
        StringBuilder sb = new StringBuilder();
        try {
            byte[] buffer = new byte[1024];
            while (true) {
                int numRead = stream.read(buffer);
                if (numRead == -1) {
                    break;
                }
                for (int i = 0; i < numRead; ++i) {
                    sb.append((char)buffer[i]);
                }
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return sb.toString();
        }
    }

    private static void runCommand(String... args) throws IOException {
        // See http://tomaszdziurko.pl/2011/09/developing-plugin-intellij-idea-some-tips-and-links/
        // StatusBar statusBar = WindowManager.getInstance()
        //                       .getStatusBar(DataKeys.PROJECT.getData(actionEvent.getDataContext()));
        // JBPopupFactory.getInstance()
        // .createHtmlTextBalloonBuilder(htmlText, messageType, null)
        //        .setFadeoutTime(7500)
        //        .createBalloon()
        //        .show(RelativePoint.getCenterOf(statusBar.getComponent()),
        //                Balloon.Position.atRight);
        ProcessBuilder pb = new ProcessBuilder(args);
        pb.redirectErrorStream(true);
        Process p = pb.start();
        try {
            p.waitFor();
            if (p.exitValue() != 0) {
                String msg = "Error executing " + StringUtils.join(args, " ") + "\n"
                           + "Error : " + readFully(p.getInputStream());
                Messages.showErrorDialog(msg, "Python Cell Mode Error");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
