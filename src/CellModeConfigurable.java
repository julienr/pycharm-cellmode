import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.ui.IdeBorderFactory;

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
    private JPanel consolePanel;
    private JTextField delimiterRegexp;
    private JPanel delimiterPanel;
    private JPanel keymapPanel;
    private JTextField delimiterInsert;

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
        delimiterRegexp.setText(prefs.getDelimiterRegexp());
        delimiterInsert.setText(prefs.getDelimiterInsert());
        consolePanel.setBorder(IdeBorderFactory.createTitledBorder("Target Python Console", false));
        ipythonOptionsPanel.setBorder(IdeBorderFactory.createTitledBorder("tmux Options", false));
        keymapPanel.setBorder(IdeBorderFactory.createTitledBorder("Keyboard shortcuts", false));
        delimiterPanel.setBorder(IdeBorderFactory.createTitledBorder("Cell delimiter", false));
        ipythonSessionName.setText(prefs.getTmuxTarget());
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
        prefs.setTmuxTarget(ipythonSessionName.getText());
        prefs.setTmuxExecutable(tmuxExecPath.getText());
        prefs.setTmuxTempFilename(tmuxTempFilename.getText());
        if (sendInternal.isSelected()) {
            prefs.setTargetConsole(Preferences.TARGET_INTERNAL_CONSOLE);
        } else {
            prefs.setTargetConsole(Preferences.TARGET_TMUX);
        }
        prefs.setDelimiterRegexp(delimiterRegexp.getText());
        prefs.setDelimiterInsert(delimiterInsert.getText());
        //System.out.println("target console : " + prefs.getTargetConsole());
        //System.out.println("tmux exec : " + prefs.getTmuxExecutable() + ", session : " + prefs.getTmuxTarget());
    }

    @Override
    public void reset() {

    }

    @Override
    public void disposeUIResources() {

    }
}
