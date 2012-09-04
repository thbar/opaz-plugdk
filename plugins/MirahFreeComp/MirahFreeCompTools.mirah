class MirahFreeCompTools
  def initialize
    @final_gain = 1.0
    @env = 0.0
  end
  
  def slider(threshold:float,ratio:float,attack:float,release:float,output:float,sample_rate:int)
    @env_rel = Math.exp(-1/(0.25*release*sample_rate))
    @thresh = Math.pow(10,threshold/20.0)
    @transA = (1/ratio) - 1
    @transB = Math.pow(10,output/20.0) * Math.pow(@thresh,1-(1/ratio))
    @output_gain = Math.pow(10,output/20.0)
    @att_coef = Math.exp(-1 / (attack/1000.0*sample_rate))
    @rel_coef = Math.exp(-1 / (release/1000.0*sample_rate))    
  end
  
  def sample(spl0:float)
    det = Math.abs(spl0) # instead of [spl0.abs, spl1.abs].max
    det += float(Math.pow(10,-29)) # cannot use 10e-30 currently

    @env = det >= @env ? det : float(det+@env_rel*(@env-det))    
    gain = @env > @thresh ? Math.pow(@env,@transA)*@transB : @output_gain

    @final_gain = float(gain < @final_gain ? gain+@att_coef*(@final_gain-gain) : gain+@rel_coef*(@final_gain-gain))
    spl0 *= float(@final_gain)
    #spl1 *= final_gain
    #[spl0, spl1]
    spl0
  end
  
  def process(inBuffer:float[], outBuffer:float[], sampleFrames:int)
    i = 0
    while i < sampleFrames
      outBuffer[i] = sample(inBuffer[i])
      i += 1
    end
  end
end