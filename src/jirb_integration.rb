require 'java'
require 'jruby'
require 'irb'
require 'irb/completion'

# adapted from the jruby distro: bin/jirb_swing

class FrameBringer
	include java.lang.Runnable
	
	def run
		text = javax.swing.JTextPane.new
		text.font = find_font('Monospaced', java.awt.Font::PLAIN, 14, 'Monaco', 'Andale Mono')
		text.margin = java.awt.Insets.new(8,8,8,8)
		text.caret_color = java.awt.Color.new(0xa4, 0x00, 0x00)
		text.background = java.awt.Color.new(0xf2, 0xf2, 0xf2)
		text.foreground = java.awt.Color.new(0xa4, 0x00, 0x00)
		
		pane = javax.swing.JScrollPane.new
		pane.viewport_view = text
		
		frame = javax.swing.JFrame.new("JRuby v#{JRUBY_VERSION} IRB Console (tab will autocomplete)")
		frame.default_close_operation = javax.swing.JFrame::DO_NOTHING_ON_CLOSE
		frame.set_size(700, 600)
		frame.content_pane.add(pane)
		
		tar = org.jruby.demo.TextAreaReadline.new(text,
			  " JRuby VST Plugin Console - running plugin instances are in array PLUGS \n\n")
		
		JRuby.objectspace = false # useful for code completion, but BIG performance hit! --> set to false
		
		tar.hook_into_runtime_with_streams(JRuby.runtime)
		
		# show frame
		frame.visible = true
	end
end

def find_font(otherwise, style, size, *families)
	avail_families = java.awt.GraphicsEnvironment.local_graphics_environment.available_font_family_names
	fontname = families.find(proc {otherwise}) { |name| avail_families.include? name }
	java.awt.Font.new(fontname, style, size)
end

# trick IRB to think that we are a terminal --> now uses :DEFAULT prompt instead of the :NULL prompt
def STDIN.tty?
	true
end

def display_jirb	
	# do swing stuff on the EDT (event dispatch thread)
	java.awt.EventQueue.invoke_later(FrameBringer.new())
	
	# or just on a separate thread
	#irb_thread = Thread.new {
	#	FrameBringer.new().run()
	#}
	
	# need to wait a little to ensure that "tar.hook_into_runtime_with_streams(JRuby.runtime)" is completet
	sleep(1)
		
	IRB.start
end
