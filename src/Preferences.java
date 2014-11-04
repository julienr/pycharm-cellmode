import com.intellij.ide.util.PropertiesComponent;

public class Preferences {
    private final static String PREFS_PREFIX = "net.fhtagn.pycharm.cellmode";

    private final static String PREF_TARGET = PREFS_PREFIX + "target";
    public final static int TARGET_INTERNAL_CONSOLE = 0;
    public final static int TARGET_TMUX = 1;

    private final static String PREF_TMUX_SESSIONNAME = PREFS_PREFIX + "tmux_session";
    private final static String PREF_TMUX_EXECPATH = PREFS_PREFIX + "tmux_path";

    private final PropertiesComponent props;

    public Preferences() {
        // http://confluence.jetbrains.com/display/IDEADEV/Persisting+State+of+Components
        // TODO : Should make the config per-project. But how do we get current project from here ?
        this.props = PropertiesComponent.getInstance();
        //props.setValue("")
    }

    public void setTargetConsole(int target) {
        if (target > 1) {
            // TODO: Should we throw an exception ? Not sure it's a good idea to crash the whole editor
            System.err.println("Invalid target : " + target);
        }
        props.setValue(PREF_TARGET, String.valueOf(target));
    }

    public int getTargetConsole() {
        return props.getOrInitInt(PREF_TARGET, TARGET_INTERNAL_CONSOLE);
    }

    public void setTmuxSessionName(String sessName) {
        props.setValue(PREF_TMUX_SESSIONNAME, sessName);
    }

    public String getTmuxSessionName() {
        return props.getOrInit(PREF_TMUX_SESSIONNAME, "ipython");
    }

    public void setTmuxExecutable(String execPath) {
        props.setValue(PREF_TMUX_EXECPATH, execPath);
    }

    public String getTmuxExecutable() {
        return props.getOrInit(PREF_TMUX_EXECPATH, "/usr/bin/tmux");
    }


}
