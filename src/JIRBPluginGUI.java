
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
 
import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
 
import org.jruby.Ruby;
import org.jruby.RubyInstanceConfig;
import org.jruby.internal.runtime.ValueAccessor;
import org.jruby.demo.TextAreaReadline;
 
import jvst.wrapper.VSTPluginAdapter;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;

import jvst.wrapper.*;
import jvst.wrapper.gui.VSTPluginGUIRunner;


 
public class JIRBPluginGUI extends VSTPluginGUIAdapter {
  
  protected Ruby runtime;
  protected VSTPluginAdapter plugin;
  
  
  public JIRBPluginGUI(VSTPluginGUIRunner r, VSTPluginAdapter plug) throws Exception {
	super(r,plug);
    log("JIRBPluginGUI <init>");
	
    this.setTitle("JRuby IRB Console (tab will autocomplete)");
    this.setSize(700, 600);
    //this.setResizable(false);
    
    this.plugin = plug;
	this.runtime = ((JRubyVSTPluginProxy)plug).runtime;
	
    this.init();
    
    //this is needed on the mac only, 
    //java guis are handled there in a pretty different way than on win/linux
    //XXX
    if (RUNNING_MAC_X) this.show();
  }
 
  public void init() {
		this.getContentPane().setLayout(new BorderLayout());
		this.setSize(700, 600);
		
		JEditorPane text = new JTextPane();
		
		text.setMargin(new Insets(8,8,8,8));
		text.setCaretColor(new Color(0xa4, 0x00, 0x00));
		text.setBackground(new Color(0xf2, 0xf2, 0xf2));
		text.setForeground(new Color(0xa4, 0x00, 0x00));
		Font font = this.findFont("Monospaced", Font.PLAIN, 14,
				new String[] {"Monaco", "Andale Mono"});
		
		text.setFont(font);
		JScrollPane pane = new JScrollPane();
		pane.setViewportView(text);
		pane.setBorder(BorderFactory.createLineBorder(Color.darkGray));
		this.getContentPane().add(pane);
		
		this.validate();
		
		final TextAreaReadline tar = new TextAreaReadline(text, 
			" JRuby VST Plugin Console - running plugin instances are in array PLUGS \n\n");
		
		/*
		final RubyInstanceConfig config = new RubyInstanceConfig() {{
			setInput(tar.getInputStream());
			setOutput(new PrintStream(tar.getOutputStream()));
			setError(new PrintStream(tar.getOutputStream()));
			setObjectSpaceEnabled(false); // would be useful for code completion inside the IRB, but BIG PERFORMCE HIT!
			setArgv(new String[]{}); //no args
		}};
		
		final Ruby runtime = Ruby.newInstance(config);
		runtime.getGlobalVariables().defineReadonly("$$", new ValueAccessor(runtime.newFixnum(System.identityHashCode(runtime))));
		runtime.getLoadService().init(new ArrayList());
		
		tar.hookIntoRuntime(runtime);
		*/
		
		tar.hookIntoRuntimeWithStreams(runtime);
		
		//runtime.evalScriptlet("require 'irb'; require 'irb/completion'; IRB.start");
    }
 
	//overrives open() from vstpluginguiadapter
	public void open() {
		this.setVisible(true);
		this.toFront();
	
		//move away from the EDT
		Thread t = new Thread() {
            public void run() {
                runtime.evalScriptlet("require 'irb'; require 'irb/completion'; IRB.start");
            }
        };
		t.start();
	}
 
    private Font findFont(String otherwise, int style, int size, String[] families) {
        String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        Arrays.sort(fonts);
        Font font = null;
        for (int i = 0; i < families.length; i++) {
            if (Arrays.binarySearch(fonts, families[i]) >= 0) {
                font = new Font(families[i], style, size);
                break;
            }
        }
        if (font == null)
            font = new Font(otherwise, style, size);
        return font;
    }
 
    private static final long serialVersionUID = 374624297323217387L;
 
}