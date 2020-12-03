# Python cell mode for PyCharm
This plugin provides actions to allow executing Python code using "cells" in PyCharm, much like [Spyder](https://docs.spyder-ide.org/current/editor.html#defining-code-cells).

A "code cell" is a block of lines, typically delimited by `##`, for example:

    ##
    print 'foo'
    if True:
        print 'bar'
    ##

The plugin options allow you to specify your own regular expression to delimits code cells. 

This plugin provides 3 actions under the Code menu, and you can assign keyboard shortcuts to each:

- Run Cell: run the current cell
- Run Cell and Move Next: run the current cell and advance cursor to next cell
- Run Line under the caret

When using `Run Cell and Move Next` and there is no next cell, a delimiter (which can be specified in the options, but which is unrelated to the above described regular expression) will be inserted in the source code.

The cell can be sent to either :

- the internal IPython console
- an external IPython kernel running in a tmux

The second option allows you to have a working interactive matplotlib in an external IPython process.

Check the "Python Cell Mode" settings in the preferences to switch between the two modes.

This plugin is similar to https://github.com/julienr/vim-cellmode

## Installation

Install from [jetbrains plugin repository](https://plugins.jetbrains.com/plugin/7858)

Alternatively, you can install directly from the jar :

1. Download [PythonCellMode.jar](https://github.com/julienr/pycharm-cellmode/blob/master/PythonCellMode.jar) 
2. In PyCharm, go to "Preferences", search for "plugin". Click on "Install from disk" and choose the downloaded jar
3. Restart PyCharm and use the new actions in the "Code" menu
4. (optional) Configure keyboard shortcuts by searching for "Cell" in your keymap

## Developing the plugin

For now, here are some instructions from memory that may be helpful:
(copied from https://github.com/Khan/ka-pycharm-plugin )

1. Install IntelliJ IDEA Community Edition.
2. Open the repo as an IntelliJ project.
3. Choose your PyCharm installation as the plugin development SDK.
4. Make a run configuration from within IntelliJ and run it. If things work, it will launch a fresh PyCharm instance
   with the plugin installed, which you can use for testing.

### Relevant plugin dev links

http://bjorn.tipling.com/how-to-make-an-intellij-idea-plugin-in-30-minutes

http://confluence.jetbrains.com/display/IDEADEV/Plugin+Compatibility+with+IntelliJ+Platform+Products

https://github.com/JetBrains/intellij-community/blob/1d171c3a1a5fafb82c9a10f8f7b2acd616254f38/python/src/com/jetbrains/python/actions/PyExecuteSelectionAction.java

https://confluence.jetbrains.com/display/IDEADEV/PluginDevelopment
