# see also /Users/dfl/projects/dsptest/FastMath.h
require 'matrix'
require './RAFL_wav'

module Dsp
  PI_2 = 0.5*Math::PI
  TWO_PI = 2.0*Math::PI
  SQRT2 = Math.sqrt(2)
  SQRT2_2 = 0.5*Math.sqrt(2)

  extend self

  def noise
    bipolar(random)
  end

  def random
    rand # Random.rand
  end

  def bipolar (x)
    2.0*x - 1.0
  end

  def xfade(a, b, x)
    (b-a)*x + a
  end

  def clamp (x, min, max)
    [min, x, max].sort[1]
  end

  def to_wav(gen, seconds, filename=nil)
    filename ||= "#{gen.class}.wav"
    filename += ".wav" unless filename =~ /\.wav^/i
    RiffFile.new(filename, "wb+") do |wav|
      data = gen.ticks(gen.sampleRate * seconds)
      rescale = calc_sample_value(-0.5, 16) / data.max # normalize to -0.5dBfs
      data.map! { |d| (d*rescale).round.to_f.to_i }
      wav.write(1, gen.sampleRate, 16, [data])
    end
  end

  class LookupTable # linear interpolated, input goes from 0 to 1
    def initialize (bits=7)
      @size = 2 ** bits
      scale = 1.0 / (@size)
      @table = (0..@size).map { |x| yield(scale * x) }
    end

    def [](arg) # from 0 to 1
      offset = arg * @size
      idx = offset.floor
      frac = offset - idx
      return @table.last if idx >= @size
      Dsp.xfade(@table[idx], @table[idx+1], frac)
    end
  end

end


module ArrayExtensions
  def full_of(val, num)
    [].fill(val, 0...num)
  end

  def zeros (num)
    full_of(0, num)
  end
end

Array.send :extend, ArrayExtensions

# module VectorExtensions
#   def full_of(val,num)
#     Vector[ *Array.full_of(val,num) ]
#   end
# end
# 
# Vector.send :extend, VectorExtensions


# test LUT
# require './dsp'
# 
# def calc_detune x
#   1.0 - (1.0-x) ** 0.2
# end
#   
# @@detune = Dsp::LookupTable.new{|x| calc_detune(x) }
# 
# @@detune[0]
# @@detune[1]
