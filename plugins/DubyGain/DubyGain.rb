# the following line fails
include_class Java::DubyGainTools

class DubyGain < OpazPlug
  plugin "DubyGain", "Opaz", "LoGeek"
  can_do "1in1out", "plugAsChannelInsert", "plugAsSend"
  unique_id "DGaN"
  
  param :gain, "Gain", 1.0, "dB"

  def process(inputs, outputs, sampleFrames)
    DubyGainTools.process(inputs[0], outputs[0], sampleFrames, gain)
  end
end
