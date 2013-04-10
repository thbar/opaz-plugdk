import jvst.wrapper.VSTPluginAdapter;

public class Liquinth implements Synthesizer, AudioSource {
	public static final String VERSION = "Liquinth a36";
	public static final String AUTHOR = "(c)2009 mumart@gmail.com";
	public static final int RELEASE_DATE = 20090916;


public class Liquinth extends VSTPluginAdapter {
	public static int      NUM_PARAMS      = 0x1;	
	public static String[] PARAM_LABELS    = new String[] { "CC Value" };
	public static String[] PARAM_NAMES     = new String[] { "MIDI CC Value" };
	public static float[]  PARAM_PRINT_MUL = new float[] { 0x7f };
	private int            currentProgram  = 0x0;

	private static final int
		LOG2_NUM_VOICES = 3,
		NUM_VOICES = 1 << LOG2_NUM_VOICES,
		NUM_CONTROLLERS = 6;

    // Some default programs
    private float[][] programs = new float[][] {
        { 0.0f }
    };

    public Liquinth(long wrapper) {
        super(wrapper);
        currentProgram = 0x0;

        // stereo plugin, for max compatibility
        this.setNumInputs(0x2);
        this.setNumOutputs(0x2);
        this.canProcessReplacing(true);                                    // mandatory for vst 2.4!
        this.setUniqueID('j' << 0x18 | 'R' << 0x10 | 'u' << 0x8 | 'b');    // jRub
        log("Construktor INVOKED!");
    }

    @Override
    public int canDo(String feature) {

        // the host asks us here what we are able to do
        int ret = CANDO_NO;

        if (feature.equals(CANDO_PLUG_2_IN_2_OUT)) {
            ret = CANDO_YES;
        }

        if (feature.equals(CANDO_PLUG_PLUG_AS_SEND)) {
            ret = CANDO_YES;
        }

        if (feature.equals(CANDO_PLUG_PLUG_AS_CHANNEL_INSERT)) {
            ret = CANDO_YES;
        }

        log("Host asked canDo: " + feature + " we replied: " + ret);

        return ret;
    }

    @Override
    public String getProductString() {
        return "Liquinth";
    }

    @Override
    public String getEffectName() {
        return "Liquinth";
    }

    @Override
    public String getProgramNameIndexed(int category, int index) {
        return "prog categ=" + category + ", idx=" + index;
    }

    @Override
    public String getVendorString() {
        return "http://jvstwrapper.sourceforge.net/";
    }

    @Override
    public boolean setBypass(boolean value) {

        // do not support soft bypass!
        return false;
    }

    @Override
    public boolean string2Parameter(int index, String value) {
        try {
            if (value != null) {
                this.setParameter(index, Float.parseFloat(value));
            }

            return true;
        } catch (Exception e) {    // ignore
            return false;
        }
    }

    public int getNumParams() {
        return NUM_PARAMS;
    }

    @Override
    public int getNumPrograms() {
        return programs.length;
    }

    @Override
    public float getParameter(int index) {
        if (index < programs[currentProgram].length) {
            return programs[currentProgram][index];
        }

        return 0.0f;
    }

    @Override
    public String getParameterDisplay(int index) {
        if (index < programs[currentProgram].length) {
            return "" + (int) (PARAM_PRINT_MUL[index] * programs[currentProgram][index]);
        }

        return "0";
    }

    @Override
    public String getParameterLabel(int index) {
        if (index < PARAM_LABELS.length) {
            return PARAM_LABELS[index];
        }

        return "";
    }

    @Override
    public String getParameterName(int index) {
        if (index < PARAM_NAMES.length) {
            return PARAM_NAMES[index];
        }

        return "param: " + index;
    }

    @Override
    public int getProgram() {
        return currentProgram;
    }

    @Override
    public String getProgramName() {
        return "program " + currentProgram;
    }

    @Override
    public void setParameter(int index, float value) {
        programs[currentProgram][index] = value;
        update();
    }

    @Override
    public void setProgram(int index) {
        currentProgram = index;
        update();
    }

    @Override
    public void setProgramName(String name) {

        // TODO: ignored
    }

    @Override
    public int getPlugCategory() {
        log("getPlugCategory");

        return PLUG_CATEG_EFFECT;
    }

    // Generate / Process the sound!
		
