require 'tools'
include Opaz::Tools

task :environment do
  @plugin_name = ENV['plugin']
  abort("Specify a plugin with 'rake compile package deploy plugin=Delay'") unless @plugin_name
  @plugin_folder = "plugins/#{@plugin_name}"
end

desc "Clean previous build (.class, /build)"
task :clean => :environment do
  Dir[@plugin_folder + "/*.class"].each { |f| rm f }
  rm_rf @plugin_folder + "/build"
end

desc "Compile the plugin"
task :compile => [:environment,:clean] do
	system!("javac src/*.java -classpath #{opaz_jars.join(':')}")
end

desc "Package the plugin for each platform"
task :package => :environment do
  rm_rf @plugin_folder + "/build"
  mkdir @plugin_folder + "/build"
  package_plugin(@plugin_name, @plugin_folder)
end
