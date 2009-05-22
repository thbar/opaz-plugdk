require 'tools'

# maintainer tasks - you shouldn't have to use these too much

namespace :prepare do
  
  desc "Download and prepare the platform-specific templates"
  task :templates do
    rm_rf "templates"
    PLATFORMS.each do |platform|
      unzip_folder = template(platform) + "/archive/unzipped"
      mkdir_p unzip_folder unless File.exist?(unzip_folder)
      download_and_unpack(platform, unzip_folder)
    
      root = "#{unzip_folder}/jVSTwRapper-Release-#{JVSTWRAPPER_VERSION}"
      if platform == :osx
        cp_r root+"-osx/jvstwrapper.vst", template(platform)
        File.rename(template(platform) + "/jvstwrapper.vst", template(platform) + '/wrapper.vst')
      else
        mkdir template(platform) + "/wrapper.vst"
        Dir[root+"/*.*"].grep(/(dll|so)$/).each { |f| cp f, template(platform) + "/wrapper.vst" }
      end
      rm_rf template(platform) + "/archive"
      Dir[template(platform) + "/**/*.*"].each do |f|
        rm f if f =~ /\.(bmp|ini|jar)$/
        mv f, File.dirname(f) + "/wrapper.#{$1}" if f =~ /\.(so|jnilib|dll)$/
      end
    end
  end

  desc "Download required libs"
  task :libs do
    rm_rf "libs"
    mkdir_p "libs/temp"
    # the jars are shared accross all distributions, so pick one and extract them
    download_and_unpack(:win, "libs/temp")
    Dir["libs/temp/**/*-#{JVSTWRAPPER_VERSION}.jar"].select { |e| e.split('/').last =~ /jvst(system|wrapper)/i }.each { |f| cp f, "libs" }

    #{}"http://repository.codehaus.org/org/jruby/jruby/1.2.0/"
    system!("curl http://repository.codehaus.org/org/jruby/jruby-complete/1.2.0/jruby-complete-1.2.0.jar -o libs/jruby-complete-1.2.0.jar --silent --show-error")
  end
  
end

