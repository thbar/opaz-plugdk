require 'rake/clean'
require 'tasks/tools'
require 'tasks/prepare'

include Opaz::Tools

abort("Specify a plugin with 'rake compile package deploy plugin=Delay'") unless PLUGIN_NAME = ENV['plugin']

JAR_SEP = jar_separator(Config::CONFIG['host_os'])
BASE_JARS = FileList['libs/*.jar'].map { |e| File.expand_path(e) }
BASE_CLASSPATH = BASE_JARS.join(JAR_SEP)
  
PLUGIN_FOLDER = File.join('plugins',PLUGIN_NAME)
PLUGIN_BUILD_FOLDER = File.join(PLUGIN_FOLDER, 'build')
PLUGIN_TYPE = Dir["#{PLUGIN_FOLDER}/*.rb"].empty? ? 'java' : 'ruby' 

CLEAN.include "src/build"
CLEAN.include PLUGIN_BUILD_FOLDER
CLEAN.include Dir[File.join(PLUGIN_FOLDER, "*.duby")].map { |e| e.gsub('.duby','.java')}
CLEAN.include File.join(PLUGIN_FOLDER, "compiled")
CLEAN.include File.join(PLUGIN_FOLDER, "*.class") # legacy - no more .class will be here once clean upgrade

# ====================== common =======================

file 'src/build' do |t|
  FileUtils.mkdir t.name
end

file 'src/build/*.class' => 'src/build' do
  in_folder('src') { system! "javac *.java -classpath #{BASE_CLASSPATH} -d build" }
end

file 'src/build/OpazPlug.jar' => 'src/build/*.class' do
  in_folder('src/build') { system! "jar -cf OpazPlug.jar *.class" }
end

# ====================== plugin =======================

file PLUGIN_BUILD_FOLDER + '/common' do |t|
  FileUtils.mkdir_p t.name
end

file "#{PLUGIN_FOLDER}/*.duby" do |t|
  Dir[t.name].each do |file|
    in_folder(File.dirname(file)) { system!("#{dubyc_command} -java #{File.basename(file)}") }
  end
end

file "#{PLUGIN_FOLDER}/*.java" => [PLUGIN_BUILD_FOLDER+'/common','src/build/OpazPlug.jar'] do |t|
  unless Dir[t.name].empty?
    classpath = (BASE_JARS + ['src/build/OpazPlug.jar']).join(JAR_SEP)
    system! "javac #{t.name} -classpath #{classpath} -d #{PLUGIN_BUILD_FOLDER}/common"
  end
end

file "#{PLUGIN_FOLDER}/*.fx" => PLUGIN_BUILD_FOLDER+'/common' do |t|
  Dir[t.name].each do |file|
    in_folder(File.dirname(file)) { system!("javafxc #{File.basename(file)} -d build/common") }
  end
end

file "#{PLUGIN_FOLDER}/*.rb" => PLUGIN_BUILD_FOLDER+'/common' do |t|
  Dir[t.name].each do |file|
    cp(file, PLUGIN_BUILD_FOLDER + '/common')
  end
end

file "#{PLUGIN_BUILD_FOLDER}/common/Plugin.jar" => ["#{PLUGIN_FOLDER}/*.duby","#{PLUGIN_FOLDER}/*.java","#{PLUGIN_FOLDER}/*.fx"] do |t|
  unless Dir[PLUGIN_BUILD_FOLDER + '/common/*.class'].empty?
    in_folder(PLUGIN_BUILD_FOLDER + '/common') do
      system! "jar -cf Plugin.jar *.class" 
      Dir["*.class"].each { |f| rm f }
    end
  end
end

task :default => :compile

task :compile => ["#{PLUGIN_BUILD_FOLDER}/common/Plugin.jar", PLUGIN_FOLDER+"/*.rb"] do
  cp "src/build/OpazPlug.jar", PLUGIN_BUILD_FOLDER + '/common'
  Dir["src/*.rb"].each { |f| cp f, PLUGIN_BUILD_FOLDER + '/common' }
end

desc "Package the plugin for each platform"
task :package => ['src/build/OpazPlug.jar', :compile] do
  package_plugin(PLUGIN_NAME, PLUGIN_FOLDER, [running_platform]) do |config|
    if PLUGIN_TYPE == 'ruby'
      config << "# Do not change"
      config << "PluginClass=JRubyVSTPluginProxy"
      config << "RubyPlugin=#{PLUGIN_NAME}"
    else
      config << "PluginClass=#{PLUGIN_NAME}"
      # TODO - tweak your GUI definition if it's not matching the convention
      config << "PluginUIClass=#{PLUGIN_NAME}GUI"
    end
  end
end

desc "Deploy the plugin to ./deploy with others - point your vst host to ./deploy or symlink"
task :deploy => [:clean, :package] do
  target_folder = File.dirname(__FILE__) + '/deploy'
  Dir["#{PLUGIN_FOLDER}/build/#{running_platform}/*"].each do |plugin|
    target_plugin = "#{target_folder}/#{plugin.split('/').last}"
    rm_rf(target_plugin) if File.exist?(target_plugin)
    cp_r plugin, target_plugin
  end
end

task :default => :spec

desc "Launch OpazPlugDK tests"
task :spec do
  system!("jruby -S spec -fs specs/opaz_plug_spec.rb")
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