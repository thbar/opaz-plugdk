class FreeComp < OpazPlug
  plugin "FreeComp", "Opaz", "LoGeek"
  can_do "1in1out", "plugAsChannelInsert", "plugAsSend"
  unique_id "fcmp"

  param :threshold, "Threshold",  0, "dB",  (-60..6)
  param :ratio,     "Ratio",      1, "n:1", (1..100)
  param :attack,    "Attack",    20, "ms",  (0..250)
  param :release,   "Release",  200, "ms",  (25..2500)
  param :output,    "Output",     0, "dB",  (0..30)

  def process(inputs, outputs, sampleFrames)
  end
end
