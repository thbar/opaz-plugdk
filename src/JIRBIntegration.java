
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
 
 
public class JIRBIntegration extends JFrame {
    public JIRBIntegration(String title) {
        super(title);
    }
 
    static Ruby startRuby(boolean attachJIRB) {
	
		if (attachJIRB) {
			VSTPluginAdapter.log("Attaching JIRB...");
			
			final JIRBIntegration console = new JIRBIntegration("JRuby IRB Console (tab will autocomplete)");
			
			console.getContentPane().setLayout(new BorderLayout());
			console.setSize(700, 600);
			
			//do nothing on close (especially, we do not want to terminate the JVM (this would be the default!))
			console.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
			
			JEditorPane text = new JTextPane();
			
			text.setMargin(new Insets(8,8,8,8));
			text.setCaretColor(new Color(0xa4, 0x00, 0x00));
			text.setBackground(new Color(0xf2, 0xf2, 0xf2));
			text.setForeground(new Color(0xa4, 0x00, 0x00));
			Font font = console.findFont("Monospaced", Font.PLAIN, 14,
					new String[] {"Monaco", "Andale Mono"});
			
			text.setFont(font);
			JScrollPane pane = new JScrollPane();
			pane.setViewportView(text);
			pane.setBorder(BorderFactory.createLineBorder(Color.darkGray));
			console.getContentPane().add(pane);
			console.validate();
			
			final TextAreaReadline tar = new TextAreaReadline(text, " JRuby VST Plugin Console - running plugin instances are in array PLUGS \n\n");
			
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
			
			//why not run on the EDT???
			//@Thibaut, you may need to change this to run on the EDT (java.awt.EventQueue.invoke_later(FrameBringer.new()))
			Thread t2 = new Thread() {
				public void run() {
					console.setVisible(true);
					runtime.evalScriptlet("require 'irb'; require 'irb/completion'; IRB.start");
				}
			};
			t2.start();
			
			return runtime;
		}
		else {
			Ruby runtime = Ruby.getDefaultInstance();
			return runtime;
		}
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