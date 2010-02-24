class ToneMatrix < OpazPlug
  plugin "ToneMatrix", "Opaz", "LoGeek"
  can_do "receiveVstEvents", "receiveVstMidiEvent"
  unique_id "tnmx"
  
  param :gain, "Gain", 1.0, "dB"

  def initialize(wrapper)
    super(wrapper)
    setNumInputs(0)
    setNumOutputs(1)
    canProcessReplacing(true)
    isSynth(true)
    suspend # what is this ?
  end

  def processEvents(events)
    1
  end
  
  def process(inputs, outputs, sampleFrames)
  end

end
