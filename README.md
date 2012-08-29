Notes by weirdpercent
-------
I love this project, it's a great idea that pulled me in the second I laid eyes on it. To all who have contributed to it, I thank you. This fork is all about revitalizing the concept and improving upon the technology. I fixed the Rakefile so that the default rake task (test) works with 'rspec' instead of 'spec.' In case you were wondering, I did some refactoring to change all 'Duby' references to the current language name 'Mirah.'


I have created a roadmap and committed version 0.1. The changes are summarized in the **roadmap** folder. I've also issued a pull request, and will do so again at version 0.2. My hope is that this super branch can eventually be merged with the master. Versions 0.1 and 0.2 will update Opaz-PlugDK for JDK 1.6/JRuby 1.6.7.2 and JDK 1.7/JRuby 1.7.0.preview2 respectively. The overall goal of these changes is to update Opaz for Ruby 1.9 and Java 7. Once this is done, I will likely add more to the roadmap.

What is Opaz-PlugDK?
--------------

Opaz-PlugDK makes it fast and easy to create portable VST2 plugins for Mac OS X, Windows, and Linux. The code can be written in JRuby, Java, Mirah, or any combination of the three.

Quick Course
------------

* Documentation for Opaz-PlugDK and jVSTwRapper can be found in the **docs** folder
* [Blog post by Thibaut](http://blog.logeek.fr/2009/11/17/how-to-prototype-vst-audio-plugins-with-jruby-and-java)
* [Screencast by Daniel309](http://www.vimeo.com/8654173)



Available Examples
------------------

About the examples in the plugin folder:

* MirahGain, MirahFilta and MirahFreeComp mix JRuby (for declarative stuff) and Mirah (for computation)
* HybridGain, HybridFilta and HybridSynth mix JRuby (for declarative stuff) and Java (for computation)
* RubyDelay, RubyFreeComp and RubyGain are pure JRuby plugins
* JavaDelay is a working pure Java plugin
* InlineGain is an example of using inline Java for computation and JRuby for everything else
* SuperSynth is [David Lowenfels'](http://github.com/dfl) pure JRuby contribution
* SimplePlugin is the starter plugin for developers, similar to RubyGain but with more features.
* The Stuff folder contains a mock VST host, a MIDI plugin skeleton, and an audio plugin skeleton, all written in Java.
* ToneMatrix is an experimental plugin using the JavaFX 1.2.3 SDK

NOTE: ToneMatrix is broken for now due to JavaFX deprecation issues. With the release of JavaFX 2.x, the creation of [GroovyFX](http://groovyfx.org/) and [ScalaFX](http://code.google.com/p/scalafx/), and the transition from JavaFX Script to [Visage](http://code.google.com/p/visage/), using FX to design plugin GUIs should **definitely** be explored some more. It's certainly on my list of ToDos. - weirdpercent

Dependencies
------------

Required:

* [Java JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html) (tested with version 1.6) - the JRE is not sufficient
* [JRuby](http://www.jruby.org/download) (tested with version 1.6.7.2)
* [RSpec](http://rspec.info/): gem install rspec (or on Windows: jruby -S gem install rspec)
* [Mirah](http://www.mirah.org/): gem install mirah (or on Windows: jruby -S gem install mirah)

Optional:

* [JavaFX](http://www.oracle.com/technetwork/java/javafx/downloads/index.html): install the JavaFX SDK
* [jVSTwRapper](http://jvstwrapper.sourceforge.net/): used by this project in JAR form
* [JVSTHost](http://github.com/mhroth/jvsthost/) by Martin Roth: a VST host implemented in Java, a great way to load VST plugins running on the JVM!

How to Build a Plugin
---------------------

You can build a single plugin like this:

	rake package plugin=SimplePlugin
  
The package system will create ready-to-use VST for Windows, Mac OS X and Linux under the build subfolder of the plugin. You can use SimplePlugin (or any other example) as a base to start developing your own. SimplePlugin includes an IRB interface and live reloading when source code changes.

Build Order
===========

Things are currently compiled in this order:

* src/* (Opaz stuff)
* your_plugin/*.mirah
* your_plugin/*.java
* your_plugin/*.fx

Plugins Status
==============

I'll post the results once I've tested and hacked on this project for a bit. - weirdpercent

GUI Support
-----------

Basic GUI support is included. Example:

	class MyEditor
		def initialize(frame)
			frame.setTitle("Hello from DefaultEditor!")
			frame.setSize(400, 300)
		end
	end
	
	class MyPlugin < OpazPlug
		plugin "MyPlugin", "Opaz", "LoGeek"
		# ...
		
		editor MyEditor
	end
	
You'll be passed the main container frame so that you can add stuff in it.

Live Debugging
--------------

Thanks to Daniel309, it's possible to enable live plugin patching through an IRB session.

Tweak tasks/tools.rb around line 90 to enable the IRB debugging editor:

	PluginUIClass=IRBPluginGUI

Logging
-------

It's often useful to log some information while creating plugins. You can achieve this by calling "log" from within the plugin:

	class MyPlugin < OpazPlug
		def some_stuff
			log("message!")
		end
	end

The logs are written to a .txt file in the plugin folder.

Notes
-----

The libs and templates folders are generated using rake prepare:templates and rake prepare:libs. Have a look at the rake file to see more about available tasks (compile, package, deploy).

AUTHORS AND CONTRIBUTORS
------------------------

* [Thibaut Barrère](http://twitter.com/thibaut_barrere) and Daniel309 (author of [jVSTwRapper](http://jvstwrapper.sourceforge.net/)) for the whole thing
* [Robert Pitts](http://github.com/rbxbx) for his test clean-up patch
* Big thanks to [Charles Oliver Nutter](http://github.com/headius) for his help on JRuby/Mirah!
* Thanks as well to [JFXtras](http://jfxtras.org/) for their implementation of SceneToJComponent.
* Thanks to [David Lowenfels](http://github.com/dfl) for his **super** branch that inspired me to begin contributing to Opaz-PlugDK.