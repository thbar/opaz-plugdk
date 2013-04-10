
package jvst.examples.liquinth;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

import jvst.wrapper.*;
import jvst.wrapper.gui.VSTPluginGUIRunner;

class LiquinthVSTGUI extends VSTPluginGUIAdapter {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Synthesizer synthesizer;

	public LiquinthVSTGUI( VSTPluginGUIRunner r, VSTPluginAdapter plugin ) {
		super( r, plugin );
		final LiquinthVST liquinth_vst = ( LiquinthVST ) plugin;

		setTitle( Liquinth.VERSION );
		JPanel panel = new JPanel();
		panel.setLayout( new BorderLayout() );
		panel.add( new LogoPanel(), BorderLayout.NORTH );
		panel.add( liquinth_vst.init_gui(), BorderLayout.CENTER );
		panel.setBorder( new EmptyBorder( 6, 6, 6, 6 ) );
		getContentPane().add( panel );
		
		pack();
		this.setResizable(false);
		
		if( RUNNING_MAC_X ) this.setVisible(true);
	}

	/**
	 * @return the synthesizer
	 */
	public Synthesizer getSynthesizer() {
		return synthesizer;
	}

	/**
	 * @param synthesizer the synthesizer to set
	 */
	public void setSynthesizer(Synthesizer synthesizer) {
		this.synthesizer = synthesizer;
	}
}
