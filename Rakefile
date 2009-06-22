require 'rbconfig'

require 'tasks/tools'
require 'tasks/prepare'
require 'tasks/benchmark'

include Opaz::Tools

task :environment do
  @plugin_name = ENV['plugin']
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

namespace :clojure do

  def execute!(cmd)
    classpath = %w(jline-0.9.94 clojure-1.0.0).map { |f| "libs/#{f}.jar"}
    classpath << "."
    classpath = classpath.join(jar_separator(Config::CONFIG['host_os']))
    system! "java -cp #{classpath} #{cmd}"
  end

  def get_file
    file = ENV['file']
    raise "You must specify a file to run:\r\nrake clojure:run file=Hello.clj" if file.nil?
    file
  end
  
  desc "Experimental: start a clojure REPL"
  task :repl do
    execute! 'jline.ConsoleRunner clojure.lang.Repl'
  end

  desc "Experimental: run a clojure script"
  task :run do
    execute! "clojure.lang.Script #{get_file}.clj"
  end

  desc "Experimental: compile a clojure source"
  task :compile do
    code = "(set! *compile-path* \\\".\\\")"
    code << " (compile '#{get_file})"
    execute! "clojure.main -e \"#{code}\"" 
  end
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
    package_ruby_plugin(@plugin_name, @plugin_folder, @source_folders)
  else
    package_java_plugin(@plugin_name, @plugin_folder, @source_folders)
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