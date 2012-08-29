if __FILE__ == $0
  # bootstrap the environment
  #noinspection RubyResolve
  require 'java'
  #noinspection RubyResolve
  Dir['../../libs/*.jar'].each { |f| require f }
  #noinspection RubyResolve
  require 'OpazSupport'
  #noinspection RubyResolve
  $CLASSPATH << '../../src'
  $LOAD_PATH << '../../src'
  #noinspection RubyResolve
  require 'opaz_plug'
  include_class 'javax.swing.JFrame'
end

include_class 'java.awt.BorderLayout'
include_class 'SceneToJComponent'
include_class 'JRubyVSTPluginProxy'

#noinspection RubyResolve
class MyRubyGUI
  def initialize(plugin, frame)
    @frame = frame
    @plugin = plugin

    scene = SceneToJComponent.loadScene("ToneMatrixGUI")
    frame.setTitle("The Tone Matrix")

    frame.setSize(300, 300)
    frame.setResizable(false)
    frame.setLayout(BorderLayout.new(10, 10))
    frame.add(scene, BorderLayout::CENTER)
  end
end

#noinspection ALL
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

  #noinspection RubyInstanceMethodNamingConvention
  def processEvents(events)
    1
  end

  def process(inputs, outputs, sampleFrames)
  end
end

if __FILE__ == $0
  #noinspection RubyResolve
  frame = javax.swing.JFrame.new("Window")

  # next stuff fails
  MyRubyGUI.new(nil, frame)

#  label = javax.swing.JLabel.new("Hello")
#  frame.getContentPane.add(label)
#  frame.setDefaultCloseOperation(javax.swing.JFrame::EXIT_ON_CLOSE)
#  frame.pack
#noinspection RubyResolve
  frame.setVisible(true)
end


