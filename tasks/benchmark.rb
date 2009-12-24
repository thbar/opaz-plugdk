desc "Experimental benchmark of a given plugin (required jrake instead of rake)"
task :benchmark => :environment do
  abort "Please run this task using jrake" unless defined?(RUBY_ENGINE) && RUBY_ENGINE == "jruby"

  # trick to bypass Ruby File shadowing java.io.File
  module JavaIO
    include_package "java.io"
  end

  # todo - detect our platform base on rbconfig output
  platform = :osx
  
  # todo - share this logic with tools.rb, or not ?
  platform_folder = File.expand_path(File.dirname(__FILE__)) + "/../#{@plugin_folder}/build/#{platform}"
  resources_folder = platform_folder + "/#{@plugin_name}.vst" + (platform == :osx ? "/Contents/Resources" : "")
  native_lib = %w(dll jnilib).map { |ext| Dir[platform_folder+"/**/*.#{ext}"] }.flatten.first # woot!

  # prepare required classes
  require 'java'
  $CLASSPATH << resources_folder # $CLASSPATH trick provided by headius on #jruby
  require 'jVSTwRapper-1.0beta.jar'
  require 'jVSTsYstem-1.0beta.jar'

  include_class Java::JRubyVSTPluginProxy
  include_class 'javax.sound.sampled.AudioFileFormat'
  include_class 'javax.sound.sampled.AudioInputStream'
  include_class 'javax.sound.sampled.AudioSystem'
  
  clip = AudioSystem.getAudioInputStream(JavaIO::File.new(File.dirname(__FILE__) + '/../samples/sample.wav'))
  puts "Test sample: #{clip.getFormat}"
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
  
  JRubyVSTPluginProxy._hackishInit(native_lib,true)
  plugin = JRubyVSTPluginProxy.new(0)

  # note the conversion to java types (trick from headius again)
  # todo - load and process a real sample instead, and measure the time taken
  plugin.process([[1.0,0.8]].to_java(Java::float[]),[[1.0,0.8]].to_java(Java::float[]),100)
end
