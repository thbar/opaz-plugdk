
package jvst.examples.liquinth;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

public class LiquinthJS extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private LiquinthJS() {
		Liquinth liquinth = new Liquinth( Player.SAMPLING_RATE );
		Player player = new Player( liquinth );
		AudioSelector audio_selector = new AudioSelector( player );
		SynthesizerPanel synth_panel = new SynthesizerPanel( liquinth );
		MidiReceiver midi_receiver = new MidiReceiver( synth_panel );
		MidiSelector midi_selector = new MidiSelector( midi_receiver );
		GridBagConstraints gbc = new GridBagConstraints();
		setLayout( new GridBagLayout() );
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.weightx = 1;
		add( audio_selector, gbc );
		add( midi_selector, gbc );
		gbc.weighty = 1;
		add( new LogoPanel(), gbc );
		gbc.weighty = 0;
		add( synth_panel, gbc );
		setBorder( new EmptyBorder( 6, 6, 6, 6 ) );
	}

	public static void main( String[] args ) {
		final String title = Liquinth.VERSION + " " + Liquinth.AUTHOR;
		System.out.println( title );
		SwingUtilities.invokeLater( new Runnable() {
			public void run() {
				JFrame frame = new JFrame( title );
				frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
				frame.getContentPane().add( new LiquinthJS() );
				frame.pack();
				frame.setVisible( true );
			}
		} );
	}
}
