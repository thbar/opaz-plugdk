//~--- non-JDK imports --------------------------------------------------------

import jvst.wrapper.VSTPluginAdapter;
import jvst.wrapper.VSTPluginGUIAdapter;
import jvst.wrapper.gui.VSTPluginGUIRunner;

import org.jruby.Ruby;
import org.jruby.demo.TextAreaReadline;

//~--- JDK imports ------------------------------------------------------------

import java.awt.*;

import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

public class IRBPluginGUI extends VSTPluginGUIAdapter {
    private static final long  serialVersionUID = 0x532eed0462e41ebL;
    protected VSTPluginAdapter iplugin;    // refactored this name from 'plugin' due to 'field hidden' warning
    protected Ruby             runtime;

    // constructor so that the IRB GUI can be started from within Ruby as well
    @SuppressWarnings("deprecation")
    public IRBPluginGUI(Ruby r) throws Exception {

        // trick VSTPluginGUIAdapter into DEBUG mode
        super(new VSTPluginGUIRunner(), null);
        log("JIRBPluginGUI <init>");
        this.setTitle("JRuby VST Plugin Console (tab will autocomplete)");
        this.setSize(0x2bc, 0x258);
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.runtime = r;
        this.init();
        this.show();    // always show
    }

    @SuppressWarnings("deprecation")
    public IRBPluginGUI(VSTPluginGUIRunner r, VSTPluginAdapter plug) throws Exception {
        super(r, plug);
        log("JIRBPluginGUI <init>");
        this.setTitle("JRuby VST Plugin Console (tab will autocomplete)");
        this.setSize(0x2bc, 0x258);

        // this.setResizable(false);
        this.iplugin = plug;
        this.runtime = ((JRubyVSTPluginProxy) plug).runtime;
        this.init();

        // this is needed on the mac only,
        // java guis are handled there in a pretty different way than on win/linux
        // XXX
        if (RUNNING_MAC_X) {
            this.show();
        }
    }

    private void init() {
        this.getContentPane().setLayout(new BorderLayout());
        this.setSize(0x2bc, 0x258);

        JEditorPane text = new JTextPane();

        text.setMargin(new Insets(0x8, 0x8, 0x8, 0x8));
        text.setCaretColor(new Color(164, 0, 0));
        text.setBackground(new Color(242, 242, 242));
        text.setForeground(new Color(164, 0, 0));

        Font font = this.findFont("Monospaced", Font.PLAIN, 0xe, new String[] { "Monaco", "Andale Mono" });

        text.setFont(font);

        JScrollPane pane = new JScrollPane();

        pane.setViewportView(text);
        pane.setBorder(BorderFactory.createLineBorder(Color.darkGray));
        this.getContentPane().add(pane);
        this.validate();

        final TextAreaReadline tar = new TextAreaReadline(text,
                                         " The running JRuby VST plugin instance is in variable \"PLUG\" \n\n");

        tar.hookIntoRuntimeWithStreams(runtime);

        // the trick here is to ensure that the call to hookIntoRuntime has completed
        // so that stdin, out and err are redirected properly. Only then start IRB
        // move IRB away from the EDT
        Thread t = new Thread() {
            @Override
            public void run() {

                // trick IRB to think that we are a terminal --> always return true for STDIN.tty?
                // this gives us the :DEFAULT prompt instead of the :NULL prompt
                runtime.evalScriptlet("require 'irb'; require 'irb/completion'; def STDIN.tty?; true; end; IRB.start");
            }
        };

        t.start();
    }

    private Font findFont(String otherwise, int style, int size, String[] families) {
        String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();

        Arrays.sort(fonts);

        Font font = null;

        for (int i = 0x0; i < families.length; i++) {
            if (Arrays.binarySearch(fonts, families[i]) >= 0x0) {
                font = new Font(families[i], style, size);

                break;
            }
        }

        if (font == null) {
            font = new Font(otherwise, style, size);
        }

        return font;
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
