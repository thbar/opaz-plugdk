
package jvst.examples.liquinth;

import java.awt.event.*;

/*
	Will only work correctly on UK keyboards :/
*/
class VirtualKeyboard implements KeyListener {
	private static final int[] keys = new int[] {
		KeyEvent.VK_ENTER,
		KeyEvent.VK_SPACE,
		KeyEvent.VK_F1,
		KeyEvent.VK_F2,
		KeyEvent.VK_F3,
		KeyEvent.VK_F4,
		KeyEvent.VK_F5,
		KeyEvent.VK_F6,
		KeyEvent.VK_F7,
		KeyEvent.VK_F8,

		KeyEvent.VK_Z, /* C0 = 10 */
		KeyEvent.VK_S,
		KeyEvent.VK_X,
		KeyEvent.VK_D,
		KeyEvent.VK_C,
		KeyEvent.VK_V,
		KeyEvent.VK_G,
		KeyEvent.VK_B,
		KeyEvent.VK_H,
		KeyEvent.VK_N,
		KeyEvent.VK_J,
		KeyEvent.VK_M,

		KeyEvent.VK_Q, /* C1 = 22 */
		KeyEvent.VK_2,
		KeyEvent.VK_W,
		KeyEvent.VK_3,
		KeyEvent.VK_E,
		KeyEvent.VK_R,
		KeyEvent.VK_5,
		KeyEvent.VK_T,
		KeyEvent.VK_6,
		KeyEvent.VK_Y,
		KeyEvent.VK_7,
		KeyEvent.VK_U,

		KeyEvent.VK_I,
		KeyEvent.VK_9,
		KeyEvent.VK_O,
		KeyEvent.VK_0,
		KeyEvent.VK_P,
	};

	private final Keyboard keyboard;
	private final int[] key_status;
	private int octave;

	public VirtualKeyboard( Keyboard kb ) {
		keyboard = kb;
		key_status = new int[ keys.length ];
		octave = 4;
	}

	private int get_key( int key_code ) {
		int key, idx;
		key = -1;
		for( idx = 0; idx < keys.length; idx++ ) {
			if( keys[ idx ] == key_code ) {
				key = idx;
			}
		}
		return key;
	}

	private void all_notes_off( boolean silence ) {
		int idx;
		keyboard.all_notes_off( silence );
		for( idx = 0; idx < key_status.length; idx++ ) {
			key_status[ idx ] = 0;
		}
	}

	public void keyPressed( KeyEvent ke ) {
		int key;
		key = get_key( ke.getKeyCode() );
		if( key >= 10 ) { /* Note */
			if( key_status[ key ] == 0 ) {
				keyboard.note_on( octave * 12 + key - 10, 127 );
				key_status[ key ] = 1;
			}
		} else if( key >= 2 ) {
			/* Set Octave */
			octave = key - 2;
			all_notes_off( false );
		} else if( key >= 0 ) {
			/* If space, release all, if enter, silence all.*/
			all_notes_off( key == 0 );
		}
	}

	public void keyReleased( KeyEvent ke ) {
		int key;
		key = get_key( ke.getKeyCode() );
		if( key >= 10 ) { /* Note */
			keyboard.note_on( octave * 12 + key - 10, 0 );
			key_status[ key ] = 0;
		}
	}

	public void keyTyped( KeyEvent ke ) {
	}
}
