require 'rbconfig'

require 'tasks/tools'
require 'tasks/prepare'

require 'tasks/benchmark' if defined?(RUBY_ENGINE) && RUBY_ENGINE == "jruby"

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

task :grep_jars do
  val = ENV['what']
  Dir["libs/*.jar"].each do |jar|
    puts "#{jar}"
    result = IO.popen("jar -tf #{jar}").read.grep(Regexp.new(val))
    unless result.empty?
      puts " > #{val} found in #{jar}"
      puts result.join.map { |e| " > #{e}" }
    end
  end
end

def in_folder(folder)
  old_dir = Dir.pwd
  Dir.chdir(folder)
  puts "Moved to #{folder}"
  yield
ensure
  puts "Moving back to #{old_dir}"
  Dir.chdir(old_dir)
end

def dubyc_command
  cmd = 'dubyc'
  cmd << '.bat' if Config::CONFIG['host_os'] =~ /mswin/
  cmd
end

desc "Automatically compile duby in the background"
task :auto_compile_duby do
  # all cross-platforms gems in theory
  require 'fssm'
  require 'ruby-growl'
  
  # todo - use popen4 or redirect to extract the error
  growl = Growl.new "localhost", "ruby-growl", ["Opaz-PlugDK"]
  
  FSSM.monitor('plugins', '**/*.duby') do
    update do |base, relative|
      in_folder(base) do
        puts "========= #{Time.now} ============"
        growl.notify "Opaz-PlugDK", relative, "Compiling..."
        unless system("dubyc -java #{relative}")
          growl.notify "Opaz-PlugDK", relative, "Duby compile failed"
        else
          growl.notify "Opaz-PlugDK", relative, "OK!"
        end
      end
    end
  end
end

desc "Clean previous build (.class, /build)"
task :clean => :environment do
  Dir[@plugin_folder + "/*.class"].each { |f| rm f }
  rm_rf build_folder(@plugin_folder)
end

desc "Compile what's necessary (plugin and/or java proxy)"
task :compile => [:environment,:clean] do
  # first pass - compile .duby to .java to keep them and have a look (useful for debugging)
  duby_files = Dir[@plugin_folder + "/*.duby"]
  unless duby_files.empty?
    duby_files.each do |file|
      in_folder(File.dirname(file)) do
        dubyc = dubyc_command
        cmd = "#{dubyc} -java #{File.basename(file)}"
        puts "Launching: #{cmd} from #{Dir.pwd}"
        system!(cmd)
      end
    end 
  end
  
  javafx_files = Dir[@plugin_folder + "/*.fx"]
  unless javafx_files.empty?
    javafx_files.each do |file|
      in_folder(File.dirname(file)) do
        cmd = "javafxc #{File.basename(file)}"
        puts "Launching: #{cmd}"
        puts Dir.pwd
        system!(cmd)
      end
    end
  end
  
  # second pass - compile .java to .class
  java_files = @source_folders.map { |e| "#{e}/*.java" }
  java_files = java_files.reject { |e| Dir[e].empty? }.join(" ")
  
  system!("javac #{java_files} -classpath #{opaz_jars.join(jar_separator(Config::CONFIG['host_os']))}")

  # third pass, create a jar out of all these .class
  in_folder(@plugin_folder) do
    system!("jar -cf OpazSupport.jar *.class")
    Dir['*.class'].each { |f| rm f }
  end
end

def running_platform
  case Config::CONFIG['host_os']
    when /darwin/; :osx
    when /mswin/; :win
    else raise "Unsupported platform for deploy"
  end
end

desc "Package the plugin for each platform"
task :package => [:compile] do
  mkdir build_folder(@plugin_folder)
  package_plugin(@plugin_name, @plugin_folder, @source_folders,[running_platform]) do |config|
    if @plugin_type == 'ruby'
      config << "# Do not change"
      config << "PluginClass=JRubyVSTPluginProxy"
      config << "RubyPlugin=#{@plugin_name}"
    else
      config << "PluginClass=#{@plugin_name}"
      # TODO - tweak your GUI definition if it's not matching the convention
      config << "PluginUIClass=#{@plugin_name}GUI"
    end
  end
end

desc "Deploy the plugin - EDIT TO MATCH YOUR ENVIRONMENT"
task :deploy => [:package] do
  target_folder = File.dirname(__FILE__) + '/deploy'
  Dir["#{@plugin_folder}/build/#{running_platform}/*"].each do |plugin|
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