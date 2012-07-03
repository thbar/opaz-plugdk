include_class Java::MirahGainTools

class MirahGain < OpazPlug
  plugin "MirahGain", "Opaz", "LoGeek"
  can_do "1in1out", "plugAsChannelInsert", "plugAsSend"
  unique_id "MGaN"
  
  param :gain, "Gain", 1.0, "dB"

  def process(inputs, outputs, sampleFrames)
    MirahGainTools.process(inputs[0], outputs[0], sampleFrames, gain)
  end
end
