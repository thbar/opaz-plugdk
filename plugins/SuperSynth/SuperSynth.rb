include_class 'IRBPluginGUI'

#noinspection RubyResolve
class SuperSynthGUI
  attr_reader :frame, :plugin

  def initialize(plugin, frame)
    @frame = frame
    @plugin = plugin

    # spawn and attach an IRB session alongside the plugin GUI
    # (stdout (puts, etc.) is redirected to the IRB session
    # irb = IRBPluginGUI.new(JRuby.runtime)  # comment out if you are done debugging


    frame.setTitle("SuperSynth GUI")
    frame.setSize(200, 120)
  end

  # Check RubyGain for a more elaborate GUI example
end

require 'phasor'

#noinspection ALL
class SuperSynth < OpazPlug
  editor SuperSynthGUI

  # plugin "plugin-name", "product-name", "vendor-name"
  plugin "SuperSynth", "SuperSynth", "jVSTwRapper"

  can_do "receiveVstEvents", "receiveVstMidiEvent"
  #noinspection RubyInstanceMethodNamingConvention
  def getPlugCategory
    VSTPluginAdapter.PLUG_CATEG_SYNTH
  end

  unique_id "OWSS"


  param :gain, "Gain", 0.8 #, "dB"

  def initialize wrapper, opts={:bus => "0x1", :synth => true}
    super wrapper
    log "Booting #{getEffectName}:#{getProductString}:#{getVendorString}\n with opts=#{opts.inspect}"
    opts[:in], opts[:out] = opts[:bus].split("x").map(&:to_i) if opts[:bus]
    opts = {:in => 1, :out => 1}.merge(opts)
    setNumInputs opts[:in]
    setNumOutputs opts[:out]
    canProcessReplacing(true)
    setUniqueID(unique_id)
    if opts[:synth]
      isSynth(@synth = true)
      # suspend
    end

    @osc = Phasor.new(@@samplerate)
    canProcessReplacing(true);
  end

  #noinspection RubyInstanceMethodNamingConvention
  def setSampleRate(srate)
    super
    self.sampleRate = srate
  end

  #noinspection RubyClassMethodNamingConvention
  def self.sampleRate= srate
    #noinspection RubyClassVariableNamingConvention
    @@sampleRate = srate
  end

  #noinspection RubyClassMethodNamingConvention
  def self.sampleRate
    # VSTTimeInfo time = this.getTimeInfo(VSTTimeInfo.VST_TIME_AUTOMATION_READING|VSTTimeInfo.VST_TIME_AUTOMATION_WRITING|VSTTimeInfo.VST_TIME_CLOCK_VALID|VSTTimeInfo.VS
    # tempo = time.getTempo();
    # samplePos = time.getSamplePos();
    # sampleRate = time.getSampleRate();
    @@sampleRate
  end


  # TODO: move this to Java/Mirah
  #noinspection RubyInstanceMethodNamingConvention
  def processReplacing(inputs, outputs, sampleFrames)
    # inBuffer, outBuffer = inputs[0], outputs[0]
    outBuffer = outputs[0]
    if @silence
      outBuffer.fill(0, 0...sampleFrames)
    else
      sampleFrames.times do |i|
        outBuffer[i] = 0.99 * gain * @amp * @osc.tick()
      end
    end
  end


  #noinspection RubyInstanceMethodNamingConvention,RubyInstanceMethodNamingConvention
  def processEvents(events)
    Midi.process(events) do |type, note, velocity, delta|
      case type
        when :all_notes_off
          log "all notes off"
          @silence = true
        when :note_on
          if velocity.zero? && note == @currentNote
            log "note on zero"
            @silence = true
          else
            log "note on"
            @silence = false
            @osc.freq = Midi.krystal_freq(note) # .note_to_freq(note)
            @amp = velocity / 127.0
            @delta = delta # TODO ???
          end
      end
    end
    1 # want more
  end

end
