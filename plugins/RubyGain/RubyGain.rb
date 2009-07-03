
include_class 'IRBPluginGUI'

class MyEditor
  attr_reader :frame
  def initialize(frame)
    @frame = frame
    frame.setTitle("Hello from DefaultEditor!")
    frame.setSize(400, 300)
    
    # spawn and attach an IRB session alongside the plugin GUI
    irb = IRBPluginGUI.new(JRuby.runtime)
  end
end

class RubyGain < OpazPlug
  plugin "RubyGain", "Opaz", "LoGeek"
  can_do "1in1out", "plugAsChannelInsert", "plugAsSend"
  unique_id 9876549
  
  param :gain, "Gain", 1.0

  editor MyEditor
  
  def process(inputs, outputs, sampleFrames)
    inBuffer, outBuffer = inputs[0], outputs[0]
    for i in (0..sampleFrames-1)
      outBuffer[i] = inBuffer[i] * gain
    end
  end
end
