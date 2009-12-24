import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import java.io.PrintWriter;
import java.io.StringWriter;

import jvst.wrapper.VSTPluginAdapter;
import jvst.wrapper.VSTPluginGUIAdapter;
import jvst.wrapper.valueobjects.*;

import org.jruby.Ruby;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;

public class JRubyVSTPluginProxy extends VSTPluginAdapter {

  protected VSTPluginAdapter adapter;
  protected IRubyObject rubyPlugin;
  protected Ruby runtime;

  public JRubyVSTPluginProxy(long wrapper) {
    super(wrapper);
    
    //This creates a new ruby interpreter instance for each instance of the plug
    //defaults are used, eg. out from the running java program (which is *_java_stdout.txt :-))
    runtime = Ruby.newInstance();
    String resourcesFolder = ProxyTools.getResourcesFolder(getLogBasePath());
    String iniFileName = ProxyTools.getIniFileName(resourcesFolder, getLogFileName());

    //log("Res folder=" + resourcesFolder);
    //log("Ini file=" + iniFileName);
    //log("wrapper=" + wrapper);
    
    // TODO: learn how to set a variable on the scope instead of hacking it this way:
    // runtime.getGlobalVariables().set("$stderr", out);
    runtime.evalScriptlet("PLUGIN_RESOURCES_FOLDER = '"+resourcesFolder+"'");
    runtime.evalScriptlet("PLUGIN_INI_FILE_NAME = '"+iniFileName+"'");
    runtime.evalScriptlet("PLUGIN_WRAPPER = "+wrapper);
    runtime.evalScriptlet("require 'opaz_bootstrap'");
    
    String reload = runtime.evalScriptlet("IO.read(PLUGIN_INI_FILE_NAME).grep(/^ReloadRubyOnChanges=(.*)/) { $1 }.first.strip").toString();
    log("reload=" + reload);
    
    this.rubyPlugin = (IRubyObject)runtime.evalScriptlet("PLUG");
    this.adapter = (VSTPluginAdapter)JavaEmbedUtils.rubyToJava(runtime, rubyPlugin, VSTPluginAdapter.class);
    
    
    //start watcher thread that looks for changes in ruby files, starts a new ruby interpreter, 
    //loads the changed ruby part and switches the references
    try {
      if ("1".equals(reload)) (new Watcher(wrapper, this)).start();
    } catch (Exception e) { e.printStackTrace(); }
  }
  
  
  private class Watcher extends Thread {
    Hashtable<File, Long> toWatch = new Hashtable<File, Long>();
    
    long wrapper = 0;
    JRubyVSTPluginProxy proxy = null;
    
    public Watcher(long w, JRubyVSTPluginProxy p) throws Exception {
      wrapper = w;
      proxy = p;
      
      File f = new File(ProxyTools.getResourcesFolder(getLogBasePath()));
      File[] files = f.listFiles(new FilenameFilter() {
        public boolean accept(File dir, String name) {return name.endsWith(".rb");}
      });
      for (File file : files) {
        //log("watching " + file.getName() + " lastmod=" + file.lastModified());
        toWatch.put(file, file.lastModified());
      }
    }
    
    public void run() {
      boolean modified = false;    	  
      
      while(true) {
        //log("new round");
        try {
          for (File file : toWatch.keySet()) {
            //log(file.lastModified() + " > " + toWatch.get(file));
            if (file.lastModified() > toWatch.get(file)) {
              log("### File: " + file.getName() + " was just modified!");
              toWatch.put(file, file.lastModified());
              modified = true;
            }
          }
          if (modified) reloadPlugin();    		  
          modified = false;
          Thread.sleep(1000);
        }
        catch (Exception e) {
         //log("exception!");
          e.printStackTrace();
        }
      }
    }
    
    private void reloadPlugin() {
      //TODO: check if the ruby plugin has a GUI (plugin.editor!=nil)
      //      if yes, re-run the GUI constructor and switch GUI references as well
      //      --> see JRubyVSTPluginGUIProxy.java (TODO: implement an equivalent void reloadPlugin() there)
      //
      //For now, the GUI instance is not reloaded. It will stay as it is, but it will of 
      //course use the reloaded plugin instances
      
      try {
        Ruby newRuntime = Ruby.newInstance();
        String resourcesFolder = ProxyTools.getResourcesFolder(getLogBasePath());
        String iniFileName = ProxyTools.getIniFileName(resourcesFolder, getLogFileName());
      
        newRuntime.evalScriptlet("PLUGIN_RESOURCES_FOLDER = '"+resourcesFolder+"'");
        newRuntime.evalScriptlet("PLUGIN_INI_FILE_NAME = '"+iniFileName+"'");
        newRuntime.evalScriptlet("PLUGIN_WRAPPER = "+wrapper);
        newRuntime.evalScriptlet("require 'opaz_bootstrap'");
        
        //oh dear, we switch references to the modified ruby plugin while _RUNNING_ :-)
        log("Switching refs!");
        //disable process() calls on the old plugin version
        proxy.runtime.evalScriptlet("$PLUGIN_IS_RELOADING = true");
        proxy.rubyPlugin = (IRubyObject)newRuntime.evalScriptlet("PLUG");
        proxy.adapter = (VSTPluginAdapter)JavaEmbedUtils.rubyToJava(newRuntime, rubyPlugin, VSTPluginAdapter.class);
        proxy.runtime = newRuntime;
        log("all good :-)");
      }
      catch (Throwable t) {
        //enable process() calls on the old plugin again
        proxy.runtime.evalScriptlet("$PLUGIN_IS_RELOADING = false");
        
        //report error to logfile (e.g. syntax error in the modified files...)
        //do not switch refs, we still use the old version of the ruby plugin we were able to load before
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        log(sw.toString()); // first, report error to file, then show dialog
        //show error dialog
        JOptionPane.showMessageDialog(null, sw.toString(), "Ruby error - Using the previously running Ruby plugin instead", JOptionPane.ERROR_MESSAGE);
      }
    }
  }
  
  
  
  
  // TODO - check if there is some way to grab back the IRubyObject from this.adapter instead
  public IRubyObject getRubyPlugin() {
    return rubyPlugin;
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