
include_class 'IRBPluginGUI'

class SimplePluginGUI
  attr_reader :frame, :plugin
  def initialize(plugin, frame)
    @frame = frame
    @plugin = plugin
    
    # spawn and attach an IRB session alongside the plugin GUI
    # comment out if you are done debugging
    # note: stdout (puts, etc.) is redirected to the IRB session
    irb = IRBPluginGUI.new(JRuby.runtime)
    
    frame.setTitle("SimplePlugin GUI")
    frame.setSize(200, 120)  
  end
  
  # Check RubyGain for a more elaborate GUI example
end



class SimplePlugin < OpazPlug
  plugin "plugin-name", "product-name", "vendor-name"
  can_do "1in1out", "plugAsChannelInsert", "plugAsSend" #our plug-in's capabilities: check original VST SDK
  unique_id "ToDo" # MUST be a 4 char string or an int
  
  ## define plugin parameters like so
  param :gain, "Gain", 0.8, "dB"

  ## define the name of the GUI class (defined above)
  editor SimplePluginGUI
  
  ## heres where the sound is generated
  ## variable input and output are arrays of arrays of floats (floats range between 0.0 and 1.0 inclusive)
  def process(inputs, outputs, sampleFrames)
    inBuffer, outBuffer = inputs[0], outputs[0]
    for i in (0..sampleFrames-1)
      outBuffer[i] = inBuffer[i] * gain
    end
  end
  
end
 