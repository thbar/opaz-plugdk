module Opaz
  module Tools

    PLATFORMS = [:linux, :osx, :win]
    JVSTWRAPPER_VERSION = '0.9g'
    
    # javac -classpath jar1:jar2:jar3 works on unix, but need ; for separator on windows
    def jar_separator(platform)
      (platform =~ /mswin/ || platform == :win) ? ';' : ':'
    end

    def bundle_url(platform)
      "http://freefr.dl.sourceforge.net/sourceforge/jvstwrapper/jVSTwRapper-Release-#{JVSTWRAPPER_VERSION}-#{platform}.zip"
    end

    def system!(cmd)
      puts "Launching #{cmd}"
      raise "Failed to launch #{cmd}" unless system(cmd)
    end

    def templatized_file(source,target)
      File.open(target,"w") do |output|
        IO.readlines(source).each do |line|
          line = yield line
          output << line
        end
      end
    end

    def template(platform)
      "templates/#{platform}"
    end
    
    def opaz_jars
      Dir[File.dirname(__FILE__) + "/../libs/*.jar"]
    end

    def download_and_unpack(platform, unzip_folder)
      url = bundle_url(platform)
      zip_file = unzip_folder + "/" + url.split('/').last
      system!("curl #{url} -o #{zip_file} --silent --show-error --location")
      system!("unzip -q #{zip_file} -d #{unzip_folder}")
    end

    def package_ruby_plugin(plugin_name,plugin_folder,java_source_folder)
      package_plugin(plugin_name, plugin_folder, java_source_folder) do |config|
        config << "PluginClass=JRubyVSTPluginProxy"
        config << "RubyPlugin=#{plugin_name}"
      end
    end
    
    def package_java_plugin(plugin_name,plugin_folder,java_source_folder)
      package_plugin(plugin_name, plugin_folder, java_source_folder) do |config|
        config << "PluginClass=#{plugin_name}"
      end
    end
    
    def build_folder(plugin_folder)
      plugin_folder + "/build"
    end
    
    def package_plugin(plugin_name,plugin_folder,source_folders)
      PLATFORMS.each do |platform|
        platform_build_folder = build_folder(plugin_folder) + "/#{platform}"
        resources_folder = platform_build_folder + "/wrapper.vst" + (platform == :osx ? "/Contents/Resources" : "")

        # copy platform template
        cp_r template(platform), platform_build_folder

        # create ini file
        ini_file = resources_folder + "/" + (platform == :osx ? "wrapper.jnilib.ini" : "wrapper.ini")
        File.open(ini_file,"w") do |output|
          content = []
          content << "ClassPath={WrapperPath}/jVSTwRapper-#{JVSTWRAPPER_VERSION}.jar"
          # TODO - is order important here ? If not, base ourselves on opaz_jars to stay DRY
          system_class_path = ["jVSTsYstem-#{JVSTWRAPPER_VERSION}","jVSTwRapper-#{JVSTWRAPPER_VERSION}", "jruby-complete-1.3.0"]
          content << "SystemClassPath=" + system_class_path.map { |jar| "{WrapperPath}/#{jar}.jar"}.join(jar_separator(platform)) + jar_separator(platform) + "{WrapperPath}/"
          content << "IsLoggingEnabled=1"
          content << "JVMOption1=-Djruby.objectspace.enabled=false" #This is the default, so this could eventually be removed
          content << ""
          content << "# JRuby Performance tweaks, enable all for best performance"
          content << "#JVMOption1=-Djruby.compile.fastsend"
          content << "#JVMOption2=-Djruby.compile.fastest"
          content << "#JVMOption3=-Djruby.indexed.methods=true"
          content << "#JVMOption4=-Djruby.compile.mode=FORCE"
          content << "#JVMOption5=-Djruby.compile.fastcase"
          content << ""
          content << "# always enable this UI class - it will ask the plugin if it has an editor or not"
          content << "# alternatively, you can use the debugging UI by uncommenting the appropriate line below"
          content << "#PluginUIClass=IRBPluginGUI"
          content << "PluginUIClass=JRubyVSTPluginGUIProxy"
          content << "AttachToNativePluginWindow=0"
          content << ""
          yield content # offer the caller a way to hook its stuff in here
          content.each { |e| output << e + "\n"}
        end

        # add classes and jars - crappy catch all (include .rb file even for pure-java stuff), but works so far
        resources = opaz_jars
        resources << source_folders.map { |f| Dir["#{f}/*.class"] }
        resources << source_folders.map { |f| Dir["#{f}/*.rb"] }
        resources.flatten.each { |f| cp f, resources_folder }

        # create Info.plist (osx only)
        if platform == :osx
          plist_file = platform_build_folder + "/wrapper.vst/Contents/Info.plist"
          plist_content = IO.read(plist_file).gsub!(/<key>(\w*)<\/key>\s+<string>([^<]+)<\/string>/) do
            key,value = $1, $2
            value = plugin_name+".jnilib" if key == 'CFBundleExecutable'
            value = plugin_name if key == 'CFBundleName'
            "<key>#{key}</key>\n	<string>#{value}</string>"
          end
          File.open(plist_file,"w") { |output| output << plist_content }
        end

        # rename to match plugin name - two pass - first the directories, then the files
        (0..1).each do |pass|
          Dir[platform_build_folder+"/**/wrapper*"].partition { |f| File.directory?(f) }[pass].each do |file|
            File.rename(file,file.gsub(/wrapper/,plugin_name))
          end
        end
      end
    end
    
  end
end
