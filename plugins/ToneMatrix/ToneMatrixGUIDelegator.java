import java.awt.BorderLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import jvst.wrapper.VSTPluginAdapter;
import jvst.wrapper.gui.VSTPluginGUIRunner;

import jvst.wrapper.VSTPluginGUIAdapter;

import com.sun.javafx.tk.FontLoader;

public class ToneMatrixGUIDelegator extends VSTPluginGUIAdapter {

    protected static boolean DEBUG = false;
    protected static final String FX_GUI_CLASS = "ToneMatrixGUI";

    
    public ToneMatrixGUIDelegator(VSTPluginGUIRunner runner, VSTPluginAdapter plug) {
        super(runner, plug);
        try {
            log("ToneMatrixGUIDelegator <init>");

            //windows properties
            this.setTitle("ToneMatrixGUIDelegator");
            this.setSize(290, 150);
            this.setResizable(false);

            log("ToneMatrixGUIDelegator <init2>");
            //use SceneToJComponent
            JComponent s = SceneToJComponent.loadVSTPluginScene2(FX_GUI_CLASS, plug);

            this.setLayout(new BorderLayout(10,10));
            this.add(s, BorderLayout.CENTER);

            log("ToneMatrixGUIDelegator <init3>");
            //this is needed on the mac only,
            //java guis are handled there in a pretty different way than on win/linux
            //XXX
            if (RUNNING_MAC_X) {
                this.show();
            }
            log("ToneMatrixGUIDelegator <done-init>");
        }
        catch (Exception ex) {
            log("** ERROR: Fatal error when loading JavaFX GUI: " + ex.toString());
            ex.printStackTrace();
        } 
    }


    /*
     * main method for convenient GUI debugging
     */
    public static void main(String[] args) throws Throwable {
        DEBUG = true;

        ToneMatrixGUIDelegator gui = new ToneMatrixGUIDelegator(new VSTPluginGUIRunner(), null);
        gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gui.show();
    }
    
}
