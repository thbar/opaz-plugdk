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



//~--- non-JDK imports --------------------------------------------------------

import jvst.wrapper.VSTPluginAdapter;

public class AudioVSTPluginSkeleton extends VSTPluginAdapter {
    public static int      NUM_PARAMS      = 0x1;
    public static String[] PARAM_LABELS    = new String[] { "CC Value" };
    public static String[] PARAM_NAMES     = new String[] { "MIDI CC Value" };
    public static float[]  PARAM_PRINT_MUL = new float[] { 0x7f };
    private int            currentProgram  = 0x0;

    // Some default programs
    private float[][] programs = new float[][] {
        { 0.0f }
    };

    public AudioVSTPluginSkeleton(long wrapper) {
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
        return "product1";
    }

    @Override
    public String getEffectName() {
        return "audioplug";
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
    }

    @Override
    public void setProgram(int index) {
        currentProgram = index;
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
    @Override
    public void processReplacing(float[][] inputs, float[][] outputs, int sampleFrames) {

        // TODO: algorithm here...
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
