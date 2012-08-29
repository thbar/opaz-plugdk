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

public class JavaDelay extends VSTPluginAdapter {
    public static String[] PARAM_LABELS = new String[] { "ms", "%", "Hz", "", "%" };
    public static String[] PARAM_NAMES  = new String[] { "Delay Time", "Feedback", "LFO Frq", "LFO Depth",
            "Wet/Dry Mix" };
    public static float[] PARAM_PRINT_MUL = new float[] { 0x3e8, 0x64, 0x1, 0x1, 0x64 };
    public static int     P_DELAY_TIME    = 0x0;
    public static int     P_FEEDBACK      = 0x1;
    public static int     P_LFO_DEPTH     = 0x3;
    public static int     P_LFO_FREQUENCY = 0x2;
    public static int     P_WET_DRY_MIX   = 0x4;
    public static int     NUM_PARAMS      = P_WET_DRY_MIX + 0x1;
    private int           currentProgram  = 0x0;
    private float         echoDW          = 0.8f;
    private float         echoFeedback    = 0x0;
    private float         echoLFODepth    = 0.8f;
    private long          echoLFODiff     = 0x0;
    private long          echoLFODiffMax  = 0x0;
    private float         echoLFOPos      = 0x0;
    private float         echoLFOSpeed    = 0x0;
    private int           echoPos         = 0x0;
    private int           echoSize        = 0x0;

    // Some default programs
    private float[][] programs = new float[][] {
        { 0.45f, 0.50f, 0.0f, 0f, 0.5f }, { 0.01f, 0.85f, 0.2f, 0.5f, 0.65f }, { 0.99f, 0.7f, 0.0f, 0.02f, 0.50f },
        { 0.3f, 0.9f, 0.0f, 0.0f, 0.50f }, { 0.004f, 0.80f, 0.1f, 0.8f, 0.50f }, { 0.4f, 0.50f, 0.1f, 0.5f, 0.50f },
        { 0.1f, 0.50f, 0.1f, 0.6f, 0.50f }, { 0.1f, 0.50f, 0.1f, 0.7f, 0.50f }
    };
    private float   sampleRate = 0xac44;
    private float[] echo;

    @SuppressWarnings("deprecation")
    public JavaDelay(long wrapper) {
        super(wrapper);
        currentProgram = 0x0;
        update();

        // communicate with the host
        this.setNumInputs(0x1);     // mono input
        this.setNumOutputs(0x1);    // mono output

        // this.hasVu(false); //deprecated as of vst2.4
        this.canProcessReplacing(true);    // mandatory for vst 2.4!
        this.setUniqueID(0x96b43f);        // random unique number registered at

        // steinberg (4 byte)
        this.canMono(true);
        log("Construktor DDelay() INVOKED!");
    }

    @Override
    public void setSampleRate(float sampleRate) {
        this.sampleRate = sampleRate;
    }

    // Allocate new buffer when modifying buffer size!
    private void setEchoTime(float millisDelay) {
        int sampleSize = (int) (millisDelay * sampleRate / 0x3e8);

        echoSize = sampleSize;

        if ((echo == null) || (echo.length != echoSize)) {
            echo = new float[echoSize];
        }
    }

    private void update() {
        setEchoTime(programs[currentProgram][P_DELAY_TIME] * 0x3e8);
        echoFeedback = programs[currentProgram][P_FEEDBACK];

        // Speed of 1 Hz => 2xPI for one revolution!
        // But - depending on number of samples this will be differently large
        // chunks of updates!?
        echoLFOSpeed   = (float) (programs[currentProgram][P_LFO_FREQUENCY] * 0x2 * 3.1415 / sampleRate);
        echoLFODepth   = programs[currentProgram][P_LFO_DEPTH];
        echoLFODiffMax = (long) ((echoSize / 2.0) * echoLFODepth);
        echoLFODiff    = 0x0;
        echoDW         = programs[currentProgram][P_WET_DRY_MIX];
        echoPos        = 0x0;
    }

    @Override
    public int canDo(String feature) {

        // the host asks us here what we are able to do
        int ret = CANDO_NO;

        if (feature.equals(CANDO_PLUG_1_IN_1_OUT)) {
            ret = CANDO_YES;
        }

        if (feature.equals(CANDO_PLUG_PLUG_AS_CHANNEL_INSERT)) {
            ret = CANDO_YES;
        }

        if (feature.equals(CANDO_PLUG_PLUG_AS_SEND)) {
            ret = CANDO_YES;
        }

        log("canDo: " + feature + " = " + ret);

        return ret;
    }

    @Override
    public String getProductString() {
        return "DDelay";
    }

    @Override
    public String getEffectName() {
        return "DDelay";
    }

    @Override
    public String getProgramNameIndexed(int category, int index) {
        return "program: cat: " + category + ", " + index;
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

    @Override
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
            return "" + ((int) (0x64 * PARAM_PRINT_MUL[index] * programs[currentProgram][index])) / 100.0;
        }

        return "0.0";
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
        update();
    }

    @Override
    public void setProgram(int index) {
        currentProgram = index;
        update();
    }

    @Override
    public void setProgramName(String name) {

        // Ignore so far...
    }

    @Override
    public int getPlugCategory() {
        log("getPlugCategory");

        return PLUG_CATEG_EFFECT;
    }

    // Generate / Process the sound!
    @Override
    public void processReplacing(float[][] inputs, float[][] outputs, int sampleFrames) {
        float[] inBuffer  = inputs[0x0];
        float[] outBuffer = outputs[0x0];

        for (int i = 0x0, n = sampleFrames; i < n; i++) {
            float exVal    = inBuffer[i];
            int   echoRead = (int) (echoPos + echoLFODiff);

            if (echoRead >= echoSize) {
                echoRead -= echoSize;
            }

            float out = (exVal * (1.0f - echoDW) + echo[echoRead] * echoDW);

            outBuffer[i]  = out;
            exVal         += echo[echoRead] * echoFeedback;
            echo[echoPos] = exVal;
            echoPos       = (echoPos + 0x1);

            if (echoPos >= echoSize) {
                echoPos = 0x0;
            }
        }

        // Update LFO - which is a sine!
        echoLFODiff = (int) (echoLFODiffMax * (1.0 + Math.sin(echoLFOPos)));
        echoLFOPos  += echoLFOSpeed * sampleFrames;
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
