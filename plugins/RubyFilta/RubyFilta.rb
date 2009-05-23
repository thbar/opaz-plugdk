require 'opaz_plug'

# work in progress - creating the DSL allowing easy plugin and params manipulation

class RubyFilta < OpazPlug
  plugin "OpazFilta", "Opaz", "LoGeek"

  can_do "1in1out", "plugAsChannelInsert", "plugAsSend"
  
  param :cut_off,   "Cut Off",         1.0
  param :resonance, "Resonance",       0.1
  param :mode,      "Mode (LP or HP)",   0

  # TODO - move constructor stuff out to opaz_plug or declarative stuff
  def initialize(wrapper)
    super
    log("Starting up!")
    setNumInputs(1)
    setNumOutputs(1)
    canProcessReplacing(true)
    setUniqueID(9876544)
  end

  def processReplacing(inputs, outputs, sampleFrames)
    # dummy loop for now
    inBuffer = inputs[0]
    outBuffer = outputs[0]
    for i in (0..sampleFrames-1)
      outBuffer[i] = inBuffer[i]
    end
  end
  
end