	private static final String[] control_names = new String[] {
		"Overdrive",
		"Filter Cutoff",
		"Filter Resonance",
		"Filter Attack Level",
		"Filter Decay",
		"Portamento Speed"
	};

	private final MoogFilter filter;
	private Envelope filter_env;
	private final Voice[] voices;
	private final byte[] key_status;
    private final byte[] controllers;
	private int num_controls;

	public Liquinth( int sampling_rate ) {
		filter = new MoogFilter( sampling_rate );
		voices = new Voice[ NUM_VOICES ];
		key_status = new byte[ 128 ];
		controllers = new byte[ get_num_controllers() ];
		set_sampling_rate( sampling_rate );
		set_controller( 0, 42 );
		set_controller( 1, 127 );
	}

	public void set_sampling_rate( int sampling_rate ) {
		int idx;
		filter_env = new Envelope( sampling_rate );
		for( idx = 0; idx < NUM_VOICES; idx++ ) {
			voices[ idx ] = new Voice( sampling_rate );
			voices[ idx ].key_on( idx );
		}
		all_notes_off( true );
		for( idx = 0; idx < controllers.length; idx++ ) {
			set_controller( idx, controllers[ idx ] );
		}
	}

	public int get_num_controllers() {
		return NUM_CONTROLLERS + Voice.NUM_CONTROLLERS;
	}

	public String get_controller_name( int control ) {
		String name;
		if( control < NUM_CONTROLLERS ) {
			name = control_names[ control ];
		} else {
			name = Voice.get_controller_name( control - NUM_CONTROLLERS );
		}
		return name;
	}

	public int[] allocate_mix_buf( int frames ) {
		return new int[ frames ];
	}

	public synchronized void get_audio( int[] out_buf, int length ) {
		int idx, cutoff, alevel;
		/* Clear mix buffer.*/
		for( idx = 0; idx < length; idx++ ) {
			out_buf[ idx ] = 0;
		}
		/* Get audio from voices. */
		for( idx = 0; idx < NUM_VOICES; idx++ ) {
			voices[ idx ].get_audio( out_buf, length );
		}
		/* Handle filter envelope.*/
		cutoff = ( controllers[ 1 ] + 1 ) << Maths.FP_SHIFT - 7;
		alevel = controllers[ 3 ] << Maths.FP_SHIFT - 7;
		cutoff += filter_env.get_amplitude() * alevel >> Maths.FP_SHIFT;
		if( cutoff > Maths.FP_ONE ) {
			cutoff = Maths.FP_ONE;
		}
		cutoff = Maths.exp_scale( cutoff, 8 );
		filter.set_cutoff( cutoff / ( float ) Maths.FP_ONE );
		filter.filter( out_buf, length );
		filter_env.update( length );
	}

	public synchronized void note_on( int key, int velocity ) {
		int idx;
		int porta_voice, assigned_voice, quietest_voice;
		int highest_key, voice_key, voice_vol, min_vol;
		boolean key_is_on;

		if( key < 0 || key > 127 ) {
			return;
		}

		key_status[ key ] = 0;
		if( velocity > 0 ) {
			key_status[ key ] = 1;
		}
		/* Determine highest depressed key. */
		highest_key = 128;
		for( idx = 0; idx < 128; idx++ ) {
			if( key_status[ idx ] > 0 ) {
				highest_key = idx;
			}
		}

		min_vol = -1;
		porta_voice = -1;
		assigned_voice = -1;
		quietest_voice = -1;
		for( idx = 0; idx < NUM_VOICES; idx++ ) {
			key_is_on = voices[ idx ].key_is_on();
			voice_key = voices[ idx ].get_key();
			if( key == voice_key ) {
				/* Voice has this key already assigned to it. */
				if( key_is_on || assigned_voice < 0 ) {
					/* Voices may have the same key. Prefer */
					/* the keyed-on voice over a keyed off one. */
					assigned_voice = idx;
				}
			}
			if( key_is_on ) {
				if( controllers[ 5 ] > 0 ) {
					/* Portamento mode. */
					if( porta_voice > -1 ) {
						/* Only one voice should be active.*/
						voices[ porta_voice ].key_off( false );
					}
					porta_voice = idx;
				}
			} else {
				/* Test if this is the quietest. */
				voice_vol = voices[ idx ].get_volume();
				if( quietest_voice < 0 || voice_vol < min_vol ) {
					quietest_voice = idx;
					min_vol = voice_vol;
				}
			}
		}

		if( velocity > 0 ) {
			/* Key on */
			if( porta_voice > -1 ) {
				if( key == highest_key ) {
					/* Key pressed is higher than before.*/
					filter_env.key_on();
					filter_env.key_off( false );
					/* New key is the highest. */
					voices[ porta_voice ].key_on( key );
				}
			} else {
				filter_env.key_on();
				filter_env.key_off( false );
				if( assigned_voice > -1 ) {
					/* Re-key previously assigned voice. */
					voices[ assigned_voice ].key_on( key );
				} else if( quietest_voice > -1 ) {
					/* Allocate new voice.*/
					voices[ quietest_voice ].key_on( key );
				}
			}
		} else {
			/* Key off */
			if( porta_voice > -1 ) {
				if( highest_key > 127 ) {
					/* Porta voice released.*/
					voices[ porta_voice ].key_off( false );
				} else if( key > highest_key ) {
					/* Highest key released, keys still down. */
					voices[ porta_voice ].key_on( highest_key );
				}
			} else {
				if( assigned_voice > -1 ) {
					/* Key off assigned voice. */
					voices[ assigned_voice ].key_off( false );
				}
			}
		}
	}

