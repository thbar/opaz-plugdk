$LOAD_PATH << 'plugins/Delay'
$LOAD_PATH << 'libs'

require 'java'
require 'jVSTwRapper-0.9g.jar'
require 'jVSTsYstem-0.9g.jar'
require 'Delay'

# todo - tweak for windows/linux here
wrapper = File.expand_path(Dir["templates/**/*.jnilib"].first)

java.lang.System.load(wrapper)

plugin = Delay.new(0)
