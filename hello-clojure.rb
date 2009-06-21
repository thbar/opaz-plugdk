# simple loading test from clojure
# compile occurs at runtime, which means it would be easier to tweak in some repl style
# run through jruby!

require 'java'
require 'libs/clojure-1.0.0.jar'
require 'src/clojure.rb'

puts Clojure.execute("(+ 1 2)")
