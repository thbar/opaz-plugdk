# TODO: convert to mirah
require './midi'
require './dsp'

#noinspection ALL
class AudioDSP
  @@srate = 44.1e3
  @@inv_srate = 1.0 / @@srate

  #noinspection RubyClassMethodNamingConvention
  def self.sampleRate
    @@srate
  end

  #noinspection RubyClassMethodNamingConvention
  def self.sampleRate= srate
    @@srate = srate
    @@inv_srate = 1.0 / @@srate
  end

  #noinspection RubyInstanceMethodNamingConvention
  def sampleRate
    @@srate
  end

end

class Generator < AudioDSP
  def tick
    raise "not implemented!"
  end

  def ticks (samples)
    (1..samples).map { tick }
  end
end

#noinspection RubyResolve
class Oscillator < Generator
  attr_accessor :freq
  DEFAULT_FREQ = Midi::A / 2

  def initialize (freq=DEFAULT_FREQ)
    self.freq = freq
    self
  end
end

#noinspection RubyClassVariableUsageInspection
class Phasor < Oscillator
  attr_accessor :phase

  OFFSET = {true => 0.0, false => 1.0} # branchless trick from Urs Heckmann

  def initialize(freq = DEFAULT_FREQ, phase = Dsp.noise)
    @phase = phase
    super freq
  end

  def tick
    @phase += @inc # increment
    @phase -= OFFSET[@phase <= 1.0] # wrap
  end

  def freq= (arg)
    @freq = arg
    @inc = @freq * @@inv_srate
  end
end


class PhasorOscillator < Oscillator
  def initialize(freq = DEFAULT_FREQ, phase=0)
    @phasor = Phasor.new(srate, phase)
    super srate
  end

  def freq= (arg)
    @phasor.freq = arg
    super
  end

  def phase= (arg)
    @phasor.phase = Dsp.clamp(arg, 0.0, 1.0)
  end

  def phase
    @phasor.phase
  end

  def tock
    @phasor.phase.tap { @phasor.tick }
  end
end

class Tri < PhasorOscillator
  FACTOR = {true => 1.0, false => -1.0}

  def tick
    idx = phase < 0.5
    4*(FACTOR[idx]*tock + Phasor::OFFSET[idx]) - 1
  end
end

class Pulse < PhasorOscillator
  FACTOR = {true => 1.0, false => -1.0}

  def initialize(freq=DEFAULT_FREQ, phase=0)
    @duty = 0.5
    super
  end

  def duty= (arg)
    @duty = Dsp.clamp(arg, 0.0, 1.0)
  end

  def tick
    FACTOR[tock <= @duty]
  end
end

#noinspection RubyResolve
class RpmSaw < PhasorOscillator
  def initialize(freq=MIDI::A, phase=0)
    @beta = 1.0
    @state = @last_out = 0
    super
  end

  def beta= (arg)
    @beta = Dsp.clamp(arg, 0.0, 2.0)
  end

  def tick
    @state = 0.5*(@state + @last_out) # one-pole averager
    @last_out = Math.sin(Dsp::WO_PI * tock + @beta * @state)
  end
end

class RpmSquare < RpmSaw
  def tick
    @state = 0.5*(@state + @last_out*@last_out) # one-pole averager, squared
    @last_out = Math.sin(Dsp::TWO_PI * tock - @beta * @state)
  end
end