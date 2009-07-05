require 'HybridTables'

# TODOS:
# - allow to tweak getParameterDisplay from the plugin
#    for instance here we need to display Sawtooth for 0.0 or Pulse for 1.0 (waveform1)
#    and volume strings in Db using dbToString
class HybridSynth < OpazPlug
  include HybridTables
  
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

  attr_accessor :phase1, :phase2, :scaler, :note_is_on
  attr_accessor :current_note, :current_velocity, :current_delta
  
  NUM_OUTPUTS = 2
  
  def initialize(wrapper)
    super(wrapper)
    setNumInputs(0)  # no input
    setNumOutputs(NUM_OUTPUTS) # 2 outputs, 1 for each oscillator
    canProcessReplacing(true)
    isSynth(true)

    phase1 = 0.0
    phase2 = 0.0
    scaler = WAVE_SIZE / 44100.0	# TODO - can we retrieve the sample rate here ?
    note_is_on = false
    current_delta = 0

    suspend # what is this ?
  end
  
  def setSampleRate(sample_rate)
    super(sample_rate)
    scaler = WAVE_SIZE.to_f / sample_rate
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

  def note_on(note, velocity, delta)
    log("Note #{note}, velocity #{velocity}")
    note_is_on = true
    current_note = note
    current_velocity = velocity
    current_delta = delta
    phase1 = phase2 = 0
  end
  
  def note_off
    log("Note off")
    note_is_on = false
  end
  
  # TODO - create an "each" friendly wrapper around VSTEvents ?
  def processEvents(ev)
    log("processEvents")
    for i in (0..ev.getNumEvents()-1)
      log("looping... #{i}")
      next if (ev.getEvents()[i].getType() != VSTEvent.VST_EVENT_MIDI_TYPE)

      event = ev.getEvents()[i]
      midiData = event.getData()
      status = midiData[0] & 0xf0 # ignore channel

      log("status: 0x#{status.to_s(16)}")
      if (status == 0x90 || status == 0x80)
        # we only look at notes
        note = midiData[1] & 0x7f
        velocity = midiData[2] & 0x7f
        velocity = 0 if status == 0x80 # note off by velocity 0

        if (velocity==0 && (note == current_note))
          note_off
        else
          note_on(note, velocity, event.getDeltaFrames())
        end
      elsif (status == 0xb0)
        # all notes off
        if (midiData[1] == 0x7e || midiData[1] == 0x7b)
          note_off
        end
      end
    end
    1 # want more
  end
  
  def process(inputs, outputs, sampleFrames)
  end
end
