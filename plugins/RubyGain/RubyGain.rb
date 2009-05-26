class RubyGain < OpazPlug
  plugin "RubyGain", "Opaz", "LoGeek"
  can_do "1in1out", "plugAsChannelInsert", "plugAsSend"
  unique_id 9876549
  
  param :gain, "Gain", 1.0

  def process(inputs, outputs, sampleFrames)
    inBuffer, outBuffer = inputs[0], outputs[0]
    for i in (0..sampleFrames-1)
      outBuffer[i] = inBuffer[i] * gain
    end
  end
end
