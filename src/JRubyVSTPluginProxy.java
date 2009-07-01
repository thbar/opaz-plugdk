import jvst.wrapper.VSTPluginAdapter;
import jvst.wrapper.valueobjects.*;

import org.jruby.Ruby;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;

public class JRubyVSTPluginProxy extends VSTPluginAdapter {

	protected VSTPluginAdapter adapter;
	protected Ruby runtime;

	public JRubyVSTPluginProxy(long wrapper) {
		super(wrapper);
		
		//This creates a new ruby interpreter instance for each instance of the plug
		//defaults are used, eg. out from the running java program (which is *_java_stdout.txt :-))
		runtime = Ruby.newInstance();
		String resourcesFolder = ProxyTools.getResourcesFolder(getLogBasePath());
		String iniFileName = ProxyTools.getIniFileName(resourcesFolder, getLogFileName());

		// TODO: learn how to set a variable on the scope instead of hacking it this way:
		runtime.evalScriptlet("PLUGIN_RESOURCES_FOLDER = '"+resourcesFolder+"'");
		runtime.evalScriptlet("PLUGIN_INI_FILE_NAME = '"+iniFileName+"'");
		runtime.evalScriptlet("PLUGIN_WRAPPER = "+wrapper);
		runtime.evalScriptlet("require 'opaz_bootstrap'");
		Object plugin = runtime.evalScriptlet("PLUG");

		this.adapter = (VSTPluginAdapter)JavaEmbedUtils.rubyToJava(runtime, (IRubyObject) plugin, VSTPluginAdapter.class);
	}

	//hackish init for MockVSTHost
	public static void _hackishInit(String dllLocation, boolean log) {
		_initPlugFromNative(dllLocation, log);
	}
	
	// mandatory overrides from here...
	public int canDo(String arg0) {
		return adapter.canDo(arg0);
	}

	public int getPlugCategory() {
		return adapter.getPlugCategory();
	}

	public String getProductString() {
		return adapter.getProductString();
	}

	public String getProgramNameIndexed(int arg0, int arg1) {
		return adapter.getProgramName();
	}

	public String getVendorString() {
		return adapter.getVendorString();
	}

	public boolean setBypass(boolean arg0) {
		return adapter.setBypass(arg0);
	}

	public boolean string2Parameter(int arg0, String arg1) {
		return adapter.string2Parameter(arg0, arg1);
	}

	public int getNumParams() {
		return adapter.getNumParams();
	}

	public int getNumPrograms() {
		return adapter.getNumPrograms();
	}

	public float getParameter(int arg0) {
		return adapter.getParameter(arg0);
	}

	public String getParameterDisplay(int arg0) {
		return adapter.getParameterDisplay(arg0);
	}

	public String getParameterLabel(int arg0) {
		return adapter.getParameterLabel(arg0);
	}

	public String getParameterName(int arg0) {
		return adapter.getParameterName(arg0);
	}

	public int getProgram() {
		return adapter.getProgram();
	}

	public String getProgramName() {
		return adapter.getProgramName();
	}

	public void processReplacing(float[][] arg0, float[][] arg1, int arg2) {
		adapter.processReplacing(arg0, arg1, arg2);
	}

	public void setParameter(int arg0, float arg1) {
		adapter.setParameter(arg0, arg1);
	}

	public void setProgram(int arg0) {
		adapter.setProgram(arg0);
	}

	public void setProgramName(String arg0) {
		adapter.setProgramName(arg0);
	}


	// optional overrides from here... (copied from VSTPluginAdapter.java)
	// --> forward calls to ruby plugin here in order to allow to be overwritten there (by jruby plug)


	//provide defaults for vst 1.0 OPTIONAL methods
	//********************************
	public void open() { adapter.open(); }
	public void close() { adapter.close(); }
	public void suspend() { adapter.suspend(); }
	public void resume() { adapter.resume(); }
	public float getVu() {return adapter.getVu(); }
	public int getChunk(byte[][] data, boolean isPreset) {return adapter.getChunk(data, isPreset); }
	public int setChunk(byte data[], int byteSize, boolean isPreset) {return adapter.setChunk(data, byteSize, isPreset);}
	public void setBlockSize(int blockSize) { adapter.setBlockSize(blockSize); }
	public void setSampleRate(float sampleRate) { adapter.setSampleRate(sampleRate); }

