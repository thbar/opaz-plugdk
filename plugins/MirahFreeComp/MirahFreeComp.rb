include_class Java::MirahFreeCompTools

class MirahFreeComp < OpazPlug
  plugin "MirahFreeComp", "Opaz", "LoGeek"
  can_do "1in1out", "plugAsChannelInsert", "plugAsSend"
  unique_id "mfcp"

  param :threshold, "Threshold",  0, "dB",  (-60.0..6.0)
  param :ratio,     "Ratio",      1, "n:1", (1.0..100.0)
  param :attack,    "Attack",    20, "ms",  (0.0..250.0)
  param :release,   "Release",  200, "ms",  (25.0..2500.0)
  param :output,    "Output",     0, "dB",  (0.0..30.0)

  def tools
    @tools ||= MirahFreeCompTools.new
  end
  
  def process(inputs, outputs, sampleFrames)
    tools.slider(threshold,ratio,attack,release,output,sample_rate)
    tools.process(inputs[0],outputs[0], sampleFrames)
  end
end
