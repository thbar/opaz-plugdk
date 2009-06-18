// ahh, the irony. Java is now the assembler of JRuby.
// this code has been inspired by existing code, then translated to C# and Java.
// sorry I cannot credit the original author.
public class FilterTools {
	float f,r,c,a1,a2,a3,b1,b2;
	
	public void recomputeParameters(float cutoff, float resonance, boolean lowPassMode, float sampleRate) {
		r = (1-resonance) * 10f; // r  = rez amount, from sqrt(2) to ~ 0.1
		if (r<0.1f) 
			r=0.1f;
		f = cutoff * sampleRate/4; // replace 44100 by context.samplerate later (or /4 ?)
		if (f<40f) 
			f=40f;

		if (lowPassMode) // lowpass mode
		{
			c = (float)(1.0 / Math.tan(Math.PI * f / sampleRate));

			a1 = 1.0f / ( 1.0f + r * c + c * c);
			a2 = 2* a1;
			a3 = a1;
			b1 = 2.0f * ( 1.0f - c*c) * a1;
			b2 = ( 1.0f - r * c + c * c) * a1;
		}
		else // hipass mode
		{
			c = (float)Math.tan(Math.PI * f / sampleRate);

			a1 = 1.0f / ( 1.0f + r * c + c * c);
			a2 = -2*a1;
			a3 = a1;
			b1 = 2.0f * ( c*c - 1.0f) * a1;
			b2 = ( 1.0f - r * c + c * c) * a1;
		}		
	}
	
	// history elements ih(x)_(y) : input sample n-y on voice x, oh(x)_y : output sample n-y on voice x
	float ih1_1=0,ih1_2=0,oh1_1=0,oh1_2=0;

	float computeFilter(float inp0,float inp1,float inp2,float outp1,float outp2)
	{
		return a1 * inp0 + a2 * inp1 + a3 * inp2 - b1*outp1 - b2*outp2;
	}

	public void apply(float[][] inputs, float[][] outputs, int sampleFrames) {
		// one channel only for the moment
		float[] input0 = inputs[0];
		float[] output0 = outputs[0];

 		// TODO: move the whole following code to some more functional-styled code, if possible
		output0[0] = computeFilter( input0[0], ih1_1, ih1_2, oh1_1,oh1_2);
		output0[1] = computeFilter( input0[1], input0[0], ih1_1, output0[0], oh1_1);
		for (int sample=2;sample<sampleFrames;sample++)
			output0[sample] = a1*input0[sample] + a2*input0[sample-1] + a3*input0[sample-2] - b1*output0[sample-1] - b2*output0[sample-2];

		// save history
		ih1_1 = input0[sampleFrames-1];
		ih1_2 = input0[sampleFrames-2];
		oh1_1 = output0[sampleFrames-1];
		oh1_2 = output0[sampleFrames-2];
	}
}
