import jvst.wrapper.*;
import jvst.wrapper.gui.VSTPluginGUIRunner;

import org.jruby.Ruby;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;

public class JRubyVSTPluginGUIProxy extends VSTPluginGUIAdapter {

  protected Ruby runtime;
  protected JRubyVSTPluginProxy plugin;

  public JRubyVSTPluginGUIProxy(VSTPluginGUIRunner runner, VSTPluginAdapter plugin) throws Exception {
    super(runner,plugin);

    this.plugin = (JRubyVSTPluginProxy)plugin;
    this.runtime = this.plugin.runtime;

    // ask the plugin which is the ruby editor class
    IRubyObject rubyPlugin = this.plugin.getRubyPlugin();
    IRubyObject rubyEditorClass = (IRubyObject)JavaEmbedUtils.invokeMethod(runtime, rubyPlugin, "editor", 
      new Object[] {}, IRubyObject.class);
    if (rubyEditorClass==null) {
      this.setTitle("No GUI defined (variable editor) in the JRuby plugin class");
      this.setSize(300,200);
      log("* WARNING: no gui defined in the jruby plugin class. Variable editor must not be nil");
      return; //die silently when no editor is defined
    }
    
    // Use JavaEmbedUtils.invokeMethod so that we're able to pass a Java instance (this) to the JRuby constructor
    // Note: having a null object for the class seems to be fine, which allows us to support the case where no editor is specified. Fix ?
    Object gui = JavaEmbedUtils.invokeMethod(runtime, rubyEditorClass, "new", 
      new Object[] { 
        JavaEmbedUtils.javaToRuby(runtime, rubyPlugin), 
        JavaEmbedUtils.javaToRuby(runtime, this)
      }, IRubyObject.class);
    
    // write the gui instance back to the ruby plugin class
    JavaEmbedUtils.invokeMethod(runtime, rubyPlugin, "set_gui_instance", 
      new Object[] { 
        JavaEmbedUtils.javaToRuby(runtime, gui)
      }, IRubyObject.class);
    
    // this is needed on the mac only, java guis are handled there in a pretty different way than on win/linux
    if (RUNNING_MAC_X) this.show();
  }

  // TODO - ask Daniel the goal of this so that I understand ?
  private static final long serialVersionUID = 374624212343217387L;
}
