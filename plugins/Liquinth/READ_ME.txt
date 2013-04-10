
Liquinth a36 (c)2009 mumart@gmail.com

Liquinth is a relatively simple polysynth for Java.
It can be used either standalone from the JAR file,
or as a VST instrument via jVSTwRapper.

Please let me know how you get on!

Changes

There have been substantial improvements since version a30.
The oscillators are now multisampled, resulting in a much
cleaner sound with less processor time.
The overdrive and vibrato are also considerably better.

Usage

The synthesizer may be run as a standalone application
by executing the JAR file. This will output 48khz audio
with a latency of approximately 25ms.

You can play notes using the computer keyboard if one
of the sliders has focus. Only QWERTY keyboards currently
work correctly. You can set the octave of the computer
keyboard using the function keys, and if any notes get
stuck you can release them with the space bar or the
return key.

When using MIDI input the sliders are assigned to modulation
controllers 20 onwards. You can also use the interface to
assign one of the sliders to controller 1, which is usually
the default for the first modulation wheel.

Configuring jVSTwRapper

When configuring Liquinth as a VST instrument, your jVSTwrapper
configuration file should contain the following entries:

PluginClass=jvst/examples/liquinth/LiquinthVST
PluginUIClass=jvst/examples/liquinth/LiquinthVSTGUI
ClassPath={WrapperPath}\jVSTwRapper-0.9g.jar;{WrapperPath}\jVSTeXamples-0.9g.jar
SystemClassPath={WrapperPath}\jVSTsYstem-0.9g.jar


Kind Regards,
Martin
