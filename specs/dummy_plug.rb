class DummyPlug < OpazPlug
  plugin "DummyPlug", "Opaz", "LoGeek"
  can_do "1in1out", "plugAsChannelInsert", "plugAsSend"
  unique_id 9876549
  
  param :cut_off,   "Cut Off",         1.0
  param :resonance, "Resonance",       0.1

  # for tests only
  def initialize(wrapper); end

  def process(inputs, outputs, sampleFrames)
    inBuffer = inputs[0]
    outBuffer = outputs[0]
    for i in (0..sampleFrames-1)
      outBuffer[i] = inBuffer[i]
    end
  end
end

