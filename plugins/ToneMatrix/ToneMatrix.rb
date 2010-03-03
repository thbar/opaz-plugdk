#include_class 'javax.swing.JComponent'
include_class 'java.awt.BorderLayout'
#include_class 'com.sun.javafx.tk.swing.SwingScene'
include_class 'SceneToJComponent'
include_class 'JRubyVSTPluginProxy'

# try out to achieve the loader part from JRuby - doesn't work yet (need to call .class on a Java class - conflicts appeared)
=begin
module JavaFX
  def self.load(fx_name)
    loader = Thread.currentThread.getContextClassLoader || JRubyVSTPluginProxy.getClassLoader
    loader.loadClass(fx_name).newInstance.impl_getPeer.scenePanel
  end
end
=end

class MyRubyGUI
  def initialize(plugin, frame)
    @frame = frame
    @plugin = plugin

    scene = SceneToJComponent.loadScene("ToneMatrixGUI")
    frame.setTitle("The Tone Matrix")
    frame.setSize(400, 300)
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


