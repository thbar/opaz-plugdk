import jvst.wrapper.VSTPluginAdapter;
import jvst.wrapper.VSTPluginGUIAdapter;
import jvst.wrapper.valueobjects.*;

import org.jruby.Ruby;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;


public class JRubyVSTPluginGUIProxy extends VSTPluginGUIAdapter {

	protected JRubyVSTPluginProxy pluginProxy;
	protected VSTPluginGUI guiProxy;
	protected Ruby runtime;
	
	public JRubyVSTPluginGUIProxy(VSTPluginGUIRunner r, VSTPluginAdapter plugin) {
		super(r, plugin);
		pluginProxy = (JRubyVSTPluginProxy) plugin;
		runtime = pluginProxy.runtime;
		
		// Current convention: %RubyPlugin%GUI.rb should define the %RubyPlugin%GUI class that implements VSTPluginGUI
		String rubyPluginGUI = runtime.evalScriptlet("IO.read(\'"+iniFileName+"\').grep(/RubyPluginGUI=(.*)/) { $1 }.first").toString();
		runtime.evalScriptlet("require '"+rubyPluginGUI+"'");

		log("Using Ruby plugin GUI: "+rubyPluginGUI);

		//TODO: give the ruby GUI a reference to the plugin (pluginProxy) and to the JFrame (this)
		Object rfj = runtime.evalScriptlet("GUI = " + rubyPluginGUI + ".new(" + this + ")");
				
		guiProxy = (VSTPluginGUI)JavaEmbedUtils.rubyToJava(runtime, (IRubyObject) rfj, VSTPluginGUI.class);
		
		log("Exiting GUI constructor...");
	}

	
	// forward calls to ruby plugin here in order to allow to be overwritten there (by jruby plug)
	// provide defaults for GUI OPTIONAL methods
	
	public void open() { guiProxy.open(); }
	public void close() { guiProxy.close(); }
	public void destroy() { guiProxy.destroy(); }

}
