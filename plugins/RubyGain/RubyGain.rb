
include_class 'IRBPluginGUI'

class RubyGainGUI
  attr_reader :frame, :plugin, :slider
  def initialize(plugin, frame)
    @frame = frame
    @plugin = plugin
    
    # spawn and attach an IRB session alongside the plugin GUI
    # comment out if you are done debugging
    # note: stdout (puts, etc.) is redirected to the IRB session
    irb = IRBPluginGUI.new(JRuby.runtime)
    
    frame.setTitle("RubyGain GUI")
    frame.setSize(400, 300)
    
    puts "gain=#{plugin.gain*100}"
    @slider = javax.swing.JSlider.new(0, 100, plugin.gain*100)
    # GUI changed param value --> update plug
    listener = javax.swing.event.ChangeListener.impl { |method, *args| 
      # cannot simply use plugin.gain = newval here, this would bypass 
      # host notification to record slider changes     
      # 
      # avoid recursion here: setParameterAutomated eventually calls RubyGainGUI.setParameter, 
      # which changes the slider value (slider.setValue(value*100))
      # which again causes a change event and we are here again. --> infinite loop
      if args[0].source.getValueIsAdjusting==false
        newval = args[0].source.value/100.0
        plugin.setParameterAutomated(0, newval)
        puts "value changed=#{newval}"
      end
    } 
    @slider.addChangeListener(listener)
    frame.add(@slider)
  end
  
  # plug changed param value --> update GUI
  def setParameter (index, value)
  puts "GUI param idx=#{index} val=#{value}"
  case index
    when 0 
      java.awt.EventQueue.invoke_later( java.lang.Runnable.impl { |method, *args| 
      slider.setValueIsAdjusting(true); #do not generate a change event
        slider.setValue(value*100)
      })
    end
  end
  
end

class RubyGain < OpazPlug
  plugin "RubyGain", "Opaz", "LoGeek"
  can_do "1in1out", "plugAsChannelInsert", "plugAsSend"
  unique_id "RGaN"
  
  param :gain, "Gain", 1.0, "nix"

  editor RubyGainGUI
  
  def process(inputs, outputs, sampleFrames)
    inBuffer, outBuffer = inputs[0], outputs[0]
    for i in (0..sampleFrames-1)
      outBuffer[i] = inBuffer[i] * gain
    end
  end
end
 