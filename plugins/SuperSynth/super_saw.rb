require './phasor'
#require './dsp'

class Processor < AudioDSP
  def tick(s)
    raise "not implemented!"
  end

  def ticks (inputs)
    inputs.map { |s| tick(s) }
  end

end

#noinspection ALL
class Biquad < Processor
  def initialize(a=[1.0, 0, 0], b=[1.0, 0, 0], norm=false)
    update(Vector[*a], Vector[*b])
    normalize if norm
    clear
    self
  end

  def clear
    @input = [0, 0, 0]
    @output = [0, 0, 0]
    stop_interpolation
  end

  #noinspection RubyInstanceVariableNamingConvention
  def update a, b
    @_a, @_b = @a, @b
    @a, @b = a, b
    interpolate if interpolating?
  end

  def normalize # what about b0 (gain)
    inv_a0 = 1.0/a0
    @a *= inv_a0
    @b *= inv_a0
  end

  def stop_interpolation
    @_a = @_b = nil
  end

  def interpolate # TODO: interpolate over VST sample frame ?
    @interp_period = (@@srate * 1e-3).floor # 1ms
    t = 1.0 / @interp_period
    @delta_a = (@a - @_a) * t
    @delta_b = (@b - @_b) * t
    @interp_ticks = 0
  end

  def interpolating?
    @_a && @_b
  end

  def tick input
    if interpolating? # process with interpolated state
      @_a += @delta_a
      @_b += @delta_b
      process(input, @_a, @_b).tap do
        stop_interpolation if (@interp_ticks += 1) >= @interp_period
      end
    else
      process(input)
    end
  end

  def process input, a=@a, b=@b # default to normal state
    output = a[0]*input + a[1]*@input[1] + a[2]*@input[2]
    output -= b[1]*@output[1] + b[2]*@output[2]
    @input[2] = @input[1]
    @input[1] = input
    @output[2] = @output[1]
    @output[1] = output
  end
end

#noinspection ALL
class ButterHpf < Biquad
  def initialize f
    self.freq = f # triggers recalc
    clear
    self
  end

  def freq= arg
    @w = arg * @@inv_srate # (0..0.5)
    recalc
  end

  def recalc
    # from /Developer/Examples/CoreAudio/AudioUnits/AUPinkNoise/Utility/Biquad.cpp 
    k = Math.tan(Math::PI * @w)
    kk = k*k;
    g = Dsp::SQRT2*k + kk
    d_inv = 1.0 / (1 + g);

    a0 = d_inv
    a1 = -2 * d_inv
    a2 = d_inv
    b0 = 1.0 # gain
    b1 = 2*kk-1 * d_inv
    b2 = (1 - g) * d_inv
    update(Vector[a0, a1, a2], Vector[b0, b1, b2])
  end
end


#noinspection ALL
class Hpf < Biquad
  def initialize(f, q=nil)
    @inv_q = q ? 1.0 / q : Math.sqrt(2) # default to butterworth
    self.freq = f # triggers recalc
    clear
    self
  end

  def q= arg
    @inv_q = 1.0 / arg
    recalc
  end

  def freq= arg
    @w = arg * Dsp::TWO_PI * @@inv_srate
    recalc
  end

  def recalc # from RBJ cookbook @ http://www.musicdsp.org/files/Audio-EQ-Cookbook.txt
    alpha = 0.5 * @inv_q * Math.sin(@w)
    cw = Math.cos(@w)
    ocw = 1+cw
    b0 = b2 = 0.5*ocw
    b1 = -ocw

    a0 = 1 + alpha
    a1 = -2.0*cw
    a2 = 1 - alpha
    update(Vector[a0, a1, a2], Vector[b0, b1, b2])
  end

end

#noinspection RubyClassVariableUsageInspection
class SuperSaw < Oscillator
  attr_accessor :phat

  def initialize (freq = DEFAULT_FREQ, spread=0.5, num=7)
    @master = Phasor.new
    @phasors = (1..num-1).map { Phasor.new }
    @phat = spread
    setup_tables
    @hpf = ButterHpf.new(@master.freq)
    randomize_phase
    self.freq = freq
    self
  end

  def randomize_phase
    @phasors.each { |p| p.phase = Dsp.random }
  end

  def clear
    @hpf.clear
    randomize_phase
  end

  def freq= (f)
    @hpf.freq = @master.freq = @freq = f
    @phasors.each_with_index { |p, i| p.freq = (1 + @@detune[@phat] * @@offsets[i]) * f; puts "#{i+1}: #{p.freq}" }
  end

  def tick
    osc = @@center[@phat] * @master.tick
    osc += @@side[@phat] * @phasors.inject(0) { |sum, p| sum + p.tick }
    @hpf.tick(osc)
  end

  def ticks (samples)
    osc = @@center[@phat] * Vector[*@master.ticks(samples)]
    osc = @@side[@phat] * @phasors.inject(osc) { |sum, p| sum + Vector[*p.ticks(samples)] }
    @hpf.ticks(osc.to_a)
  end

  private

  def setup_tables
    @@offsets ||= [-0.11002313, -0.06288439, -0.01952356, 0.01991221, 0.06216538, 0.10745242]
    @@detune ||= Dsp::LookupTable.new { |x| calc_detune(x) }
    @@side ||= Dsp::LookupTable.new { |x| calc_side(x) }
    @@center ||= Dsp::LookupTable.new { |x| calc_center(x) }
  end

  def calc_detune (x)
    1.0 - (1.0-x) ** 0.2
  end

  def calc_side (x)
    -0.73754*x*x + 1.2841*x + 0.044372
  end

  def calc_center (x)
    -0.55366*x + 0.99785
  end

end