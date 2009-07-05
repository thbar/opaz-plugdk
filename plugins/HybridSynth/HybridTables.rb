module HybridTables
  WAVE_SIZE = 4096      # samples (must be power of 2 here)
  NUM_FREQUENCIES = 128 # 128 midi notes
  
  def sawtooth
    @sawtooth ||= (0..WAVE_SIZE-1).map { |i| -1.0 + (2.0 * i.to_f / WAVE_SIZE) }
  end
  
  def pulse
    @pulse ||= (0..WAVE_SIZE-1).map { |i| i < WAVE_SIZE / 4 ? -1.0 : 1.0 }
  end
  
  def frequency_table
    @frequency_table ||= begin
      k = 1.059463094359 # 12th root of 2
    	a = 6.875*k*k*k
    	
    	result = []
    	for i in (0..NUM_FREQUENCIES-1)
    	  result[i] = a; a *= k
    	end
    	result
    end
  end
end
