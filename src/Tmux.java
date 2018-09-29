import com.intellij.openapi.ui.Messages;
import org.apache.commons.lang.StringUtils;

import java.io.*;

public class Tmux {
    private static Object monitor = new Object();

    public static void executeInTmux(Preferences prefs, String codeText) {
        String target = "\"" + prefs.getTmuxTarget() + "\"";
        String tmuxExec = prefs.getTmuxExecutable();
        String fname = prefs.getTmuxTempFilename();
        try {
            File temp = new File(fname);

            // We don't want to have multiple command interleaved
            synchronized(monitor) {
                // Add end-of-line
                writeToFile(temp, codeText + "\n");
                // TODO: Check if tmux target exists

                // Use the ipython %load magic
                runCommand(tmuxExec, "set-buffer", "\"%load -y " + temp.getAbsolutePath() + "\"\n");
                runCommand(tmuxExec, "paste-buffer", "-t " + target);
                // Simulate double enter to scroll through and run loaded code
                runCommand(tmuxExec, "send-keys", "-t " + target, "Enter");
                runCommand(tmuxExec, "send-keys", "-t " + target, "Enter");
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
        final boolean isUnix = !System.getProperty("os.name").startsWith("Windows");

        ProcessBuilder pb;
        if (isUnix) {
            // TODO: For some strange reason, direct execution (without /bin/sh) fails on the -t $sess:win.0 . It seems
            // that the ".0" at the end gets truncated for whatever reason and tmux doesn't find the session...
            // encoding or escaping issue ?
            // So for now, do everything through /bin/sh
            final String command = StringUtils.join(args, " ");
            pb = new ProcessBuilder("/bin/sh", "-c", command);
        } else {
            pb = new ProcessBuilder(args);
        }

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
