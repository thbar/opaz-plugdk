require 'rbconfig'

require 'tasks/tools'
require 'tasks/prepare'
require 'tasks/benchmark'

include Opaz::Tools

task :environment do
  @plugin_name = ENV['plugin']

  # ensure the plugin name is exactly the same, case-sensitive name as its folder - avoids hard to understand loading issues
  # todo - automatically replace ? add more checks for class defined under the plugin folder ?
  @exact_plugin_name = Dir['plugins/*'].find { |e| e.split('/').last.upcase == @plugin_name.upcase }.split('/').last
  raise "Plugin names are case-sensitive. Did you mean #{@exact_plugin_name} instead of #{@plugin_name} ?" if @exact_plugin_name != @plugin_name
  
  @plugin_folder = "plugins/#{@plugin_name}"
  @plugin_type = Dir["#{@plugin_folder}/*.rb"].empty? ? 'java' : 'ruby' 
  @source_folders = []
  @source_folders << @plugin_folder
  @source_folders << 'src' if @plugin_type == 'ruby' # add the proxy only for pure-ruby plugins
  abort("Specify a plugin with 'rake compile package deploy plugin=Delay'") unless @plugin_name
end

task :clean_system do
  Dir["src/*.class"].each { |f| rm f }
end

desc "Clean previous build (.class, /build)"
task :clean => :environment do
  Dir[@plugin_folder + "/*.class"].each { |f| rm f }
  rm_rf build_folder(@plugin_folder)
end

desc "Compile what's necessary (plugin and/or java proxy)"
task :compile => [:environment,:clean] do
  java_files = @source_folders.map { |e| "#{e}/*.java" }
  java_files = java_files.reject { |e| Dir[e].empty? }.join(" ")
  
  system!("javac #{java_files} -classpath #{opaz_jars.join(jar_separator(Config::CONFIG['host_os']))}")
end

desc "Package the plugin for each platform"
task :package => [:compile] do
  mkdir build_folder(@plugin_folder)
  if @plugin_type == 'ruby'
    package_plugin(@plugin_name, @plugin_folder, @source_folders) do |config|
      config << "PluginClass=JRubyVSTPluginProxy"
      config << "RubyPlugin=#{@plugin_name}"
      config << "PluginUIClass=JRubyVSTPluginGUIProxy" # editor class will be given by the ruby plugin itself
      config << "#PluginUIClass=IRBPluginGUI" # uncomment this and comment previous to activate IRB debugger
    end
  else
    package_plugin(@plugin_name, @plugin_folder, @source_folders) do |config|
      config << "PluginClass=#{@plugin_name}"
    end
  end
end

desc "Deploy the plugin - EDIT TO MATCH YOUR ENVIRONMENT"
task :deploy => [:package] do
  #target_folder = "/Library/Audio/Plug-Ins/VST/"
  target_folder = File.expand_path("~/VST-Dev")
  Dir["#{@plugin_folder}/build/osx/*"].each do |plugin|
    target_plugin = "#{target_folder}/#{plugin.split('/').last}"
    rm_rf(target_plugin) if File.exist?(target_plugin)
    cp_r plugin, target_plugin
  end
end

desc "Launch OpazPlugDK tests"
task :spec do
  system!("jruby -S spec -fs specs/opaz_plug_spec.rb")
end