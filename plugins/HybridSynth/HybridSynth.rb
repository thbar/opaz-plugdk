require 'HybridTables'

# TODOS:
# - allow to tweak getParameterDisplay from the plugin
#    for instance here we need to display Sawtooth for 0.0 or Pulse for 1.0 (waveform1)
#    and volume strings in Db using dbToString
class HybridSynth < OpazPlug
  include HybridTables
  
  plugin "HybridSynth", "Opaz", "LoGeek"
  can_do "receiveVstEvents", "receiveVstMidiEvent", "midiProgramNames"
  unique_id "hsth"

  param :volume     , "Volume"       , 1.0,  "dB"

  param :waveform1  , "Waveform 1"   , 0.0,  "Shape"
  param :frequency1 , "Frequency 1"  , 0.1,  "Hz"
  param :volume1    , "Volume 1"     , 1.0,  "dB"

  param :waveform2  , "Waveform 2"   , 0.0,  "Shape"
  param :frequency2 , "Frequency 2"  , 0.1,  "Hz"
  param :volume2    , "Volume 2"     , 1.0,  "dB"

  attr_reader :phase1, :phase2, :scaler, :note_is_on, :current_delta
  
  NUM_OUTPUTS = 2
  
  def initialize(wrapper)
    super(wrapper)
    setNumInputs(0)  # no input
    setNumOutputs(NUM_OUTPUTS) # 2 outputs, 1 for each oscillator
    canProcessReplacing(true)
    isSynth(true)

    @phase1 = 0.0
    @phase2 = 0.0
    @scaler = WAVE_SIZE / 44100.0	# TODO - can we retrieve the sample rate here ?
    @note_is_on = false
    @current_delta = 0

    suspend # what is this ?
  end
  
  def setSampleRate(sample_rate)
    super(sample_rate)
    @scaler = WAVE_SIZE.to_f / sample_rate
  end

  def getOutputProperties(index)
    ret = nil

    if index < NUM_OUTPUTS
      ret = VSTPinProperties.new
      ret.setLabel("jVSTx #{index+1}d")
      ret.setFlags(VSTPinProperties.VST_PIN_IS_ACTIVE)
      if (index < 2)
	      # make channel 1+2 stereo
        ret.setFlags(ret.getFlags() | VSTPinProperties.VST_PIN_IS_STEREO)
      end
    end
    
    ret
  end
  
  def process(inputs, outputs, sampleFrames)
  end
end
