require "rspec/core/rake_task"
require 'tasks/tools'
require 'tasks/prepare'

include Opaz::Tools

task :default => :spec

RSpec::Core::RakeTask.new do |t|
  t.pattern = 'specs/*_spec.rb'    
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