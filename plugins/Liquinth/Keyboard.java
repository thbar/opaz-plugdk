
package jvst.examples.liquinth;

public interface Keyboard {
	public void note_on( int key, int velocity );
	public void all_notes_off( boolean sound_off );
}
