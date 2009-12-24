require 'java'
$CLASSPATH << PLUGIN_RESOURCES_FOLDER # required for .class loading of hybrid plugins
$LOAD_PATH << PLUGIN_RESOURCES_FOLDER
require 'opaz_plug'

# this is to be able to call the static log() method in VSTPluginAdapter
include_class 'jvst.wrapper.VSTPluginAdapter'
def log(msg)
  VSTPluginAdapter.log("JRuby: #{msg}")
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