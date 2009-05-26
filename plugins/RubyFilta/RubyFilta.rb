class RubyFilta < OpazPlug
  plugin "OpazFilta", "Opaz", "LoGeek"
  can_do "1in1out", "plugAsChannelInsert", "plugAsSend"
  unique_id 9876544
  
  param :cut_off,   "Cut Off",         1.0
  param :resonance, "Resonance",       0.1
  param :mode,      "Mode (LP or HP)",   0

  def process(inputs, outputs, sampleFrames)
    inBuffer = inputs[0]
    outBuffer = outputs[0]
    for i in (0..sampleFrames-1)
      outBuffer[i] = inBuffer[i]
    end
  end
end
