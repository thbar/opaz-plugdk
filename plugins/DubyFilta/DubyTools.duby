# doesn't compile to .java currently - I need to figure out how to keep everything in floats, or to convert from double to float from Duby
=begin
Method []=(Type(int), Type(double)) on Type(float array) not found
Method []=(Type(int), Type(double)) on Type(float array) not found
Method []=(Type(int), Type(double)) on Type(float array) not found
/Users/thbar/.rvm/gems/ruby/1.8.7/gems/duby-0.0.1/lib/duby/typer.rb:334:in `resolve': Could not infer typing for nodes: (Duby::Typer::InferenceError)
  Call([]=) at line 50 (child of Body)
  Call([]=) at line 45 (child of Body)
  Call([]=) at line 46 (child of Body)
=end

class DubyTools

  def not_below(val:float,min:float)
    val < min ? val : min
  end
  
  def recompute_parameters(cutoff:float, resonance:float, mode:int, sampleRate:float)
    @r = not_below( (1-resonance) * 1.0, 0.1 )
    @f = not_below( cutoff * sampleRate / 4, 40.0 )
    
    # temp : hardcode PI, as I don't know how to access Math.PI from Duby
    pi = 3.141592653589793
    
    if (mode == 0) # low pass
      @c = 1 / Math.tan(pi * @f / sampleRate)

			@a1 = 1 / ( 1 + @r * @c + @c * @c)
			@a2 = 2* @a1
			@a3 = @a1
			@b1 = 2 * ( 1 - @c*@c) * @a1
			@b2 = ( 1 - @r * @c + @c * @c) * @a1
    else # hi pass
			@c = Math.tan(pi * @f / sampleRate)

			@a1 = 1 / ( 1 + @r * @c + @c * @c)
			@a2 = -2*@a1
			@a3 = @a1
			@b1 = 2 * ( @c*@c - 1) * @a1
			@b2 = ( 1 - @r * @c + @c * @c) * @a1
    end
  end
  
  def apply(input0:float[], output0:float[], sampleFrames:int)
    output0[0] = @a1 * input0[0] + @a2 * @ih1_1 + @a3 * @ih1_2 - @b1*@oh1_1 - @b2*@oh1_2
		output0[1] = @a1 * input0[1] + @a2 * input0[0] + @a3 * @ih1_1 - @b1*output0[0] - @b2*@oh1_1

		sample = 2
		while sample < sampleFrames
			output0[sample] = @a1*input0[sample] + @a2*input0[sample-1] + @a3*input0[sample-2] - @b1*output0[sample-1] - @b2*output0[sample-2]
			sample += 1
	  end

		@ih1_1 = input0[sampleFrames-1]
		@ih1_2 = input0[sampleFrames-2]
		@oh1_1 = output0[sampleFrames-1]
		@oh1_2 = output0[sampleFrames-2]
  end
 
end
