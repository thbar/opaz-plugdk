What's there ?
--------------

It's a work in progress to bring JavaFX integration inside Opaz-PlugDK.

Required
--------

* install JavaFX SDK 1.2 (I'm using the whole NetBeans package as well, easier to edit controls)
* see tasks/prepare.rb/javafx_libs (not cross platform yet) to copy required jars under libs

In tools.rb ~ l79, replace:

      content << "PluginUIClass=JRubyVSTPluginGUIProxy"

by

      content << "PluginUIClass=ToneMatrixGUIDelegator"

Current status
--------------

The plugin fails to pass init3 (see log below) in ToneMatrixGUIDelegator:

    registering native Library '/Users/thbar/VST-Dev/ToneMatrix.vst/Contents/MacOS/ToneMatrix.jnilib'
    JRuby: plug='ToneMatrix'
    JRuby: res folder='/Users/thbar/VST-Dev/ToneMatrix.vst/Contents/MacOS/../Resources'
    JRuby: ini file='/Users/thbar/VST-Dev/ToneMatrix.vst/Contents/MacOS/../Resources/ToneMatrix.jnilib.ini'
    JRuby: wrapper='54187520'
    JRuby: Booting ToneMatrix:Opaz:LoGeek
    reload=1
    registering native Library '/Users/thbar/VST-Dev/ToneMatrix.vst/Contents/MacOS/ToneMatrix.jnilib'
    JRuby: plug='ToneMatrix'
    JRuby: res folder='/Users/thbar/VST-Dev/ToneMatrix.vst/Contents/MacOS/../Resources'
    JRuby: ini file='/Users/thbar/VST-Dev/ToneMatrix.vst/Contents/MacOS/../Resources/ToneMatrix.jnilib.ini'
    JRuby: wrapper='764123136'
    JRuby: Booting ToneMatrix:Opaz:LoGeek
    reload=1
    Initializing CLASSLOAD Plugin GUI=ToneMatrixGUIDelegator
    BEFORE Initializing Plugin GUI on AWT-Event Thread CLAZZ=class ToneMatrixGUIDelegator constructor=public ToneMatrixGUIDelegator(jvst.wrapper.gui.VSTPluginGUIRunner,jvst.wrapper.VSTPluginAdapter)
    JAVA GUI Plugin intitialised properly!
    ToneMatrixGUIDelegator <init>
    ToneMatrixGUIDelegator <init2>

Credits
-------

- daniel309 for his advice
- 'nix' for interop code extracted from jVSTwRapper JavaFXExamples
- guidelines found in original code comments
- well, and me, too :)