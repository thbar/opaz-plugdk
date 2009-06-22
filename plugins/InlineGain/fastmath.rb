require 'rubygems'
require 'inline'
require 'java_inline'
require 'benchmark'

class FastMath
  def factorial_ruby(n)
    f = 1
    n.downto(2) { |x| f *= x }
    return f
  end

  def fib_ruby(n)
    if n < 2
      n
    else
      fib_ruby(n - 2) + fib_ruby(n - 1)
    end
  end
  
  inline :Java do |builder|
    builder.package "org.jruby.test"
    builder.java "
      public static long factorial_java(int max) {
        int i=max, result=1;
        while (i >= 2) { result *= i--; }
        return result;
      }
      "

    builder.java "
      public static int fib_java(int n) {
        if (n < 2) return n;

        return fib_java(n - 2) + fib_java(n - 1); 
      }
      "
  end

  def bench
    require 'benchmark'

    Benchmark.bm(30) do |bm|
    end
  end
end

math = FastMath.new

Benchmark.bmbm(30) {|bm|
  5.times { bm.report("factorial_ruby") { 30000.times { math.factorial_ruby(30) } } }
  5.times { bm.report("factorial_java") { 30000.times { math.factorial_java(30) } } }
  5.times { bm.report("fib_ruby(35)") { math.fib_ruby(30) } }
  5.times { bm.report("fib_java(35)") { math.fib_java(30) } }
}

