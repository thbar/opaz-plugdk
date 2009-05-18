import jvst.wrapper.*;
import org.jruby.Ruby;
import org.jruby.runtime.builtin.*;
import org.jruby.javasupport.JavaEmbedUtils;
import java.lang.reflect.*;

public class JRubyProxy extends VSTPluginAdapter {

	public JRubyProxy(long wrapper) {
		super(wrapper);

		log("***************** boot *****************");
		Ruby runtime = Ruby.getDefaultInstance();
		
		// TODO - load relatively to the loader folder
		String rubySourceFile = "/Users/thbar/git/opaz/labs/eclipse-sandbox/Gain/ruby_src/Delay.rb";
		String rubyCode = runtime.evalScriptlet("File.open('"+ rubySourceFile+"').read").toString();
		runtime.evalScriptlet(rubyCode);
		
		Object rfj = runtime.evalScriptlet("Delay.new("+wrapper+")");
		VSTPluginAdapter adapter = (VSTPluginAdapter)JavaEmbedUtils.rubyToJava(runtime, (IRubyObject) rfj, VSTPluginAdapter.class);

		//log("adapter.getClass() => " + adapter.getClass());

 	Method m[] = adapter.getClass().getMethods();
	for (int i = 0; i < m.length; i++)
		System.out.println(m[i].toString());
		
		/*
		log("---- adapter.getClass().getDeclaredMethods() ----");
		Method m[] = adapter.getClass().getDeclaredMethods();
		for (int i = 0; i < m.length; i++)
			System.out.println(m[i].toString());
		
		log("---- adapter.getClass().getDeclaredMethods() ----");
		Field f[] = adapter.getClass().getFields();
		for (int i = 0; i < f.length; i++)
			System.out.println(f[i].toString());
		*/
		
		// communicate with the host
		this.setNumInputs(1);// mono input
		this.setNumOutputs(1);// mono output
		// this.hasVu(false); //deprecated as of vst2.4
		this.canProcessReplacing(true);// mandatory for vst 2.4!
		this.setUniqueID(9876557);// random unique number registered at
									// steinberg (4 byte)

		this.canMono(true);
		log("Exiting constructor...");
	}

	public int canDo(String arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getPlugCategory() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getProductString() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getProgramNameIndexed(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getVendorString() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean setBypass(boolean arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean string2Parameter(int arg0, String arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	public int getNumParams() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getNumPrograms() {
		// TODO Auto-generated method stub
		return 0;
	}

	public float getParameter(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getParameterDisplay(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getParameterLabel(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getParameterName(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getProgram() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getProgramName() {
		// TODO Auto-generated method stub
		return null;
	}

	public void processReplacing(float[][] arg0, float[][] arg1, int arg2) {
		// TODO Auto-generated method stub

	}

	public void setParameter(int arg0, float arg1) {
		// TODO Auto-generated method stub

	}

	public void setProgram(int arg0) {
		// TODO Auto-generated method stub

	}

	public void setProgramName(String arg0) {
		// TODO Auto-generated method stub
	}

}