	public synchronized int get_controller( int controller ) {
		int value = 0;
		if( controller >= 0 && controller < controllers.length ) {
			return controllers[ controller ];
		}
		return value;
	}

	public synchronized void set_controller( int controller, int value ) {
		int idx;
		if( controller < 0 || controller >= controllers.length ) {
			return;
		}
		if( value < 0 || value > 127 ) {
			return;
		}
		controllers[ controller ] = ( byte ) value;
		switch( controller ) {
			case 0:
				/* Logarithmic preamp from 1/NUM_VOICES to 2 */
				value = value << Maths.FP_SHIFT - 7;
				value = Maths.exp_scale( value, LOG2_NUM_VOICES + 1 ) << 1;
				for( idx = 0; idx < NUM_VOICES; idx++ ) {
					voices[ idx ].set_volume( value );
				}
				break;
			case 1:
				/* Filter cutoff, handled in envelope calculations.*/
				break;
			case 2:
				filter.set_resonance( value * 0.0314f );
				break;
			case 3:
				/* Filter envelope level.*/
				break;
			case 4:
				filter_env.set_release_time( value << 4 );
				break;
			case 5:
				value = value << Maths.FP_SHIFT - 7;
				value = Maths.exp_scale( value, 7 );
				for( idx = 0; idx < NUM_VOICES; idx++ ) {
					voices[ idx ].set_portamento_time( value * 1000 / Maths.FP_ONE );
				}
				break;
			default:
				for( idx = 0; idx < NUM_VOICES; idx++ ) {
					voices[ idx ].set_controller( controller - NUM_CONTROLLERS, value );
				}
				break;
		}
	}

	public synchronized void set_pitch_wheel( int value ) {
		int idx;
		for( idx = 0; idx < NUM_VOICES; idx++ ) {
			voices[ idx ].set_pitch_wheel( value );
		}
	}

	public synchronized void set_mod_wheel( int value ) {
		// Hard coded to vibrato depth.
		set_controller( 11, value );
	}

	public synchronized void all_notes_off( boolean sound_off ) {
		int idx;
		for( idx = 0; idx < NUM_VOICES; idx++ ) {
			voices[ idx ].key_off( sound_off );
		}
		for( idx = 0; idx < 128; idx++ ) {
			key_status[ idx ] = 0;
		}
	}

	private static int downsample( int s, final int[] buffer, final int len ) {
		// Convolve with kernel ( 0.25, 0.5, 0.25 ).
		// Filter envelope is Sin^2( PI * f ) / ( PI * f )^2 where fs = 1.0.
		for( int in = 0, out = 0; in < len; in += 2, out += 1 ) {
			int a = s + ( buffer[ in ] >> 1 );
			s = buffer[ in + 1 ] >> 2;
			buffer[ out ] = a + s;
		}
		return s;
	}

	/**
	 * @return the num_controls
	 */
	public int getNum_controls() {
		return num_controls;
	}

	/**
	 * @param num_controls the num_controls to set
	 */
	public void setNum_controls(int num_controls) {
		this.num_controls = num_controls;
	}
}

