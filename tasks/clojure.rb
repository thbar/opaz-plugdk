# experimental work that will allow to mix-in clojure code when relevant
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

