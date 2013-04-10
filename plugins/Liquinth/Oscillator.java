
package jvst.examples.liquinth;

class Oscillator {
	private static final int NUM_TABLES = 8;
	private static final int A8_TABLE_PARTS = 6;
	private static final int A5_TABLE_INDEX = 3;
	private static final int WAVE_LEN = 1 << 11;
	private static final int WAVE_MASK = WAVE_LEN - 1;
	private static final int PHASE_MASK = ( WAVE_LEN << Maths.FP_SHIFT ) - 1;

	private static short[][] saw_table, sqr_table;

	private final int a5_pitch;
    private int waveform;
    private int table;
    private int phase;
	private int ampl_1, ampl_2, step_1, step_2;

	public Oscillator( int sampling_rate ) {
		if( saw_table == null ) saw_table = wavetable( true );
		if( sqr_table == null ) sqr_table = wavetable( false );
		int a5_step = 440 * ( ( WAVE_LEN << Maths.FP_SHIFT ) / sampling_rate );
		a5_pitch = Maths.log2( a5_step );
	}

	public void set_wave( int wave ) {
		waveform = wave & Maths.FP_MASK;
	}

	public void set_amplitude( int amplitude, boolean now ) {
		ampl_2 = amplitude;
		if( now ) ampl_1 = ampl_2;
	}

	/* Pitch is in octaves relative to 440hz (A5)*/
	public void set_pitch( int pitch, boolean now ) {
		table = A5_TABLE_INDEX - ( pitch >> Maths.FP_SHIFT );
		if( table < 0 ) table = 0;
		if( table >= NUM_TABLES ) table = NUM_TABLES - 1;
		step_2 = Maths.exp2( a5_pitch + pitch );
		if( now ) step_1 = step_2;
	}

	public int get_phase() {
		return phase;
	}

	public void set_phase( int phase ) {
		this.phase = phase & PHASE_MASK;
	}
	
	public void get_audio( int[] out_buf, int offset, int length ) {	
		int step = step_1;
		int dstp = ( step_2 - step_1 ) / length;
		int ampl = ampl_1;
		int damp = ( ampl_2 - ampl_1 ) / length;

		int sqr_amp = waveform;
		int saw_amp = Maths.FP_ONE - sqr_amp;
		int phase = this.phase;
		short[] saw_t = saw_table[ table ];
		short[] sqr_t = sqr_table[ table ];
		for( int end = offset + length; offset < end; offset++ ) {
			int x = phase >> Maths.FP_SHIFT;
			int saw = saw_t[ x ] * saw_amp >> Maths.FP_SHIFT;
			int sqr = sqr_t[ x ] * sqr_amp >> Maths.FP_SHIFT;
			out_buf[ offset ] += ( saw + sqr ) * ampl >> Maths.FP_SHIFT;
			phase = ( phase + step ) & PHASE_MASK;
			ampl += damp;
			step += dstp;
		}
		this.phase = phase;
		step_1 = step_2;
		ampl_1 = ampl_2;
	}
	
	private static short[][] wavetable( boolean saw ) {
		double[] buffer = new double[ WAVE_LEN ];
		short[][] output = new short[ NUM_TABLES ][ WAVE_LEN ];
		double[] sin = new double[ WAVE_LEN ];
		double t = 0, dt = 2 * Math.PI / WAVE_LEN;
		for( int idx = 0; idx < WAVE_LEN; idx++, t += dt ) {
			sin[ idx ] = Math.sin( t );
		}
		int inc = saw ? 1 : 2;
		int part = 1, parts = A8_TABLE_PARTS;
		for( int tab = 0; tab < NUM_TABLES; tab++, parts <<= 1 ) {
			while( part <= parts ) {
				double amp = 2.0 / ( Math.PI * part );
				for( int idx = 0; idx < WAVE_LEN; idx++ ) {
					buffer[ idx ] += sin[ ( idx * part ) & WAVE_MASK ] * amp;
				}
				part += inc;
			}
			short[] out = output[ tab ];
			for( int idx = 0; idx < WAVE_LEN; idx++ ) {
				out[ idx ] = ( short ) ( buffer[ idx ] * 27000 );
			}
		}
		return output;
	}
}