	//provide defaults for vst 2.0 OPTIONAL methods
	//********************************
	public String getEffectName() {return adapter.getEffectName(); }
	public int getVendorVersion() {return adapter.getVendorVersion(); }
	public boolean canParameterBeAutomated(int index) {return adapter.canParameterBeAutomated(index); }
	public boolean copyProgram(int destination) {return adapter.copyProgram(destination); }
	public int fxIdle() {return adapter.fxIdle();}
	public float getChannelParameter(int channel, int index) {return adapter.getChannelParameter(channel, index); }
	public int getNumCategories() {return adapter.getNumCategories();}

	public VSTPinProperties getInputProperties(int index) {return adapter.getInputProperties(index);}
	public VSTPinProperties getOutputProperties(int index) {return adapter.getOutputProperties(index);}

	public String getErrorText() {return adapter.getErrorText();}
	public int getGetTailSize() {return adapter.getGetTailSize();}
	public VSTParameterProperties getParameterProperties(int index) {return adapter.getParameterProperties(index);}

	public int getVstVersion () {return adapter.getVstVersion();} 
	public void inputConnected (int index, boolean state) { adapter.inputConnected(index, state); }
	public void outputConnected (int index, boolean state) { adapter.outputConnected(index, state); }
	public boolean keysRequired () {return adapter.keysRequired();}

	public int processEvents (VSTEvents e) {return adapter.processEvents (e);}
	public boolean processVariableIo (VSTVariableIO vario) {return adapter.processVariableIo (vario);}
	public int reportCurrentPosition () {return adapter.reportCurrentPosition();}
	public float[] reportDestinationBuffer () {return adapter.reportDestinationBuffer();}
	public void setBlockSizeAndSampleRate(int blockSize, float sampleRate) { adapter.setBlockSizeAndSampleRate(blockSize, sampleRate); }

	public boolean setSpeakerArrangement (VSTSpeakerArrangement pluginInput, VSTSpeakerArrangement pluginOutput) {
		return adapter.setSpeakerArrangement(pluginInput, pluginOutput);
	}
	public boolean getSpeakerArrangement (VSTSpeakerArrangement pluginInput, VSTSpeakerArrangement pluginOutput) {
		return adapter.getSpeakerArrangement(pluginInput, pluginOutput);
	}

	//provide defaults for vst 2.1 OPTIONAL methods
	//********************************
	public int getMidiProgramName (int channel, MidiProgramName midiProgramName) { return adapter.getMidiProgramName (channel, midiProgramName); }
	public int getCurrentMidiProgram(int channel, MidiProgramName currentProgram) { return adapter.getCurrentMidiProgram(channel, currentProgram); }
	public int getMidiProgramCategory(int channel,MidiProgramCategory category) { return adapter.getMidiProgramCategory(channel, category); }
	public boolean hasMidiProgramsChanged(int channel) { return adapter.hasMidiProgramsChanged(channel); }
	public boolean getMidiKeyName(int channel, MidiKeyName keyName) { return adapter.getMidiKeyName(channel, keyName); }
	public boolean beginSetProgram() { return adapter.beginSetProgram();}
	public boolean endSetProgram() { return adapter.endSetProgram();}


	//provide defaults for vst 2.3 OPTIONAL methods
	//********************************
	public int setTotalSampleToProcess (int value) { return adapter.setTotalSampleToProcess (value); }
	public int getNextShellPlugin(String name) { return adapter.getNextShellPlugin(name); }
	public int startProcess() { return adapter.startProcess(); }
	public int stopProcess() { return adapter.stopProcess(); }


	//provide defaults for vst 2.4 OPTIONAL methods
	//********************************
	public void processDoubleReplacing (double[][] inputs, double[][] outputs, int sampleFrames) {adapter.processDoubleReplacing (inputs, outputs, sampleFrames); }

	public boolean setProcessPrecision (int precision){return adapter.setProcessPrecision (precision);}
	public int getNumMidiInputChannels(){return adapter.getNumMidiInputChannels();}
	public int getNumMidiOutputChannels(){return adapter.getNumMidiOutputChannels();}

}
