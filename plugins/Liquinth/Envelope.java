
package jvst.examples.liquinth;

class Envelope {
	private boolean key_is_on;
	private final int tick_len;
    private int amplitude;
	private int attack_len, attack_delta;
	private int release_len, release_delta;

	public Envelope( int sampling_rate ) {
		tick_len = sampling_rate / 1000;
		set_attack_time( 0 );
		set_release_time( 0 );
	}

	public void set_attack_time( int millis ) {
		attack_len = 0;
		attack_delta = 0;
		if( millis > 0 ) {
			attack_len = tick_len * millis;
			attack_delta = ( Maths.FP_ONE << Maths.FP_SHIFT ) / attack_len;
		}
	}

	public void set_release_time( int millis ) {
		release_len = 0;
		release_delta = 0;
		if( millis > 0 ) {
			release_len = tick_len * millis;
			release_delta = ( Maths.FP_ONE << Maths.FP_SHIFT ) / release_len;
		}
	}

	public boolean key_is_on() {
		return key_is_on;
	}

	public void key_on() {
		key_is_on = true;
		if( attack_len <= 0 ) {
			amplitude = Maths.FP_ONE << Maths.FP_SHIFT;
		}
	}

	public void key_off( boolean silence ) {
		key_is_on = false;
		if( release_len <= 0 || silence ) {
			amplitude = 0;
		}
	}

	public int get_amplitude() {
		return amplitude >> Maths.FP_SHIFT;
	}

	public void update( int length ) {
		int max_amplitude;
		if( key_is_on ) { /* Attack */
			max_amplitude = Maths.FP_ONE << Maths.FP_SHIFT;
			if( length >= attack_len ) {
				amplitude = max_amplitude;
			} else {
				amplitude += length * attack_delta;
				if( amplitude > max_amplitude ) {
					amplitude = max_amplitude;
				}
			}
		} else { /* Release */
			if( length >= release_len ) {
				amplitude = 0;
			} else {
				amplitude -= length * release_delta;
				if( amplitude < 0 ) {
					amplitude = 0;
				}
			}
		}
	}
}
