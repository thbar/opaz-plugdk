class RubyFreeComp < OpazPlug
  plugin "FreeComp", "Opaz", "LoGeek"
  can_do "1in1out", "plugAsChannelInsert", "plugAsSend"
  unique_id "rfcp"

  param :threshold, "Threshold",  0, "dB",  (-60.0..6.0)
  param :ratio,     "Ratio",      1, "n:1", (1.0..100.0)
  param :attack,    "Attack",    20, "ms",  (0.0..250.0)
  param :release,   "Release",  200, "ms",  (25.0..2500.0)
  param :output,    "Output",     0, "dB",  (0.0..30.0)

  def init
    @final_gain = 1.0
    @env = 0.0
  end
  
  def slider
    @env_rel = Math.exp(-1/(0.25*release*sample_rate))
    @thresh = 10**(threshold/20.0)
    @transA = (1/ratio) - 1
    @transB = 10**(output/20.0) * (@thresh ** (1-(1/ratio)))
    @output_gain = 10**(output/20.0)
    @att_coef = Math.exp(-1 / (attack/1000.0*sample_rate))
    @rel_coef = Math.exp(-1 / (release/1000.0*sample_rate))    
  end

  # mono version to get started
  def sample(spl0) #, spl1)
    det = spl0.abs # instead of [spl0.abs, spl1.abs].max
    det += 10e-30

    @env = det >= @env ? det : det+@env_rel*(@env-det)    
    gain = @env > @thresh ? (@env**@transA)*@transB : @output_gain

    @final_gain = gain < @final_gain ? gain+@att_coef*(@final_gain-gain) : gain+@rel_coef*(@final_gain-gain)
    spl0 *= @final_gain
    #spl1 *= final_gain
    #[spl0, spl1]
    spl0
  end
  
  def process(inputs, outputs, sampleFrames)
    # todo - optimize this by avoiding calling them too often
    init if @final_gain.nil?
    slider

    # todo - work with stereo
    inBuffer, outBuffer = inputs[0], outputs[0]
    for i in (0..sampleFrames-1)
      outBuffer[i] = sample(inBuffer[i])
    end
  end
end
