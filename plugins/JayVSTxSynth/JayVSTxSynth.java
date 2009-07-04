/* 
 * jVSTwRapper - The Java way into VST world!
 * 
 * jVSTwRapper is an easy and reliable Java Wrapper for the Steinberg VST interface. 
 * It enables you to develop VST 2.3 compatible audio plugins and virtual instruments 
 * plus user interfaces with the Java Programming Language. 3 Demo Plugins(+src) are included!
 * 
 * Copyright (C) 2006  Daniel Martin [daniel309@users.sourceforge.net] 
 * 					   and many others, see CREDITS.txt
 *
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

import jvst.wrapper.*;
import jvst.wrapper.valueobjects.*;


public class JayVSTxSynth extends VSTPluginAdapter {
  private static final int NUM_FREQUENCIES = 128;	// 128 midi notes
  private static final int WAVE_SIZE = 4096;		// samples (must be power of 2 here)

  private static final double MIDI_SCALER = 1.0D / 127.0D;
  private static final int NUM_PROGRAMS = 16;
  private static final int NUM_OUTPUTS = 2;


  private JayVSTxSynthProgram[] programs = new JayVSTxSynthProgram[NUM_PROGRAMS];
  private int currentProgram;

  private float fWaveform1;
  private float fFreq1;
  private float fVolume1;
  private float fWaveform2;
  private float fFreq2;
  private float fVolume2;

  private float fVolume;
  private float fPhase1, fPhase2;
  private float fScaler;

  private int channelPrograms[] = new int[NUM_PROGRAMS];

  private int currentNote;
  private int currentVelocity;
  private int currentDelta;
  private boolean noteIsOn;



  private static float SAWTOOTH[] = new float[WAVE_SIZE];
  private static float PULSE[] = new float[WAVE_SIZE];
  private static float FREQ_TAB[] = new float[WAVE_SIZE];

  static {
    // make waveforms
    long wh = WAVE_SIZE / 4;	// 1:3 pulse
    for (int i = 0; i < WAVE_SIZE; i++) {
            SAWTOOTH[i] = (float)(-1. + (2. * ((double)i / (double)WAVE_SIZE)));
            PULSE[i] = (i < wh) ? -1.f : 1.f;
    }

    // make frequency (Hz) table
    double k = 1.059463094359;	// 12th root of 2
    double a = 6.875;	// a
    a *= k;	// b
    a *= k;	// bb
    a *= k;	// c, frequency of midi note 0
    for (int i = 0; i < NUM_FREQUENCIES; i++) {	// 128 midi notes
      FREQ_TAB[i] = (float) a;
      a *= k;
    }
  }



  public JayVSTxSynth(long wrapper) {
    super(wrapper);
    log("Construktor jVSTxSynth() START!");

    for (int i = 0; i < this.programs.length; i++) this.programs[i] = new JayVSTxSynthProgram();
    for (int i = 0; i < this.channelPrograms.length; i++) this.channelPrograms[i] = i;

    this.setProgram(0);


    this.setNumInputs(0);// no input
    this.setNumOutputs(2);// 2 outputs, 1 for each oscillator
    //this.hasVu(false); //deprecated as of vst2.4
    //this.hasClip(false); //deprecated as of vst2.4
    this.canProcessReplacing(true);

    this.isSynth(true);

    this.setUniqueID('j'<<24 | 'X'<<16 | 's'<<8 | 'y');

    this.fPhase1 = this.fPhase2 = 0.f;
    this.fScaler = (float)((double)WAVE_SIZE / 44100.0D);	// we don't know the sample rate yet
    this.noteIsOn = false;
    this.currentDelta = 0;

    this.suspend();

    log("Construktor jVSTxSynth() INVOKED!");
  }


  public void resume() {
     this.wantEvents(1); //deprecated as of vst2.4
     					 //keep it anyways to be backward compatible...
  }

  public void setSampleRate(float sampleRate) {
    this.fScaler = (float)((double)WAVE_SIZE / (double)sampleRate);
  }

  public void setProgram(int index) {
    if (index < 0 || index >= NUM_PROGRAMS) return;

    JayVSTxSynthProgram dp = this.programs[index];
    this.currentProgram = index;

    this.setParameter(JayVSTxSynthProgram.PARAM_ID_VOLUME, dp.getVolume());

    this.setParameter(JayVSTxSynthProgram.PARAM_ID_WAVEFORM1, dp.getWaveform1());
    this.setParameter(JayVSTxSynthProgram.PARAM_ID_FREQ1, dp.getFreq1());
    this.setParameter(JayVSTxSynthProgram.PARAM_ID_VOLUME1, dp.getVolume1());

    this.setParameter(JayVSTxSynthProgram.PARAM_ID_WAVEFORM2, dp.getWaveform2());
    this.setParameter(JayVSTxSynthProgram.PARAM_ID_FREQ2, dp.getFreq2());
    this.setParameter(JayVSTxSynthProgram.PARAM_ID_VOLUME2, dp.getVolume2());
  }

  public void setProgramName(String name) {
    this.programs[this.currentProgram].setName(name);
  }

  public String getProgramName() {
    String name;

    if (this.programs[this.currentProgram].getName().equals("Init")) {
      name = this.programs[this.currentProgram].getName() + " " + (this.currentProgram + 1);
    }
    else {
      name = this.programs[this.currentProgram].getName();
    }

    return name;
  }

  public String getParameterLabel(int index) {
    String label = "";

    switch (index) {
      case JayVSTxSynthProgram.PARAM_ID_WAVEFORM1:
      case JayVSTxSynthProgram.PARAM_ID_WAVEFORM2:
        label = "Shape";
        break;
      case JayVSTxSynthProgram.PARAM_ID_FREQ1:
      case JayVSTxSynthProgram.PARAM_ID_FREQ2:
        label = "Hz";
        break;
      case JayVSTxSynthProgram.PARAM_ID_VOLUME:
      case JayVSTxSynthProgram.PARAM_ID_VOLUME1:
      case JayVSTxSynthProgram.PARAM_ID_VOLUME2:
        label = "dB";
        break;
    }

    return label;
  }

  public String getParameterDisplay(int index) {
    String text = "";

    switch (index) {
      case JayVSTxSynthProgram.PARAM_ID_WAVEFORM1: {
        if (this.fWaveform1 < 0.5f) text = "Sawtooth";
        else text = "Pulse   ";
        break;
      }
      case JayVSTxSynthProgram.PARAM_ID_WAVEFORM2:{
        if (this.fWaveform2 < 0.5f) text = "Sawtooth";
        else text = "Pulse   ";
        break;
      }
      case JayVSTxSynthProgram.PARAM_ID_FREQ1: {
        text = Float.toString(this.fFreq1);
        break;
      }
      case JayVSTxSynthProgram.PARAM_ID_FREQ2:{
        text = Float.toString(this.fFreq2);
        break;
      }
      case JayVSTxSynthProgram.PARAM_ID_VOLUME: {
        text = this.dbToString(this.fVolume);
        break;
      }
      case JayVSTxSynthProgram.PARAM_ID_VOLUME1: {
        text = this.dbToString(this.fVolume1);
        break;
      }
      case JayVSTxSynthProgram.PARAM_ID_VOLUME2: {
        text = this.dbToString(this.fVolume2);
        break;
      }
    }

    return text;
  }

  public String getParameterName(int index) {
    String label = "";

   switch (index) {
      case JayVSTxSynthProgram.PARAM_ID_WAVEFORM1: label = "Wave 1"; break;
      case JayVSTxSynthProgram.PARAM_ID_WAVEFORM2: label = "Wave 2"; break;
      case JayVSTxSynthProgram.PARAM_ID_FREQ1: label = "Freq 1"; break;
      case JayVSTxSynthProgram.PARAM_ID_FREQ2: label = "Freq 2"; break;
      case JayVSTxSynthProgram.PARAM_ID_VOLUME: label = "Volume"; break;
      case JayVSTxSynthProgram.PARAM_ID_VOLUME1: label = "Level 1"; break;
      case JayVSTxSynthProgram.PARAM_ID_VOLUME2: label = "Level 2"; break;
    }

    return label;
  }

  public void setParameter(int index, float value) {
    JayVSTxSynthProgram dp = this.programs[this.currentProgram];

    switch (index) {
      case JayVSTxSynthProgram.PARAM_ID_WAVEFORM1: {
        dp.setWaveform1(value);
        this.fWaveform1 = value;
        break;
      }
      case JayVSTxSynthProgram.PARAM_ID_WAVEFORM2:{
        dp.setWaveform2(value);
        this.fWaveform2 = value;
        break;
      }
      case JayVSTxSynthProgram.PARAM_ID_FREQ1: {
        dp.setFreq1(value);
        this.fFreq1 = value;
        break;
      }
      case JayVSTxSynthProgram.PARAM_ID_FREQ2:{
        dp.setFreq2(value);
        this.fFreq2 = value;
        break;
      }
      case JayVSTxSynthProgram.PARAM_ID_VOLUME: {
        dp.setVolume(value);
        this.fVolume = value;
        break;
      }
      case JayVSTxSynthProgram.PARAM_ID_VOLUME1: {
        dp.setVolume1(value);
        this.fVolume1 = value;
        break;
      }
      case JayVSTxSynthProgram.PARAM_ID_VOLUME2: {
        dp.setVolume2(value);
        this.fVolume2 = value;
        break;
      }
    }

  }

  public float getParameter(int index) {
    float v = 0;

    switch (index) {
      case JayVSTxSynthProgram.PARAM_ID_WAVEFORM1: v = this.fWaveform1; break;
      case JayVSTxSynthProgram.PARAM_ID_WAVEFORM2: v = this.fWaveform2; break;
      case JayVSTxSynthProgram.PARAM_ID_FREQ1: v = this.fFreq1; break;
      case JayVSTxSynthProgram.PARAM_ID_FREQ2: v = this.fFreq2; break;
      case JayVSTxSynthProgram.PARAM_ID_VOLUME: v = this.fVolume; break;
      case JayVSTxSynthProgram.PARAM_ID_VOLUME1: v = this.fVolume1; break;
      case JayVSTxSynthProgram.PARAM_ID_VOLUME2: v = this.fVolume2; break;
    }
    return v;
  }

  public VSTPinProperties getOutputProperties (int index) {
    VSTPinProperties ret = null;

    if (index < NUM_OUTPUTS) {
      ret = new VSTPinProperties();
      ret.setLabel("jVSTx " + (index + 1) + "d");
      ret.setFlags(VSTPinProperties.VST_PIN_IS_ACTIVE);
      if (index < 2) {
	// make channel 1+2 stereo
        ret.setFlags(ret.getFlags() | VSTPinProperties.VST_PIN_IS_STEREO);
      }
    }

    return ret;
  }

  public String getProgramNameIndexed(int category, int index) {
    String text = "";
    if (index < this.programs.length) text = this.programs[index].getName();
    if ("Init".equals(text)) text = text + " " + index;
    return text;
  }

  public boolean copyProgram (int destination) {
    if (destination < NUM_PROGRAMS) {
      this.programs[destination] = this.programs[this.currentProgram];
      return true;
    }
    return false;
  }

  public String getEffectName() { return "jVSTxSynth"; }
  public String getVendorString() { return "jVSTwRapper"; }
  public String getProductString() { return "jVSTxSynth"; }
  public int getNumPrograms() { return NUM_PROGRAMS; }
  public int getNumParams() { return JayVSTxSynthProgram.NUM_PARAMS; }
  public boolean setBypass(boolean value) { return false; }
  public int getProgram() { return this.currentProgram; }
  public int getPlugCategory() { return VSTPluginAdapter.PLUG_CATEG_SYNTH; }

  public int canDo(String feature) {
    int ret = JayVSTxSynth.CANDO_NO;

    if (JayVSTxSynth.CANDO_PLUG_RECEIVE_VST_EVENTS.equals(feature)) ret = JayVSTxSynth.CANDO_YES;
    if (JayVSTxSynth.CANDO_PLUG_RECEIVE_VST_MIDI_EVENT.equals(feature)) ret = JayVSTxSynth.CANDO_YES;
    if (JayVSTxSynth.CANDO_PLUG_MIDI_PROGRAM_NAMES.equals(feature)) ret = JayVSTxSynth.CANDO_YES;

    return ret;
  }

  public boolean string2Parameter(int index, String value) {
    boolean ret = false;

    try {
      if (value != null) this.setParameter(index, Float.parseFloat(value));
      ret=true;
    } catch(Exception e) {log(e.toString());}

    return ret;
  }


// midi program names:
// as an example, GM names are used here. in fact, VstXSynth doesn't even support
// multi-timbral operation so it's really just for demonstration.
// a 'real' instrument would have a number of voices which use the
// programs[channelProgram[channel]] parameters when it receives
// a note on message.

  public int getMidiProgramName(int channel, MidiProgramName mpn) {
    int prg = mpn.getThisProgramIndex();

    if (prg < 0 || prg >= 128) return 0;
    this.fillProgram(channel, prg, mpn);
    if (channel == 9) return 1;

    return 128;
  }

  public int getCurrentMidiProgram (int channel, MidiProgramName mpn) {
    if (channel < 0 || channel >= 16 || mpn==null) return -1;

    int prg = this.channelPrograms[channel];
    mpn.setThisProgramIndex(prg);
    fillProgram (channel, prg, mpn);

    return prg;
  }

  public int getMidiProgramCategory (int channel, MidiProgramCategory cat) {
    cat.setParentCategoryIndex(-1);	// -1:no parent category
    cat.setFlags(0);			// reserved, none defined yet, zero.

    int category = cat.getThisCategoryIndex();
    if (channel == 9) {
      cat.setName("Drums");
      return 1;
    }
    if (category >= 0 && category < GMNames.NUM_GM_CATEGORIES)
      cat.setName(GMNames.GM_CATEGORIES[category]);

    return GMNames.NUM_GM_CATEGORIES;
  }

  public boolean hasMidiProgramsChanged (int channel) {
    return false;
    //this.updateDisplay()
  }

// struct will be filled with information for 'thisProgramIndex' and 'thisKeyNumber'
// if keyName is "" the standard name of the key will be displayed.
// if false is returned, no MidiKeyNames defined for 'thisProgramIndex'.
  public boolean getMidiKeyName (long channel, MidiKeyName key) {
    return false;
  }



  //DEPRECATED SINCE 2.4!
  //process is ACCUMULATING the calculated floats to the output
  //BUT STILL, leave it there for backward compatibility (some hosts only call this one
  //and are not aware of processReplacing...)
  public void process(float[][] inputs, float[][] outputs, int sampleFrames) {
    // process () is required, and accumulating (out += h)
    // processReplacing () is optional, and in place (out = h). even though
    // processReplacing () is optional, it is very highly recommended to support it

    if (this.noteIsOn) {
      float[] out1 = outputs[0];
      float[] out2 = outputs[1];
      float baseFreq = FREQ_TAB[this.currentNote & 0x7f] * this.fScaler;
      float freq1 = baseFreq + this.fFreq1;	// not really linear...
      float freq2 = baseFreq + this.fFreq2;
      float wave1[] = (this.fWaveform1 < .5) ? SAWTOOTH : PULSE;
      float wave2[] = (this.fWaveform2 < .5) ? SAWTOOTH : PULSE;
      float wsf = (float)WAVE_SIZE;
      float vol = (float)(this.fVolume * (double)this.currentVelocity * MIDI_SCALER);
      int mask = WAVE_SIZE - 1;

      int start = 0;
      
      if (this.currentDelta > 0) {
        if (this.currentDelta >= sampleFrames) { //future
          this.currentDelta -= sampleFrames;
          return;
        }
        for(int i = 0; i < this.currentDelta; i++) {//zero delta frames
        	out1[i] = 0;
    		out2[i] = 0;
        }
        start = this.currentDelta;
        sampleFrames -= this.currentDelta;
        this.currentDelta = 0;
      }

      for (int i = start, j=out1.length; i < j; i++) {
        // this is all very raw, there is no means of interpolation,
        // and we will certainly get aliasing due to non-bandlimited
        // waveforms. don't use this for serious projects...
        out1[i] += wave1[(int)fPhase1 & mask] * this.fVolume1 * vol;
        out2[i] += wave2[(int)fPhase2 & mask] * this.fVolume2 * vol;
        this.fPhase1 += freq1;
        this.fPhase2 += freq2;
      }
    }
    else {
    	//note off
    	for (int i=0; i<outputs[0].length; i++){
    		outputs[0][i] = 0;
    		outputs[1][i] = 0;
    	}
    }
  }

  //processReplacing is REPLACING the calculated floats to the output
  public void processReplacing(float[][] inputs, float[][] outputs, int sampleFrames) {
    if (this.noteIsOn) {
      float[] out1 = outputs[0];
      float[] out2 = outputs[1];
      float baseFreq = FREQ_TAB[this.currentNote & 0x7f] * this.fScaler;
      float freq1 = baseFreq + this.fFreq1;	// not really linear...
      float freq2 = baseFreq + this.fFreq2;
      float wave1[] = (this.fWaveform1 < .5) ? SAWTOOTH : PULSE;
      float wave2[] = (this.fWaveform2 < .5) ? SAWTOOTH : PULSE;
      float wsf = (float)WAVE_SIZE;
      float vol = (float)(this.fVolume * (double)this.currentVelocity * MIDI_SCALER);
      int mask = WAVE_SIZE - 1;

      int start = 0;
      
      if (this.currentDelta > 0) {
        if (this.currentDelta >= sampleFrames) { //future
          this.currentDelta -= sampleFrames;
          return;
        }
        for(int i = 0; i < this.currentDelta; i++) { //zero delta frames
        	out1[i] = 0;
    		out2[i] = 0;
        }
        start = this.currentDelta;
        sampleFrames -= this.currentDelta;
        this.currentDelta = 0;
      }

      for (int i = start, j=out1.length; i < j; i++) {
        // this is all very raw, there is no means of interpolation,
        // and we will certainly get aliasing due to non-bandlimited
        // waveforms. don't use this for serious projects...
        out1[i] = wave1[(int)fPhase1 & mask] * this.fVolume1 * vol;
        out2[i] = wave2[(int)fPhase2 & mask] * this.fVolume2 * vol;
        this.fPhase1 += freq1;
        this.fPhase2 += freq2;
      }
    }
    else {
    	//note off
    	for (int i=0; i<outputs[0].length; i++){
    		outputs[0][i] = 0;
    		outputs[1][i] = 0;
    	}
    }
  }

  public int processEvents (VSTEvents ev) {
    for (int i = 0; i < ev.getNumEvents(); i++) {
      if (ev.getEvents()[i].getType() != VSTEvent.VST_EVENT_MIDI_TYPE) continue;

      VSTMidiEvent event = (VSTMidiEvent)ev.getEvents()[i];
      byte[] midiData = event.getData();
      int status = midiData[0] & 0xf0;// ignoring channel

      if (status == 0x90 || status == 0x80) {
        // we only look at notes
        int note = midiData[1] & 0x7f;
        int velocity = midiData[2] & 0x7f;
        if (status == 0x80) velocity = 0;	// note off by velocity 0

        if (velocity==0 && (note == currentNote)) this.noteOff();
        else this.noteOn (note, velocity, event.getDeltaFrames());
      }
      else if (status == 0xb0) {
        // all notes off
        if (midiData[1] == 0x7e || midiData[1] == 0x7b)	this.noteOff();
      }
    }

    return 1;	// want more
  }



  private void noteOff() {
    noteIsOn = false;
  }
  private void noteOn(int note, int velocity, int delta) {
    currentNote = note;
    currentVelocity = velocity;
    currentDelta = delta;
    noteIsOn = true;
    fPhase1 = fPhase2 = 0;
  }
  private void fillProgram (int channel, int prg, MidiProgramName mpn) {
    if (channel == 9) {
      //drums
      mpn.setName("Standard");
      mpn.setMidiProgram((byte)0);
      mpn.setParentCategoryIndex(0);
    }
    else {
      mpn.setName(GMNames.GM_NAMES[prg]);
      mpn.setMidiProgram((byte)prg);
      mpn.setParentCategoryIndex(-1);	// for now

      for (int i = 0; i < GMNames.NUM_GM_CATEGORIES; i++) {
        if (prg >= GMNames.GM_CATEGORIES_FIRST_INDICES[i] &&
            prg < GMNames.GM_CATEGORIES_FIRST_INDICES[i + 1]) {
          mpn.setParentCategoryIndex(i);
          break;
        }
      }

    }

  }


}


class JayVSTxSynthProgram {
  public final static int PARAM_ID_VOLUME = 0;
  public final static int PARAM_ID_WAVEFORM1 = 1;
  public final static int PARAM_ID_FREQ1 = 2;
  public final static int PARAM_ID_VOLUME1 = 3;
  public final static int PARAM_ID_WAVEFORM2 = 4;
  public final static int PARAM_ID_FREQ2 = 5;
  public final static int PARAM_ID_VOLUME2 = 6;

  public final static int NUM_PARAMS = PARAM_ID_VOLUME2 + 1;


  private String name = "Init";

  private float volume = 0.9F;

  private float waveform1 = 0.f;	// saw
  private float freq1 = 0.01f;
  private float volume1 = 0.33f;

  private float waveform2 = 1.f;	// pulse
  private float freq2 = 0.05f;
  private float volume2 = 0.33f;


  public String getName() { return this.name; }
  public void setName(String name) { this.name = name; }

  public float getVolume() { return this.volume; }
  public void setVolume(float v) { this.volume = v; }


  public float getVolume1() { return this.volume1; }
  public void setVolume1(float v) { this.volume1 = v; }
  public float getVolume2() { return this.volume2; }
  public void setVolume2(float v) { this.volume2 = v; }

  public float getWaveform1() { return this.waveform1; }
  public void setWaveform1(float v) { this.waveform1 = v; }
  public float getWaveform2() { return this.waveform2; }
  public void setWaveform2(float v) { this.waveform2 = v; }

  public float getFreq1() { return this.freq1; }
  public void setFreq1(float v) { this.freq1 = v; }
  public float getFreq2() { return this.freq2; }
  public void setFreq2(float v) { this.freq2 = v; }
}
