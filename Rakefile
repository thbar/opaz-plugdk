task :default => :spec

desc "Launch OpazPlugDK tests"
task :spec do
  system("jruby -S spec -fs specs/opaz_plug_spec.rb")
end

desc "Start a console"
task :console do
  abort "Please run this task using jrake" unless defined?(RUBY_ENGINE) && RUBY_ENGINE == "jruby"
  ARGV.clear #Avoid passing args to IRB
  require 'java'
  $CLASSPATH << 'libs'
  $LOAD_PATH << 'src'
  require 'jVSTwRapper-1.0beta.jar'
  require 'jVSTsYstem-1.0beta.jar'
  require 'opaz_plug'
  require 'irb'
  require 'irb/completion'
  IRB.start
end