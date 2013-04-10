
package jvst.examples.liquinth;

import javax.sound.midi.*;

class MidiReceiver implements Receiver {
	private final Synthesizer synthesizer;
	private int midi_channel;

	public MidiReceiver( Synthesizer synth ) {
		synthesizer = synth;
		set_channel( 1 );
	}

	public void set_channel( int channel ) {
		if( channel >= 1 && channel <= 16 ) {
			midi_channel = channel;
		}
	}

	public void send( MidiMessage midi_msg, long time_stamp ) {
		send( midi_msg.getMessage() );
	}

	public void send( byte[] msg_data ) {
		int ctrl_index, ctrl_value, msg_status, msg_channel;
		msg_status = ( msg_data[ 0 ] & 0xF0 ) >> 4;
		if( msg_status == 0xF ) {
			/* Ignore system messages.*/
			return;
		}
		msg_channel = ( msg_data[ 0 ] & 0xF ) + 1;
		if( msg_channel != midi_channel ) {
			/* Message not on our channel.*/
			return;
		}
		switch( msg_status ) {
			case 0x8: /* Note off.*/
				synthesizer.note_on( msg_data[ 1 ] & 0x7F, 0 );
				break;
			case 0x9: /* Note on.*/
				/* It seems note on with velocity = 0 is also note off.*/
				synthesizer.note_on( msg_data[ 1 ] & 0x7F, msg_data[ 2 ] & 0x7F );
				break;
			case 0xB: /* Control change.*/
				/* Controller 120 = all sound off */
				/* Controller 121 = reset all controllers */
				/* Controller 123 = all notes off */
				ctrl_index = msg_data[ 1 ] & 0x7F;
				ctrl_value = msg_data[ 2 ] & 0x7F;
				if( ctrl_index == 1 ) {
					// Modulation wheel
					synthesizer.set_mod_wheel( ctrl_value );
				} else {
					synthesizer.set_controller( ctrl_index - 20, ctrl_value );
				}
				break;
			case 0xC: /* Program change.*/
				/* program = msg_data[ 1 ] & 0x7F; */
				break;
			case 0xE: /* Pitch wheel.*/
				ctrl_value = ( msg_data[ 1 ] & 0x7F ) | ( ( msg_data[ 2 ] & 0x7F ) << 7 );
				synthesizer.set_pitch_wheel( ctrl_value - 8192 );
				break;
		}
	}

	public void close() {
	}
}
