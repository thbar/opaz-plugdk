include_class Java::HybridGainTools

class HybridGain < OpazPlug
  plugin "HybridGain", "Opaz", "LoGeek"
  can_do "1in1out", "plugAsChannelInsert", "plugAsSend"
  unique_id 9876549
  
  param :gain, "Gain", 1.0

  def process(inputs, outputs, sampleFrames)
    HybridGainTools.applyGain(inputs[0], outputs[0], sampleFrames, gain)
  end
end
