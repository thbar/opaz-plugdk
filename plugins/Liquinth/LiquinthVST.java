
package jvst.examples.liquinth;

import jvst.wrapper.*;
import jvst.wrapper.valueobjects.*;

class LiquinthVST extends VSTPluginAdapter {
	private static final int
		NUM_PROGRAMS = 16,
		MIX_BUF_FRAMES = 1024;

	private Synthesizer synthesizer;
	private final AudioSource audio_source;
	private MidiReceiver midi_receiver;
	private final Program[] programs;
	private int current_program;
	private final int[] mix_buf;

	public LiquinthVST( long wrapper ) {
		super( wrapper );
		
		Liquinth liquinth = new Liquinth( 48000 );
		synthesizer = liquinth;
		audio_source = liquinth;
		midi_receiver = new MidiReceiver( synthesizer );

		programs = new Program[ NUM_PROGRAMS ];
		for( int prg_idx = 0; prg_idx < NUM_PROGRAMS; prg_idx++ ) {
			programs[ prg_idx ] = new Program( "Blank " + prg_idx, synthesizer );
		}

		mix_buf = audio_source.allocate_mix_buf( MIX_BUF_FRAMES );
		
		setNumInputs( 0 );
		setNumOutputs( 1 );
		canProcessReplacing( true );
		isSynth( true );
		setUniqueID( Liquinth.RELEASE_DATE );

		suspend();
	}

	public void suspend() {
		// TODO Auto-generated method stub
		
	}

	public SynthesizerPanel init_gui() {
		synthesizer = new SynthesizerPanel( synthesizer );
		midi_receiver = new MidiReceiver( synthesizer );
		for( int prg_idx = 0; prg_idx < NUM_PROGRAMS; prg_idx++ ) {
			programs[ prg_idx ].set_controls( synthesizer );
		}
		return ( SynthesizerPanel ) synthesizer;
	}
	
	private void set_controller( int ctrl_idx, int value ) {
		synthesizer.set_controller( ctrl_idx, value );
	}

	/* Deprecated as of VST 2.4 */
	public void resume() {
		wantEvents( 1 );
	}

	public void setSampleRate( float sample_rate ) {
		audio_source.set_sampling_rate( ( int ) sample_rate );
	}

	public void setProgram( int index ) {
		if( index < 0 || index >= NUM_PROGRAMS ) return;
		programs[ current_program ].store();
		current_program = index;
		programs[ current_program ].load();
	}

	public void setParameter(int index, float value) {
		set_controller( index, Math.round( value * 127 ));
	}

	public float getParameter( int index ) {
		return synthesizer.get_controller( index ) / 127f;
	}

	public void setProgramName( String name ) {
		programs[ current_program ].name = name;
	}

	public String getProgramName() {
		return programs[ current_program ].name;
	}

	public String getParameterLabel( int index ) {
		return "";
	}

	public String getParameterDisplay( int index ) {
		return "";
	}

	public String getParameterName( int index ) {
		return synthesizer.get_controller_name( index );
	}

	public VSTPinProperties getOutputProperties( int index ) {
		VSTPinProperties vpp = null;
		if( index == 0 ) {
			vpp = new VSTPinProperties();
			vpp.setLabel( "Liquinth" );
			vpp.setFlags( VSTPinProperties.VST_PIN_IS_ACTIVE );
		}
		return vpp;
	}

	public String getProgramNameIndexed( int category, int index ) {
		if( index < 0 || index >= NUM_PROGRAMS ) return "";
		return programs[ index ].name;
	}

	/* Deprecated as of VST 2.4 */
	public boolean copyProgram( int dest_idx ) {
		if( dest_idx < 0 || dest_idx >= NUM_PROGRAMS ) return false;
		programs[ dest_idx ] = new Program( programs[ current_program ] );
		return true;
	}

	public String getEffectName() {
		return Liquinth.VERSION;
	}

	public String getVendorString() {
		return Liquinth.AUTHOR;
	}

	public String getProductString() {
		return Liquinth.VERSION;
	}

	public int getNumPrograms() {
		return NUM_PROGRAMS;
	}

	public int getNumParams() {
		return synthesizer.get_num_controllers();
	}

	public boolean setBypass( boolean value ) {
		return false;
	}

	public int getProgram() {	
		return current_program;
	}

	public int getPlugCategory() {
		return VSTPluginAdapter.PLUG_CATEG_SYNTH;
	}

	public int canDo( String feature ) {
		if( feature.equals( CANDO_PLUG_RECEIVE_VST_EVENTS ) )
			return CANDO_YES;
		if( feature.equals( CANDO_PLUG_RECEIVE_VST_MIDI_EVENT ) )
			return CANDO_YES;
		return CANDO_NO;
	}

	public boolean string2Parameter( int index, String value ) {
		try {
			float float_value = Float.parseFloat( value );
			setParameter( index, float_value );
		} catch( Exception e ) {
			return false;
		}
		return true;
	}

	/* Deprecated as of VST 2.4 */
	public void process( float[][] inputs, float[][] outputs, int frames ) {
		float[] output = outputs[ 0 ];
		int out_idx = 0;
		while( frames > 0 ) {
			int length = frames;
			if( length > MIX_BUF_FRAMES ) length = MIX_BUF_FRAMES;
			audio_source.get_audio( mix_buf, length );
			for( int mix_idx = 0; mix_idx < length; mix_idx++ ) {
				float out = mix_buf[ mix_idx ];
				output[ out_idx++ ] += out * 0.00003f; 
			}
			frames -= length;
		}
	}

	public void processReplacing( float[][] inputs, float[][] outputs, int frames ) {
		float[] output = outputs[ 0 ];
		int out_idx = 0;
		while( frames > 0 ) {
			int length = frames;
			if( length > MIX_BUF_FRAMES ) length = MIX_BUF_FRAMES;
			audio_source.get_audio( mix_buf, length );
			for( int mix_idx = 0; mix_idx < length; mix_idx++ ) {
				float out = mix_buf[ mix_idx ];
				output[ out_idx++ ] = out * 0.00003f; 
			}
			frames -= length;
		}
	}

	public int processEvents( VSTEvents vst_events ) {
		VSTEvent[] events = vst_events.getEvents();
		int num_events = vst_events.getNumEvents();
		for( int ev_idx = 0; ev_idx < num_events; ev_idx++ ) {
			VSTEvent event = events[ ev_idx ];
			if( event.getType() == VSTEvent.VST_EVENT_MIDI_TYPE ) {
				midi_receiver.send( ( ( VSTMidiEvent ) event ).getData() );
			}
		}
		return 1;
	}
}

class Program {
	public String name = "";
	private final int[] controllers;
	private Controls controls;
	
	public Program( String name, Controls controls ) {
		this.name = name;
		controllers = new int[ controls.get_num_controllers() ];
		set_controls( controls );
		store();
	}
	
	public Program( Program program ) {
		name = program.name;
		controls = program.controls;
		controllers = new int[ program.controllers.length ];
        System.arraycopy(program.controllers, 0, controllers, 0, controllers.length);
	}
	
	public void load() {
		for( int idx = 0; idx < controllers.length; idx++ ) {
			controls.set_controller( idx, controllers[ idx ] );
		}		
	}
	
	public void store() {
		for( int idx = 0; idx < controllers.length; idx++ ) {
			controllers[ idx ] = controls.get_controller( idx );
		}
	}
	
	public void set_controls( Controls controls ) {
		if( controls.get_num_controllers() != controllers.length ) {
			throw new IllegalArgumentException( "Number of controllers differ." );
		}
		this.controls = controls;
	}
}
