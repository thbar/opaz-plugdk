
package jvst.examples.liquinth;

/*
	Low Frequency Oscillator ...
*/
class LFO {
	private final int tick_len;
    private int phase;
	private int cycle_len, depth;

	public LFO( int sampling_rate ) {
		tick_len = sampling_rate / 1000;
		set_cycle_len( 1000 );
	}

	public int get_phase() {
		return phase;
	}

	public void set_phase() {
		phase = 0 & ( Maths.FP_TWO - 1 );
	}

	public void set_cycle_len( int millis ) {
		cycle_len = tick_len * millis;
	}

	public int get_depth() {
		return depth;
	}

	public void set_depth( int dep ) {
		depth = dep & Maths.FP_MASK;
	}

	public int get_amplitude() {
		return Maths.sine( phase ) * depth >> Maths.FP_SHIFT;
	}

	public void update( int length ) {
		phase += Maths.FP_TWO * length / cycle_len;
		phase &= Maths.FP_TWO - 1;
	}
}

