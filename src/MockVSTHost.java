
public class MockVSTHost {
	
	public static void main(String[] args) {
		//satisfy jvstwrapper native methods
		//TODO: make platform aware...
		System.load("C:/@DATA/private/jvst/jvst_native/src/Release/jvstwrapper.dll");
		
		//directly instantiate plugin
		//JRubyVSTPluginProxy.setIsLogEnabled(true);//this only works if using JVSTPluginAdapter.java from the CVS HEAD
		JRubyVSTPluginProxy p = new JRubyVSTPluginProxy(0);
		
		//NOTE: because 0 being passed as the reference to the native counterpart of the VST plugin should cause all plug-->host calls 
		//      (e.g. this.setNumInputs(1)) to be ignored. Using any other value than 0 immediately caused crashes since 
		//      this there is no VST plugin instance at this memory address (see e.g. VSTV10ToHost.cpp)
		
		//process some audio (host --> plug)
		p.processReplacing(new float[][]{{}}, new float[][]{{}}, 0);
	}
	
}