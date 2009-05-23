/* 
 * jVSTwRapper - The Java way into VST world!
 * 
 * jVSTwRapper is an easy and reliable Java Wrapper for the Steinberg VST interface. 
 * It enables you to develop VST 2.4 compatible audio plugins and virtual instruments 
 * plus user interfaces with the Java Programming Language.
 * 
 * jVSTwRapper 
 * Copyright (C) 2006  Daniel Martin [daniel309@users.sourceforge.net] 
 *             and many others, see CREDITS.txt
 *
 * DDelay - Delay Unit 
 * Copyright (C) 2007 Joakim Eriksson [joakime@sics.se]
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */


import jvst.wrapper.VSTPluginAdapter;
import jvst.wrapper.valueobjects.VSTEvent;
import jvst.wrapper.valueobjects.VSTEvents;
import jvst.wrapper.valueobjects.VSTMidiEvent;

public class MIDIVSTPluginSkeleton extends VSTPluginAdapter {

	//cached instances --> avoid GC --> GOOD!
	VSTMidiEvent vme = new VSTMidiEvent();
	VSTEvents ves = new VSTEvents();

	
	public static int NUM_PARAMS = 1;
	public static String[] PARAM_NAMES = new String[] { "MIDI CC Value" };
	public static String[] PARAM_LABELS = new String[] { "CC Value" };
	public static float[] PARAM_PRINT_MUL = new float[] { 127 };

	// Some default programs
	private float[][] programs = new float[][] { { 0.0f } };
	private int currentProgram = 0;

	public MIDIVSTPluginSkeleton(long wrapper) {
		super(wrapper);

		currentProgram = 0;

		// for compatibility, we say that we have 2 ins and outs
		this.setNumInputs(2);
		this.setNumOutputs(2);

		this.canProcessReplacing(true);// mandatory for vst 2.4!
		this.setUniqueID('j' << 24 | 'R' << 16 | 'u' << 8 | 'b');// jRub
		
		log("Construktor INVOKED!");
	}

	public int canDo(String feature) {
		// the host asks us here what we are able to do
		int ret = CANDO_NO;

		if (feature.equals(CANDO_PLUG_SEND_VST_MIDI_EVENT))
			ret = CANDO_YES;
		if (feature.equals(CANDO_PLUG_SEND_VST_EVENTS))
			ret = CANDO_YES;

		if (feature.equals(CANDO_PLUG_RECEIVE_VST_EVENTS))
			ret = CANDO_YES;
		if (feature.equals(CANDO_PLUG_RECEIVE_VST_MIDI_EVENT))
			ret = CANDO_YES;

		if (feature.equals(CANDO_PLUG_MIDI_PROGRAM_NAMES)) //TODO: delete ???
			ret = CANDO_YES;
		
		log("Host asked canDo: " + feature + " we replied: " + ret);
		return ret;
	}

	public String getProductString() {
		return "product1";
	}

	public String getEffectName() {
		return "midiplug";
	}

	public String getProgramNameIndexed(int category, int index) {
		return "prog categ=" + category + ", idx=" + index;
	}

	public String getVendorString() {
		return "http://jvstwrapper.sourceforge.net/";
	}

	public boolean setBypass(boolean value) {
		// do not support soft bypass!
		return false;
	}

	public boolean string2Parameter(int index, String value) {
		try {
			if (value != null) this.setParameter(index, Float.parseFloat(value));
			return true;
		} catch (Exception e) { // ignore
			return false;
		}
	}

	public int getNumParams() {
		return NUM_PARAMS;
	}

	public int getNumPrograms() {
		return programs.length;
	}

	public float getParameter(int index) {
		if (index < programs[currentProgram].length)
			return programs[currentProgram][index];
		return 0.0f;
	}

	public String getParameterDisplay(int index) {
		if (index < programs[currentProgram].length) {
			return "" + (int) (PARAM_PRINT_MUL[index] * programs[currentProgram][index]);
		}
		return "0";
	}

	public String getParameterLabel(int index) {
		if (index < PARAM_LABELS.length) return PARAM_LABELS[index];
		return "";
	}

	public String getParameterName(int index) {
		if (index < PARAM_NAMES.length) return PARAM_NAMES[index];
		return "param: " + index;
	}

	public int getProgram() {
		return currentProgram;
	}

	public String getProgramName() {
		return "program " + currentProgram;
	}

	public void setParameter(int index, float value) {
		programs[currentProgram][index] = value;
	}

	public void setProgram(int index) {
		currentProgram = index;
	}

	public void setProgramName(String name) {
		// TODO: ignored
	}

	public int getPlugCategory() {
		log("getPlugCategory");
		return PLUG_CATEG_EFFECT;  //TODO: maybe return categ synth here ???
		//return PLUG_CATEG_SYNTH;
	}

	
	// Generate / Process the sound!
	public void processReplacing(float[][] inputs, float[][] outputs, int sampleFrames) {
		//DO NOTHING HERE
	}

	
	// process MIDI
	public int processEvents(VSTEvents ev) {
		// TODO: midi impl. here
		// for now, all incoming MIDI is echoed
		this.sendVstEventsToHost(ev); //simply echo all incoming events
		return 1; // want more midi :-)
	}

}
