import jvst.wrapper.*;
import jvst.wrapper.gui.VSTPluginGUIRunner;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import java.awt.Insets;
import java.awt.BorderLayout;
import java.awt.Color;

import org.jruby.Ruby;

public class JRubyVSTPluginGUIProxy extends VSTPluginGUIAdapter {

	protected Ruby runtime;
	protected VSTPluginAdapter plugin;

	public JRubyVSTPluginGUIProxy(VSTPluginGUIRunner runner, VSTPluginAdapter plugin) throws Exception {
		super(runner,plugin);

		this.setTitle("JRuby GUI proxy is in da house");
		this.setSize(700, 600);

		this.plugin = plugin;
		this.runtime = ((JRubyVSTPluginProxy)plugin).runtime;

		this.init();

		// this is needed on the mac only, java guis are handled there in a pretty different way than on win/linux
		if (RUNNING_MAC_X) this.show();
	}

	public void init() {
		this.getContentPane().setLayout(new BorderLayout());
		this.setSize(700, 600);

		JEditorPane text = new JTextPane();

		text.setMargin(new Insets(8,8,8,8));
		JScrollPane pane = new JScrollPane();
		pane.setViewportView(text);
		pane.setBorder(BorderFactory.createLineBorder(Color.darkGray));
		this.getContentPane().add(pane);
		this.validate();
	}

	// TODO - ask Daniel the goal of this so that I understand
	private static final long serialVersionUID = 374624297323217387L;

}