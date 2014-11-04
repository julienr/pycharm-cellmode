import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;

import javax.swing.*;

public class CellModeConfigurable implements Configurable {
    private JPanel panel1;
    private JRadioButton sendInternal;
    private JRadioButton sendTmux;
    private JTextField ipythonSessionName;
    private JPanel ipythonOptionsPanel;
    private JPanel targetPanel;
    private JTextField tmuxExecPath;
    private JTextField tmuxTempFilename;

    private final Preferences prefs;

    public CellModeConfigurable() {
        prefs = new Preferences();
    }

    @Override
    public String getDisplayName() {
        return "Python Cell Mode";
    }

    @Override
    public String getHelpTopic() {
        return null;
    }

    @Override
    public JComponent createComponent() {
        ipythonSessionName.setText(prefs.getTmuxSessionName());
        tmuxExecPath.setText(prefs.getTmuxExecutable());
        tmuxTempFilename.setText(prefs.getTmuxTempFilename());
        sendInternal.setSelected(prefs.getTargetConsole() == Preferences.TARGET_INTERNAL_CONSOLE);
        sendTmux.setSelected(prefs.getTargetConsole() == Preferences.TARGET_TMUX);
        return (JComponent)targetPanel;
    }

    @Override
    public boolean isModified() {
        return true;
    }

    @Override
    public void apply() throws ConfigurationException {
        prefs.setTmuxSessionName(ipythonSessionName.getText());
        prefs.setTmuxExecutable(tmuxExecPath.getText());
        prefs.setTmuxTempFilename(tmuxTempFilename.getText());
        if (sendInternal.isSelected()) {
            prefs.setTargetConsole(Preferences.TARGET_INTERNAL_CONSOLE);
        } else {
            prefs.setTargetConsole(Preferences.TARGET_TMUX);
        }
        //System.out.println("target console : " + prefs.getTargetConsole());
        //System.out.println("tmux exec : " + prefs.getTmuxExecutable() + ", session : " + prefs.getTmuxSessionName());
    }

    @Override
    public void reset() {

    }

    @Override
    public void disposeUIResources() {

    }
}
