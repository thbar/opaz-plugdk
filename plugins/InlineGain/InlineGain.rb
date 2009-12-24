
require 'java_inline'

class InlineGain < OpazPlug
  #make stereo
  def initialize(wrapper)
    super(wrapper)
    setNumInputs(2)
    setNumOutputs(2)
  end

  plugin "InlineGain", "Opaz", "daniel309"
  can_do "2in2out", "plugAsChannelInsert", "plugAsSend" #stereo (2in2out)
  unique_id "IlG1"
  
  param :gain, "Gain", 1.0
  
  def process(inputs, outputs, sampleFrames)
    process_gain(inputs, outputs, sampleFrames, gain)
  end
  
  # inline java code here
  inline :Java do |builder|
    builder.java "
      public static void process_gain(float[][] ins, float[][] outs, int frames, float gain) {
        for (int i=0; i < frames; i++) {
          outs[0][i] = gain * ins[0][i];
          outs[1][i] = gain * ins[1][i];
        }
      }
    "
  end
  
end
