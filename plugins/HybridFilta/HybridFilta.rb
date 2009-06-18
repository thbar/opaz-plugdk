include_class Java::FilterTools # TODO: automatically include all java classes in the plugin folder ?

class HybridFilta < OpazPlug
  plugin "HybridFilta", "Opaz", "LoGeek"
  can_do "1in1out", "plugAsChannelInsert", "plugAsSend"
  unique_id "hflt"
  
  param :cut_off,   "Cut Off",         1.0
  param :resonance, "Resonance",       0.1
  param :mode,      "Mode (LP or HP)",   0

  def filter
    @filter ||= FilterTools.new
  end
  
  def use_low_pass?
    mode < 0.5 # TODO - implement boolean params instead
  end
  
  def process(inputs, outputs, sampleFrames)
    filter.recomputeParameters(cut_off, resonance, use_low_pass?, sample_rate)
    filter.apply(inputs, outputs, sampleFrames)
  end
end
