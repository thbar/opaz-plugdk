class SimplePlugin < OpazPlug
  def initialize (wrapper)
    @in11 = @in12 = @out11 = @out12 = 0.0
    @p1smooth = 1.0
    @p1filtercoeff = 0.0007
  end

  plugin "plugin-name", "product-name", "vendor-name"
  can_do "1in1out", "plugAsChannelInsert", "plugAsSend" 
  unique_id "ToDo" # MUST be a 4 char string or an int
  
  param :p1, "cutoff", 0.5, "Hz"
  param :p2, "resonance", 0.5, "Hz"
  param :p3, "max cutoff", 0.5, "Hz"
  param :p4, "fine reso", 0.7, ""

  editor SimplePluginGUI
  
  def process(inputs, outputs, sampleFrames)
    in1, out1 = inputs[0], outputs[0]
    for i in (0..sampleFrames-1)
      if i%20 == 0 
        @p1smooth = @p1filtercoeff * p1 + ( 1.0 - @p1filtercoeff) * @p1smooth;
        c = 1.0 / (Math.tan(Math::PI * ((0.001 + @p1smooth * p3) / 2.15)));
        csq = c * c;
        q = Math.sqrt(2.0) * (1 - p2) * (1 - p4);
        a0 = 1.0 / (1.0 + (q * c) + csq);
        a1 = 2.0 * a0;
        a2 = a0;
        b1 = (2.0 * a0) * (1.0 - csq);
        b2 = a0 * (1.0 - (q * c) + csq);
      end
      out1[i] = in1[i] * a0 + @in11 * a1 + @in12 * a2 - @out11 * b1 - @out12 * b2;
      @out12 = @out11; @out11 = out1[i]; @in12 = @in11; @in11 = in1[i];
    end
  end
  
end


# cutoff sweep
for i in (1..10); PLUG.setParameter(0,i/10.0); sleep 1; end
# param randomizer
for i in (0..4); PLUG.setParameter(i,rand); end

# gui
PLUG.editor_instance.frame.getContentPane.setLayout(java.awt.FlowLayout.new)
include_class 'jvst.wrapper.gui.RotaryKnobPlusText'

knob1 = RotaryKnobPlusText.new("cutoff")
PLUG.editor_instance.frame.add knob1
l1 = javax.swing.event.ChangeListener.impl { |method, *args| 
        PLUG.setParameterAutomated(0, args[0].source.value) }
knob1.getKnob.addChangeListener(l1)
PLUG.editor_instance.frame.pack

knob2 = RotaryKnobPlusText.new("reso")
knob3 = RotaryKnobPlusText.new("fine cutoff")
knob4 = RotaryKnobPlusText.new("fine reso")
PLUG.editor_instance.frame.add knob2
PLUG.editor_instance.frame.add knob3
PLUG.editor_instance.frame.add knob4
l2 = javax.swing.event.ChangeListener.impl { |method, *args| 
        PLUG.setParameterAutomated(1, args[0].source.value) }
l3 = javax.swing.event.ChangeListener.impl { |method, *args| 
        PLUG.setParameterAutomated(2, args[0].source.value) }
l4 = javax.swing.event.ChangeListener.impl { |method, *args| 
        PLUG.setParameterAutomated(3, args[0].source.value) }
knob2.getKnob.addChangeListener(l2)
knob3.getKnob.addChangeListener(l3)
knob4.getKnob.addChangeListener(l4)
PLUG.editor_instance.frame.pack


# slider value progression
for i in (1..10); knob1.getKnob.setValue(i/10.0); sleep 1; end
for i in (1..4); eval "knob#{i}.getKnob.setValue(rand)"; end


knob5 = RotaryKnobPlusText.new("volume")
PLUG.editor_instance.frame.add knob5
l5 = javax.swing.event.ChangeListener.impl { |method, *args| 
        PLUG.setParameterAutomated(4, args[0].source.value) }
knob5.getKnob.addChangeListener(l5)
PLUG.editor_instance.frame.pack


IRB.save_session


