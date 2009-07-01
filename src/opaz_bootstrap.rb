require 'java'
$CLASSPATH << PLUGIN_RESOURCES_FOLDER # required for .class loading of hybrid plugins
$LOAD_PATH << PLUGIN_RESOURCES_FOLDER
require 'opaz_plug'

# extract ruby plugin class name from ini file
# current convention: %RubyPlugin%.rb should define the %RubyPlugin% class
plugin_class_name = IO.read(PLUGIN_INI_FILE_NAME).grep(/^RubyPlugin=(.*)/) { $1 }.first

# load the plugin code
require plugin_class_name

# create an instance of plugin
# store it in PLUG for later reference (eg: IRB)
# last evaluated statement must be PLUG (TODO: learn how to read scope variables properly)
PLUG = Object.const_get(plugin_class_name).new(PLUGIN_WRAPPER)