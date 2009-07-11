public class HybridSynthTools {
	public boolean noteIsOn;
  public int currentNote;
  public int currentVelocity;
  public int currentDelta;
  public float phase1, phase2;
	public float scaler;
	
	public int waveSize;
	public float[] sawtooth, pulse, frequency_table;
	
	private static final double MIDI_SCALER = 1.0D / 127.0D;
  
	public HybridSynthTools() {
		this.noteIsOn = false;
		this.currentDelta = 0;
		this.phase1 = this.phase2 = 0;
	}
	
	public void note_on(int note, int velocity, int delta) {
	  this.noteIsOn = true;
	  this.currentNote = note;
	  this.currentVelocity = velocity;
	  this.currentDelta = delta;
	  this.phase1 = this.phase2 = 0;
	}

	public void note_off() {
    this.noteIsOn = false;
  }
	
	public void zeroFill(float[][] outputs, int count) {
		float[] output0 = outputs[0];
		float[] output1 = outputs[1];
		for(int i = 0; i < count; i++) {
			output0[i] = 0;
			output1[i] = 0;
		}
	}
	
	public void computeWave(float[][] outputs, float[] wave1, float[] wave2, int mask, int start, float freq1, float freq2, float vol, float volume1, float volume2) {
		float[] output0 = outputs[0];
		float[] output1 = outputs[1];
		for (int i = start, j=output0.length; i < j; i++) {
			// this is all very raw, there is no means of interpolation,
			// and we will certainly get aliasing due to non-bandlimited
			// waveforms. don't use this for serious projects...
			output0[i] = wave1[(int)phase1 & mask] * volume1 * vol;
			output1[i] = wave2[(int)phase2 & mask] * volume2 * vol;
			this.phase1 += freq1;
			this.phase2 += freq2;
		}
	}

	public void processReplacing(
		float[][] inputs, float[][] outputs, int sampleFrames,
		float volume, 
		float volume1, float volume2, 
		float frequency1, float frequency2,
		float waveform1, float waveform2) {
		if (this.noteIsOn) {
			float[] out1 = outputs[0];
			float[] out2 = outputs[1];
			float baseFreq = this.frequency_table[this.currentNote & 0x7f] * this.scaler;
			float freq1 = baseFreq + frequency1;	// not really linear...
			float freq2 = baseFreq + frequency2;
			float wave1[] = (waveform1 < .5) ? sawtooth : pulse;
			float wave2[] = (waveform2 < .5) ? sawtooth : pulse;
			float wsf = (float)waveSize;
			float vol = (float)(volume * (double)this.currentVelocity * MIDI_SCALER);
			int mask = waveSize - 1;

			int start = 0;

			if (this.currentDelta > 0) {
				if (this.currentDelta >= sampleFrames) { //future
					this.currentDelta -= sampleFrames;
					return;
				}
				zeroFill(outputs, this.currentDelta);
				start = this.currentDelta;
				sampleFrames -= this.currentDelta;
				this.currentDelta = 0;
			}

			computeWave(outputs, wave1, wave2, mask, start, freq1, freq2, vol, volume1, volume);
		}
		else {
			zeroFill(outputs, outputs[0].length); //note off
		}
	}
}