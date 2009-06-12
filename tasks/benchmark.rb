desc "Experimental benchmark of a given plugin (required jrake instead of rake)"
task :benchmark => :environment do
  abort "Please run this task using jrake" unless defined?(RUBY_ENGINE) && RUBY_ENGINE == "jruby"

  # todo - detect our platform base on rbconfig output
  platform = :osx
  
  # todo - share this logic with tools.rb, or not ?
  platform_folder = File.expand_path(File.dirname(__FILE__)) + "/../#{@plugin_folder}/build/#{platform}"
  resources_folder = platform_folder + "/#{@plugin_name}.vst" + (platform == :osx ? "/Contents/Resources" : "")
  native_lib = %w(dll jnilib).map { |ext| Dir[platform_folder+"/**/*.#{ext}"] }.flatten.first # woot!

  # prepare required classes
  require 'java'
  $CLASSPATH << resources_folder # $CLASSPATH trick provided by headius on #jruby
  require 'jVSTwRapper-0.9g.jar'
  require 'jVSTsYstem-0.9g.jar'

  include_class Java::JRubyVSTPluginProxy

  JRubyVSTPluginProxy._hackishInit(native_lib,true)
  plugin = JRubyVSTPluginProxy.new(0)

  # note the conversion to java types (trick from headius again)
  # todo - load and process a real sample instead, and measure the time taken
  plugin.process([[1.0,0.8]].to_java(Java::float[]),[[1.0,0.8]].to_java(Java::float[]),100)
end
