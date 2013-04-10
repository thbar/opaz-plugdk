
package jvst.examples.liquinth;

class Voice {
	public static final int NUM_CONTROLLERS = 6;
	private static final String[] control_names = new String[] {
		"Waveform",
		"Attack",
		"Release",
		"Detune",
		"Vibrato Speed",
		"Vibrato Depth"
	};

	private final Oscillator osc_1;
    private final Oscillator osc_2;
	private final Envelope vol_env;
	private final LFO vib_lfo_1;
    private final LFO vib_lfo_2;

	private final int tick_len;
    private int porta_time;
	private int pitch, porta_pitch, porta_rate;
	private int volume, key, pitch_wheel, detune;
	private int amplitude;

	public static String get_controller_name( int controller ) {
		return control_names[ controller ];
	}

	public Voice( int sampling_rate ) {
		int idx;
		tick_len = sampling_rate / 1000;
		osc_1 = new Oscillator( sampling_rate );
		osc_2 = new Oscillator( sampling_rate );
		vol_env = new Envelope( sampling_rate );
		vib_lfo_1 = new LFO( sampling_rate );
		vib_lfo_2 = new LFO( sampling_rate );
		set_pitch_wheel( 0 );
		for( idx = 0; idx < NUM_CONTROLLERS; idx++ ) {
			set_controller( idx, 0 );
		}
		set_portamento_time( 0 );
		set_volume( Maths.FP_ONE );
		key_on( 60 ); /* Middle C.*/
		key_off( true );
	}

	/* Portamento speed in ms. */
	public void set_portamento_time( int millis ) {
		if( millis >= 0 ) {
			porta_time = millis;
		}
	}

	public void set_volume( int vol ) {
		volume = vol;
	}

	/* Key_on without key_off for portamento. */
	public void key_on( int key ) {
		if( key != this.key ) {
			this.key = key;
			porta_pitch = Maths.FP_ONE * ( ( key & 0x7F ) - 69 ) / 12;
			if( vol_env.key_is_on() ) {
				/* Portamento */
				porta_rate = 0;
				if( porta_time > 0 ) {
					porta_rate = ( porta_pitch - pitch ) / porta_time;
				}
				if( porta_rate == 0 ) {
					pitch = porta_pitch;
					calculate_pitch( true );
				}
			} else {
				/* Not portamento.*/
				vol_env.key_off( true );
				pitch = porta_pitch;
				calculate_pitch( true );
			} 
		}
		if( vol_env.get_amplitude() <= 0 ) {
			vib_lfo_1.set_phase();
			vib_lfo_2.set_phase();
		}
		vol_env.key_on();
		calculate_amplitude( true );
	}

	public boolean key_is_on() {
		return vol_env.key_is_on();
	}

	public int get_key() {
		return key;
	}

	public void key_off( boolean sound_off ) {
		vol_env.key_off( sound_off );
		calculate_amplitude( true );
	}

	public int get_volume() {
		return vol_env.get_amplitude();
	}

	/*
		Set pitch wheel position ( +-1 octave ).
		Value is from -8192 to 8191 inclusive.
	*/
	public void set_pitch_wheel( int value ) {
		if( 8192 > value && value >= -8192 ) {
			pitch_wheel = ( value << 18 ) >> 31 - Maths.FP_SHIFT;
			pitch_wheel /= 6;
		}
	}

	/*
		Set a modulation controller position.
		Value is from 0 to 127 inclusive.
	*/
	public void set_controller( int control_idx, int value ) {
		if( control_idx < 0 || value < 0 || value > 127 ) {
			return;
		}
		switch( control_idx ) {
			case 0:
				osc_1.set_wave( value << Maths.FP_SHIFT - 7 );
				osc_2.set_wave( value << Maths.FP_SHIFT - 7 );
				break;
			case 1:
				vol_env.set_attack_time( value << 4 );
				break;
			case 2:
				vol_env.set_release_time( value << 4 );
				break;
			case 3:
				if( value <= 0 ) {
					detune = 0;
					if( vib_lfo_1.get_depth() <= 0 ) {
						/* Lock the oscillators together.*/
						osc_2.set_phase( osc_1.get_phase() );
						calculate_pitch( true );
					}
				} else {
					value = ( value + 1 ) << Maths.FP_SHIFT - 7;
					detune = Maths.exp_scale( value, 8 );
				}
				break;
			case 4:
				value = 128 - value << Maths.FP_SHIFT - 7;
				value = Maths.exp_scale( value, 11 );
				vib_lfo_1.set_cycle_len( value );
				vib_lfo_2.set_cycle_len( value * 99 / 70 );
				break;
			case 5:
				if( value <= 0 ) {
					vib_lfo_1.set_depth( 0 );
					vib_lfo_2.set_depth( 0 );
					if( detune <= 0 ) {
						osc_2.set_phase( osc_1.get_phase() );
						calculate_pitch( true );
					}
				} else {
					value = value << Maths.FP_SHIFT - 7;
					value = Maths.exp_scale( value, 8 );
					vib_lfo_1.set_depth( value );
					vib_lfo_2.set_depth( value );
				}
				break;
		}
	}

	public void get_audio(int[] out_buf, int length) {
		vib_lfo_1.update( length );
		vib_lfo_2.update( length );
		if( pitch < porta_pitch ) {
			pitch = pitch + porta_rate * length / tick_len;
			if( pitch > porta_pitch ) {
				pitch = porta_pitch;
			}
		}
		if( pitch > porta_pitch ) {
			pitch = pitch + porta_rate * length / tick_len;
			if( pitch < porta_pitch ) {
				pitch = porta_pitch;
			}
		}
		calculate_pitch( false );
		vol_env.update( length );
		calculate_amplitude( false );
		osc_1.get_audio( out_buf, 0, length );
		osc_2.get_audio( out_buf, 0, length );
	}

	private void calculate_pitch( boolean now ) {
		int vibrato_1 = vib_lfo_1.get_amplitude();
		int vibrato_2 = vib_lfo_2.get_amplitude();
		osc_1.set_pitch( pitch + pitch_wheel + vibrato_1, now );
		osc_2.set_pitch( pitch + pitch_wheel + vibrato_2 + detune, now );
	}

	private void calculate_amplitude( boolean now ) {
		int amplitude;
		amplitude = vol_env.get_amplitude() * volume >> Maths.FP_SHIFT + 1;
		osc_1.set_amplitude( amplitude, now );
		osc_2.set_amplitude( amplitude, now );
	}

	/**
	 * @return the amplitude
	 */
	public int getAmplitude() {
		return amplitude;
	}

	/**
	 * @param amplitude the amplitude to set
	 */
	public void setAmplitude(int amplitude) {
		this.amplitude = amplitude;
	}
}
