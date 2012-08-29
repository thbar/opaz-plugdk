# trick to bypass Ruby File shadowing java.io.File
module JavaIO
  include_package "java.io"
end

#noinspection RubyInstanceVariableToStringInspection,RubyResolve
def load_plugin
  # todo - detect our platform base on rbconfig output
  platform = :osx

  # todo - share this logic with tools.rb, or not ?
  platform_folder = File.expand_path(File.dirname(__FILE__)) + "/../#{@plugin_folder}/build/#{platform}"
  resources_folder = platform_folder + "/#{@plugin_name}.vst" + (platform == :osx ? "/Contents/Resources" : "")
  native_lib = %w(dll jnilib).map { |ext| Dir[platform_folder+"/**/*.#{ext}"] }.flatten.first # woot!

  # prepare required classes
  #noinspection RubyResolve
  require 'java'
  #noinspection RubyResolve
  $CLASSPATH << resources_folder # $CLASSPATH trick provided by headius on #jruby
  #noinspection RubyResolve
  require 'jVSTwRapper-1.0beta.jar'
  #noinspection RubyResolve
  require 'jVSTsYstem-1.0beta.jar'

  #noinspection RubyResolve
  include_class Java::JRubyVSTPluginProxy

  #noinspection RubyResolve
  JRubyVSTPluginProxy._hackishInit(native_lib, true)
  #noinspection RubyResolve
  JRubyVSTPluginProxy.new(0)
end

desc "Try to ensure the plugin is syntactically correct"
#noinspection RubyResolve
task :validate => :environment do
  plugin = load_plugin
  #noinspection RubyResolve
  plugin.process([[1.0, 0.8]].to_java(Java::float[]), [[1.0, 0.8]].to_java(Java::float[]), 100)
end

desc "Experimental benchmark of a given plugin (required jrake instead of rake)"
#noinspection RubyResolve
task :benchmark => :environment do
  include_class 'javax.sound.sampled.AudioFileFormat'
  include_class 'javax.sound.sampled.AudioInputStream'
  include_class 'javax.sound.sampled.AudioSystem'

  #noinspection RubyResolve
  clip = AudioSystem.getAudioInputStream(JavaIO::File.new(File.dirname(__FILE__) + '/../samples/sample.wav'))
  #noinspection RubyResolve
  puts "Test sample: #{clip.getFormat}"
  #noinspection RubyResolve
  puts "#{clip.getFrameLength} sample frames"

  # todos:
  # - load the whole PCM data in memory and convert it to floats before starting measuring time
  # - pass the floats through plugin.process
  # - save the resulting file to the disk for inspection ?
  #
  # issues to be fixed:
  # - when run on HybridGain, I get:
  #    hasCliprake aborted!
  #    assigning non-exception to $!
  #
  # to read:
  # - http://stackoverflow.com/questions/957850/how-to-convert-a-wav-audio-data-sample-into-an-double-type for help


  # note the conversion to java types (trick from headius again)
  # todo - load and process a real sample instead, and measure the time taken
  plugin = load_plugin
  #noinspection RubyResolve
  plugin.process([[1.0, 0.8]].to_java(Java::float[]), [[1.0, 0.8]].to_java(Java::float[]), 100)
end
