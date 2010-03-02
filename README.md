WHAT'S THERE ?
--------------

Opaz-PlugDK makes it easier/faster to create portable (Mac OS X, Windows, Linux) VST 2 plugins.

You can mix and match JRuby, Duby and Java to implement the plugins.

QUICK COURSE
------------

* [Blog post by Thibaut](http://blog.logeek.fr/2009/11/17/how-to-prototype-vst-audio-plugins-with-jruby-and-java)
* [Screencast by Daniel309](http://www.vimeo.com/8654173)

AVAILABLE EXAMPLES
------------------

The examples are under the plugins folder:

* DubyGain, DubyFilta and DubyFreeComp are reflecting our current work trend, ie: mix JRuby (for declarative stuff) and Duby (for computation)
* HybridGain, HybridFilta and HybridSynth mix JRuby (for declarative stuff) and Java (for computation)
* RubyDelay, RubyFreeComp and RubyGain are pure JRuby version

DEPENDENCIES
------------

Required:

* Java JDK (tested with version 1.6+) - the JRE is not sufficient
* JRuby (tested with version 1.4.0)

Optional:

* Duby: jgem install duby (or on Windows: jruby -S gem install duby, if jgem.bat is missing)
* JavaFX: install the JavaFX SDK 1.2.3

HOW TO BUILD A PLUGIN
---------------------

See under plugins/ then use the available rake tasks to package one of them:

	rake package plugin=DubyFreeComp
  
The package system will create ready-to-use VST for Windows, Mac OS X and Linux under the build subfolder of the plugin.

GUI SUPPORT
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

LIVE DEBUGGING
--------------

Thanks to Daniel309, it's possible to enable live plugin patching through an IRB session.

Tweak tasks/tools.rb around line 90 to enable the IRB debugging editor:

	PluginUIClass=IRBPluginGUI

LOGGING
-------

It's often useful to log some informations while creating plugins. You can achieve this by calling "log" from the plugin:

	class MyPlugin < OpazPlug
		def some_stuff
			log("message!")
		end
	end

The logs end up inside some txt file in the plugin folder.

NOTES
-----

The libs and templates folders are generated using rake prepare:templates and rake prepare:libs. Have a look at the rake file to see more about available tasks (compile, package, deploy).

AUTHORS
-------

[Thibaut Barrère](http://twitter.com/thibaut_barrere) and Daniel309 (author of [jVSTwRapper](http://jvstwrapper.sourceforge.net/)).

Big thanks to Charles Oliver Nutter for his help on JRuby/Duby!