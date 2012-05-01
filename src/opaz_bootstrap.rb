require 'java'
$CLASSPATH << PLUGIN_RESOURCES_FOLDER # required for .class loading of hybrid plugins
$LOAD_PATH << PLUGIN_RESOURCES_FOLDER

# opaz libs
require 'OpazPlug.jar'
require 'opaz_plug'

# plugin libs
require 'irb'
require 'Plugin.jar' if File.exists?( File.join(PLUGIN_RESOURCES_FOLDER, "Plugin.jar") )

# this is to be able to call the static log() method in VSTPluginAdapter
include_class 'jvst.wrapper.VSTPluginAdapter'
def log(msg)
  VSTPluginAdapter.log("JRuby: #{msg}")
end

# From: http://blade.nagaokaut.ac.jp/cgi-bin/scat.rb/ruby/ruby-talk/110924
# An extension to IRB to save the session to a file
# filename: is the name of the file to save the session in
# hide_IRB: is a command to suppress output of IRB class method calls.
# to prevent execution of the session in Ruby from giving errors.
def IRB.save_session(filename="irb-session.rb",hide_IRB=true)
   file = File.open(filename,"w")
   file.puts "# Saved IRB session for #{Time.now}",""
   Readline::HISTORY.each do |line|
     next if line =~ /^\s*IRB\./ && hide_IRB
     file.puts line
   end
   file.close
end

$PLUGIN_IS_RELOADING = false

# extract ruby plugin class name from ini file
# current convention: %RubyPlugin%.rb should define the %RubyPlugin% class
plugin_class_name = IO.read(PLUGIN_INI_FILE_NAME).grep(/^RubyPlugin=(.*)/) { $1 }.first.strip

# print some debug stuff
log "plug='#{plugin_class_name}'"
log "res folder='#{PLUGIN_RESOURCES_FOLDER}'"
log "ini file='#{PLUGIN_INI_FILE_NAME}'"
log "wrapper='#{PLUGIN_WRAPPER}'"
#log "load path='#{$LOAD_PATH}'"
#log "class path='#{$CLASSPATH}'"

# load the plugin code
require plugin_class_name

# create an instance of plugin
# store it in PLUG for later reference (eg: IRB)
# last evaluated statement must be PLUG (TODO: learn how to read scope variables properly)
PLUG = Object.const_get(plugin_class_name).new(PLUGIN_WRAPPER)