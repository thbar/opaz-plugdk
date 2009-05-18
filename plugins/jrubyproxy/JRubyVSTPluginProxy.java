package loader;
import jvst.wrapper.VSTPluginAdapter;

import org.jruby.Ruby;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;

public class JRubyVSTPluginProxy extends VSTPluginAdapter {

	protected VSTPluginAdapter adapter;
	
	public JRubyVSTPluginProxy(long wrapper) {
		super(wrapper);

		// TODO: redirect stdin, out and err to a file 
		log("***************** boot *****************");
		Ruby runtime = Ruby.getDefaultInstance();
		
		// TODO - load relatively to the loader folder
		// TODO: make configurable (jvstruby.ini or similar)
		//String rubySourceFile = "/Users/thbar/git/opaz/labs/eclipse-sandbox/Gain/ruby_src/Delay.rb";
		String rubySourceFile = "C:/@DATA/private/jvst/opaz-plugdk/src/Delay.rb";
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

	@Override
	public int canDo(String arg0) {
		return adapter.canDo(arg0);
	}

	@Override
	public int getPlugCategory() {
		return adapter.getPlugCategory();
	}

	@Override
	public String getProductString() {
		return adapter.getProductString();
	}

	@Override
	public String getProgramNameIndexed(int arg0, int arg1) {
		return adapter.getProgramName();
	}

	@Override
	public String getVendorString() {
		return adapter.getVendorString();
	}
	
	@Override
	public boolean setBypass(boolean arg0) {
		return adapter.setBypass(arg0);
	}

	@Override
	public boolean string2Parameter(int arg0, String arg1) {
		return adapter.string2Parameter(arg0, arg1);
	}

	@Override
	public int getNumParams() {
		return adapter.getNumParams();
	}

	@Override
	public int getNumPrograms() {
		return adapter.getNumPrograms();
	}

	@Override
	public float getParameter(int arg0) {
		return adapter.getParameter(arg0);
	}

	@Override
	public String getParameterDisplay(int arg0) {
		return adapter.getParameterDisplay(arg0);
	}

	@Override
	public String getParameterLabel(int arg0) {
		return adapter.getParameterLabel(arg0);
	}

	@Override
	public String getParameterName(int arg0) {
		return adapter.getParameterName(arg0);
	}

	@Override
	public int getProgram() {
		return adapter.getProgram();
	}

	@Override
	public String getProgramName() {
		return adapter.getProgramName();
	}

	@Override
	public void processReplacing(float[][] arg0, float[][] arg1, int arg2) {
		adapter.processReplacing(arg0, arg1, arg2);
	}

	@Override
	public void setParameter(int arg0, float arg1) {
		adapter.setParameter(arg0, arg1);
	}

	@Override
	public void setProgram(int arg0) {
		adapter.setProgram(arg0);
	}

	@Override
	public void setProgramName(String arg0) {
		adapter.setProgramName(arg0);
	}
}
