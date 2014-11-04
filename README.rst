Python cell mode for PyCharm
============================
This provides actions to execute a python "cell" in PyCharm.
The cell can be sent to either :
1. the internal ipython console
2. an external ipython running in a tmux

Option 2. allows you to have a working interactive matplotlib in an external
ipython process.

Check the "Python Cell Mode" settings in the preferences to switch between
the two modes.

Similar to https://github.com/julienr/vim-cellmode

Developing the plugin
=====================
For now, here are some instructions from memory that may be helpful:
(copied from https://github.com/Khan/ka-pycharm-plugin )

1. Install IntelliJ IDEA Community Edition.
2. Open the repo as an IntelliJ project.
3. Choose your PyCharm installation as the plugin development SDK.
4. Make a run configuration from within IntelliJ and run it. If things work, it will launch a fresh PyCharm instance
   with the plugin installed, which you can use for testing.

Relevant plugin dev links
-------------------------

http://bjorn.tipling.com/how-to-make-an-intellij-idea-plugin-in-30-minutes

http://confluence.jetbrains.com/display/IDEADEV/Plugin+Compatibility+with+IntelliJ+Platform+Products

https://github.com/JetBrains/intellij-community/blob/1d171c3a1a5fafb82c9a10f8f7b2acd616254f38/python/src/com/jetbrains/python/actions/PyExecuteSelectionAction.java

https://confluence.jetbrains.com/display/IDEADEV/PluginDevelopment
