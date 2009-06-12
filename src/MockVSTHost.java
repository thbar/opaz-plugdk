
public class MockVSTHost {
	
	//TODO: make platform aware and generalize
	private static final String NATIVE_LIB_LOCATION = "C:/@DATA/private/jvst/jvst_native/src/Release/jvstwrapper.dll";
	
	
	public static void main(String[] args) {
		//System.load(NATIVE_LIB_LOCATION);
		//JRubyVSTPluginProxy.setIsLogEnabled(true);//this only works if using JVSTPluginAdapter.java from the CVS HEAD
													//enables logging to be send directly to stdout instead of a file
		
		JRubyVSTPluginProxy._initPlugFromNative(NATIVE_LIB_LOCATION, true); //enable logging (this creates the _java_stdout.txt file as usual)
		JRubyVSTPluginProxy p = new JRubyVSTPluginProxy(0);
		//NOTE: because 0 passed as the reference to the native counterpart of the VST plugin, all plug-->host calls 
		//      (e.g. this.setNumInputs(1)) are ignored. Using any other value than 0 immediately causes crashes since 
		//      there is no VST plugin instance at this address in memory (see VSTV10ToHost.cpp)
		
		
		//TODO: plugin init seqence here (suspend, resume, ...)
		
		
		//process some audio (host --> plug)
		p.processReplacing(new float[][]{{}}, new float[][]{{}}, 0);
	}
	
}