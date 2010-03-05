if __FILE__ == $0
  # bootstrap the environment
  require 'java'
  Dir['../../libs/*.jar'].each { |f| require f }
  require 'OpazSupport'
  $CLASSPATH << '../../src'
  $LOAD_PATH << '../../src'
  require 'opaz_plug'
  include_class 'javax.swing.JFrame'
end

include_class 'java.awt.BorderLayout'
include_class 'SceneToJComponent'
include_class 'JRubyVSTPluginProxy'

class MyRubyGUI
  def initialize(plugin, frame)
    @frame = frame
    @plugin = plugin

    scene = SceneToJComponent.loadScene("ToneMatrixGUI")
    frame.setTitle("The Tone Matrix")
    
    frame.setSize(300,300)
    frame.setResizable(false)
    frame.setLayout(BorderLayout.new(10,10))
    frame.add(scene, BorderLayout::CENTER)
  end
end

class ToneMatrix < OpazPlug
  plugin "ToneMatrix", "Opaz", "LoGeek"
  can_do "receiveVstEvents", "receiveVstMidiEvent"
  unique_id "tnmx"
  
  param :gain, "Gain", 1.0, "dB"
  
  editor MyRubyGUI

  def initialize(wrapper)
    super(wrapper)
    setNumInputs(0)
    setNumOutputs(1)
    canProcessReplacing(true)
    isSynth(true)
    suspend # what is this ?
  end

  def processEvents(events)
    1
  end
  
  def process(inputs, outputs, sampleFrames)
  end
end

if __FILE__ == $0
  frame = javax.swing.JFrame.new("Window")
  
  # next stuff fails
  MyRubyGUI.new(nil, frame)
  
#  label = javax.swing.JLabel.new("Hello")
#  frame.getContentPane.add(label)
#  frame.setDefaultCloseOperation(javax.swing.JFrame::EXIT_ON_CLOSE)
#  frame.pack
  frame.setVisible(true)
end


