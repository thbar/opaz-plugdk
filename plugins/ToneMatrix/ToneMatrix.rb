require 'javafx-ui-common'
require 'javafx-ui-swing'
require 'javafxrt'

java_import 'javafx.reflect.FXLocal'
java_import 'java.lang.Thread'

#include_class 'javax.swing.JComponent'
include_class 'java.awt.BorderLayout'
#include_class 'com.sun.javafx.tk.swing.SwingScene'
include_class 'SceneToJComponent'

class MyRubyGUI
  def initialize(plugin, frame)
    puts "********* MyRubyGUI ***********"
    @frame = frame
    @plugin = plugin

    begin
      puts "1"
      scene = SceneToJComponent.loadVSTPluginScene2("ToneMatrixGUI", nil)
      puts '2'
    rescue => e
      puts "********** #{e} ************"
    end
    
    frame.setTitle("The Tone Matrix")
    frame.setSize(400, 300)
    frame.setResizable(false)
    
    frame.setLayout(BorderLayout.new(10,10))
    frame.add(scene, BorderLayout::CENTER)
    puts "********* MyRubyGUI DONE ***********"
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


