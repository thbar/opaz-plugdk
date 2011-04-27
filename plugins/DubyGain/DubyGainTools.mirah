class DubyGainTools
  def self.process(inBuffer:float[], outBuffer:float[], sampleFrames:int, gain:float)
    i = 0
    while i < sampleFrames
      outBuffer[i] = inBuffer[i] * gain
      i += 1
    end
  end
end
