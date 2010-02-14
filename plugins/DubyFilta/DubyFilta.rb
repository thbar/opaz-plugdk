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
  
  def use_low_pass?
    mode < 0.5 ? 0 : 1 # TODO - implement boolean params instead
  end
  
  def process(inputs, outputs, sampleFrames)
    filter.recompute_parameters(cut_off, resonance, use_low_pass?, sample_rate)
    filter.apply(inputs, outputs, sampleFrames)
  end
end
