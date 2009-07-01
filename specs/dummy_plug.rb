class DummyEditor
end

class DummyPlug < OpazPlug
  plugin "DummyPlug", "Opaz", "LoGeek"
  can_do "1in1out", "plugAsChannelInsert", "plugAsSend"
  unique_id "opaz"
  
  editor DummyEditor
  
  # one param without unit specified (should be blank)
  param :cut_off,   "Cut Off",         1.0
  # one pram with unit specified
  param :resonance, "Resonance",       0.1,  "resograms"

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

