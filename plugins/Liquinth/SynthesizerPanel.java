
package jvst.examples.liquinth;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;

public class SynthesizerPanel extends JPanel implements Synthesizer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Synthesizer synthesizer;
	private final JComboBox c1_assign_cb;
	private final JSlider[] controllers;

	public SynthesizerPanel( Synthesizer synth ) {
		int idx, num_controllers, value;
		String control_name;
		GridBagLayout gbl;
		GridBagConstraints gbc;
		VirtualKeyboard keyboard;

		synthesizer = synth;
		keyboard = new VirtualKeyboard( synth );

		gbl = new GridBagLayout();
		setLayout( gbl );

		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets( 2, 2, 2, 2 );

		gbc.weightx = 0;
		gbc.gridwidth = 1;
		add( new JLabel( "Modulation Wheel" ), gbc );
		gbc.weightx = 1;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		c1_assign_cb = new JComboBox();
		add( c1_assign_cb, gbc );

		num_controllers = synth.get_num_controllers();
		controllers = new JSlider[ num_controllers ];
		for( idx = 0; idx < num_controllers; idx++ ) {
			gbc.weightx = 0;
			gbc.gridwidth = 1;
			control_name = synth.get_controller_name( idx );
			c1_assign_cb.addItem( control_name );
			add( new JLabel( control_name ), gbc );
			gbc.weightx = 1;
			gbc.gridwidth = GridBagConstraints.REMAINDER;
			value = synth.get_controller( idx );
			controllers[ idx ] = new JSlider( JSlider.HORIZONTAL, 0, 127, value );
			controllers[ idx ].addChangeListener( new SliderListener( idx ) );
			controllers[ idx ].addKeyListener( keyboard );
			add( controllers[ idx ], gbc );
		}
	}
	
	public void note_on( int key, int velocity ) {
		synthesizer.note_on( key, velocity );
	}
	
	public void all_notes_off( boolean sound_off ) {
		synthesizer.all_notes_off( sound_off );
	}
	
	public int get_num_controllers() {
		return synthesizer.get_num_controllers();
	}
	
	public String get_controller_name( int control ) {
		return synthesizer.get_controller_name( control );
	}
	
	public int get_controller( int controller ) {
		return synthesizer.get_controller( controller );
	}
		
	public void set_controller( final int controller, final int value ) {
		SwingUtilities.invokeLater( new Runnable() {
			public void run() {
				controllers[ controller ].setValue( value );
			}
		} );
	}

	public void set_pitch_wheel( int value ) {
		synthesizer.set_pitch_wheel( value );
	}
	
	public void set_mod_wheel( int value ) {
		set_controller( c1_assign_cb.getSelectedIndex(), value );
	}
	
	private class SliderListener implements ChangeListener {
		private final int controller;

		public SliderListener( int control_idx ) {
			controller = control_idx;
		}

		public void stateChanged( ChangeEvent e ) {
			int value;
			value = controllers[ controller ].getValue();
			synthesizer.set_controller( controller, value );
		}
	}
}
