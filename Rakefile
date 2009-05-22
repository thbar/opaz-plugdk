require 'tasks/tools'
require 'tasks/prepare'
include Opaz::Tools

task :environment do
  @plugin_name, @plugin_type = ENV['plugin'], ENV['type']
  @plugin_folder = "plugins/#{@plugin_name}"
  @java_source_folder = (@plugin_type == 'ruby') ? 'src' : @plugin_folder
  abort("Specify a plugin with 'rake compile package deploy plugin=Delay type=[ruby/java]'") unless @plugin_name && %w(ruby java).include?(@plugin_type)
end

desc "Clean previous build (.class, /build)"
task :clean => :environment do
  Dir[@plugin_folder + "/*.class"].each { |f| rm f }
  rm_rf build_folder(@plugin_folder)
end

desc "Compile what's necessary (plugin and/or java proxy)"
task :compile => [:environment,:clean] do
  system!("javac #{@java_source_folder}/*.java -classpath #{opaz_jars.join(':')}")
end

desc "Package the plugin for each platform"
task :package => [:compile] do
  mkdir build_folder(@plugin_folder)
  if @plugin_type == 'ruby'
    package_ruby_plugin(@plugin_name, @plugin_folder, @java_source_folder)
  else
    package_java_plugin(@plugin_name, @plugin_folder, @java_source_folder)
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
