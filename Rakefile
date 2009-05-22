require 'tasks/opaz-tasks'

desc "Deploy the plugin"
task :deploy => [:environment] do#, :package] do
  #target_folder = "/Library/Audio/Plug-Ins/VST/"
  target_folder = File.expand_path("~/VST-Dev")
  Dir["#{@plugin_folder}/build/osx/*"].each do |plugin|
    target_plugin = "#{target_folder}/#{plugin.split('/').last}"
    rm_rf(target_plugin) if File.exist?(target_plugin)
    cp_r plugin, target_plugin
  end
end
