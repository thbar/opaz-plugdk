include_class 'HybridSynthTools'
require 'MidiTools'

# TODOS:
# - allow to tweak getParameterDisplay from the plugin
#    for instance here we need to display Sawtooth for 0.0 or Pulse for 1.0 (waveform1)
#    and volume strings in Db using dbToString
class HybridSynth < OpazPlug
  include MidiTools
  
  plugin "HybridSynth", "Opaz", "LoGeek"
  can_do "receiveVstEvents", "receiveVstMidiEvent"
  unique_id "hsth"

  param :volume     , "Volume"       , 1.0,  "dB"

  param :waveform1  , "Waveform 1"   , 0.0,  "Shape"
  param :frequency1 , "Frequency 1"  , 0.1,  "Hz"
  param :volume1    , "Volume 1"     , 1.0,  "dB"

  param :waveform2  , "Waveform 2"   , 0.0,  "Shape"
  param :frequency2 , "Frequency 2"  , 0.1,  "Hz"
  param :volume2    , "Volume 2"     , 1.0,  "dB"

  attr_accessor :tools
  
  NUM_OUTPUTS = 2

  WAVE_SIZE = 4096      # samples (must be power of 2 here)
  NUM_FREQUENCIES = 128 # 128 midi notes
  
  def sawtooth
    @sawtooth ||= (0..WAVE_SIZE-1).map { |i| -1.0 + (2.0 * i.to_f / WAVE_SIZE) }
  end
  
  def pulse
    @pulse ||= (0..WAVE_SIZE-1).map { |i| i < WAVE_SIZE / 4 ? -1.0 : 1.0 }
  end
  
  def frequency_table
    @frequency_table ||= begin
      k = 1.059463094359 # 12th root of 2
    	a = 6.875*k*k*k
    	
    	result = []
    	for i in (0..NUM_FREQUENCIES-1)
    	  result[i] = a; a *= k
    	end
    	result
    end
  end
  
  def initialize(wrapper)
    super(wrapper)
    setNumInputs(0)  # no input
    setNumOutputs(NUM_OUTPUTS) # 2 outputs, 1 for each oscillator
    canProcessReplacing(true)
    isSynth(true)
    suspend # what is this ?
  end
  
  def tools
    @tools ||= begin
      t = HybridSynthTools.new
      t.waveSize = WAVE_SIZE
      t.sawtooth = sawtooth.to_java(Java::float)
      t.pulse = pulse.to_java(Java::float)
      t.frequency_table = frequency_table.to_java(Java::float)
      t.scaler = WAVE_SIZE / 44100.0
      t
    end
  end
  
  def setSampleRate(sample_rate)
    super(sample_rate)
    self.tools.scaler = WAVE_SIZE.to_f / sample_rate
  end

  # no idea what's the purpose of this. anyone ???
  def getOutputProperties(index)
    ret = nil

    if index < NUM_OUTPUTS
      ret = VSTPinProperties.new
      ret.setLabel("jVSTx #{index+1}d")
      ret.setFlags(VSTPinProperties::VST_PIN_IS_ACTIVE)
      if (index < 2)
	      # make channel 1+2 stereo
        ret.setFlags(ret.getFlags() | VSTPinProperties::VST_PIN_IS_STEREO)
      end
    end
    
    ret
  end

  # TODO - create an "each" friendly wrapper around VSTEvents ?
  def processEvents(events)
    notes(events) do |type, note, velocity, delta|
      case type
        when :all_notes_off;
          tools.note_off # we have only one note here
        when :note_on;
          if velocity == 0 && note == tools.currentNote
            tools.note_off
          else
            tools.note_on(note, velocity, delta)
          end
      end
    end
    1 # want more
  end
  
  def process(inputs, outputs, sampleFrames)
    tools.processReplacing(inputs, outputs, sampleFrames, volume, volume1, volume2, frequency1, frequency2, waveform1, waveform2)
  end
end
