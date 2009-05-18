import jvst.wrapper.VSTPluginAdapter;

import org.jruby.Ruby;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;

public class JRubyVSTPluginProxy extends VSTPluginAdapter {

	protected VSTPluginAdapter adapter;
	
	public boolean useMacOSX() {
		String lcOSName = System.getProperty("os.name").toLowerCase();
		return lcOSName.startsWith("mac os x");
	}
	
	public JRubyVSTPluginProxy(long wrapper) {
		super(wrapper);

		// TODO: make configurable (jvstruby.ini or similar)
		// - plugin class
		// - ruby file to load from (or we can autoload all the files available)
		// TODO: redirect stdin, out and err to a file 
		
		String resourcesFolder = getLogBasePath();
		if (useMacOSX()) // mac os x tweak :o
			resourcesFolder += "/../Resources";
		
		Ruby runtime = Ruby.getDefaultInstance();
		String rubySourceFile = resourcesFolder + "/Delay.rb";

		log("Trying to load " + rubySourceFile);
		String rubyCode = runtime.evalScriptlet("File.open('"+ rubySourceFile+"').read").toString();
		runtime.evalScriptlet(rubyCode);
		
		Object rfj = runtime.evalScriptlet("Delay.new("+wrapper+")");
		this.adapter = (VSTPluginAdapter)JavaEmbedUtils.rubyToJava(runtime, (IRubyObject) rfj, VSTPluginAdapter.class);
		
		log("Exiting constructor...");
	}
	
	//convenience for logging in jruby
	public void Log(String msg) {
		log(msg);
	}

  // Note (thbar) - @Override seems to choke on my compiler - commenting them for the moment
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
}
