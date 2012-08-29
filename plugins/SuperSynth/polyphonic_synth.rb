require 'matrix'
require './dsp'
require './midi'
require './phasor'

class PolyphonicSynth
  #noinspection RubyResolve,RubyUnusedLocalVariable
  class Voice
    # attr_accessor :note #, :velocity, :delta

    def initialize(voicer, synth, opts={})
      @voicer = voicer
      @synth = synth.new(opts[:srate]) #, opts TODO: pass self for stop callback
      self
    end

    def tick
      @synth.tick
    end

    def ticks (samples)
      if @delta > 0
        @delta -= samples if @delta >= samples
        output = Array.zeros(@delta)
        zeros(@delta) + (@delta+1..samples).map { @synth.tick }
      else
        (1..samples).map { @synth.tick }
      end
    end

    def play(note, velocity, delta=0)
      @note, @velocity, @delta = note, velocity, delta
      @synth.freq = Midi.note_to_freq(note)
      @synth.trigger(:attack)
      self
    end

    def release
      @synth.trigger(:release)
    end

    def stop # TODO: call this when synth release envelope stops
      @voicer.free_voice self
    end
  end
  #noinspection RubyResolve
  attr_accessor :srate

  def initialize (synth, num_voices=8, synth_opts={})
    synth_opts = {:srate => 44.1e3}.merge!(synth_opts)
    @voice_scaling = (1..num_voices).map { |i| 1.0 / Math.sqrt(i) } # lookup table
    @voice_pool = (1..num_voices).map { Voice.new(self, synth, synth_opts) }
    @notes_playing = {}
  end

  def all_notes_off
    active_voices.each(&:stop)
    @notes_playing = {}
  end

  def note_on (note, velocity=100, delta=0)
    return note_off(note) if velocity.zero?
    voice = @notes_playing.delete[note] || allocate_voice
    @notes_playing[note] = voice.play(note, velocity, delta)
  end

  def note_off (note)
    #noinspection RubyResolve
    @notes_playing[note].try(&:release)
  end

  def free_voice (voice)
    #noinspection RubyResolve
    if voice == @notes_playing.delete(note) # return to pool if playing
      @voice_pool << voice
    end
    @voice = voice
  end

  def tick
    voices = active_voices
    @voice_scaling[voices.count] * voices.inject(0) { |sum, voice| sum + voice.tick }
  end

  def ticks (samples)
    voices = active_voices
    zeros = Vector[*Array.zeros(samples)]
    output = voices.inject(zeros) { |sum, voice| sum + voice.ticks(samples) }
    (@voice_scaling[voices.count] * output).to_a
  end

  private

  def allocate_voice
    @voice_pool.pop || steal_voice
  end

  def steal_voice # FIXME: we might need a fade out to prevent clicks here?
    active_voices.sort_by(&:delta).first
  end

  def active_voices
    @notes_playing.values
  end

end