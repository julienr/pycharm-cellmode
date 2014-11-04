import org.apache.commons.lang.StringUtils;

import java.io.*;

public class Tmux {
    public static void executeInTmux(Preferences prefs, String codeText) {
        String sessionName = prefs.getTmuxSessionName();
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
                    sb.append(buffer[i]);
                }
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return sb.toString();
        }
    }

    private static void runCommand(String... args) throws IOException {
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
}
