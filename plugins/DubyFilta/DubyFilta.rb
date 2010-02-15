include_class Java::DubyTools

class DubyFilta < OpazPlug
  plugin "DubyFilta", "Opaz", "LoGeek"
  can_do "1in1out", "plugAsChannelInsert", "plugAsSend"
  unique_id "dflt"
  
  param :cut_off,   "Cut Off",         1.0
  param :resonance, "Resonance",       0.1
  param :mode,      "Mode (LP or HP)",   0

  def filter
    @filter ||= DubyTools.new
  end
  
  def process(inputs, outputs, sampleFrames)
    filter.recompute_parameters(cut_off, resonance, (mode < 0.5 ? +1 : -1), sample_rate)
    filter.apply(inputs[0], outputs[0], sampleFrames)
  end
end
