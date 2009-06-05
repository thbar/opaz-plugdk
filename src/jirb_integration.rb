require 'java'
require 'jruby'
require 'irb'
require 'irb/completion'

# adapted from bin/jirb_swing

class FrameBringer
	include java.lang.Runnable
	def initialize(frame)
		@frame = frame
	end
	def run
		@frame.visible = true
	end
end

def find_font(otherwise, style, size, *families)
	avail_families = java.awt.GraphicsEnvironment.local_graphics_environment.available_font_family_names
	fontname = families.find(proc {otherwise}) { |name| avail_families.include? name }
	java.awt.Font.new(fontname, style, size)
end

def setup_jirb
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
	
	JRuby.objectspace = false # useful for code completion --> BIG performance hit! --> set to false
	
	tar.hook_into_runtime_with_streams(JRuby.runtime)
	java.awt.EventQueue.invoke_later(FrameBringer.new(frame))
	
	#sleep(2)
    #IRB.conf[:VERBOSE] = true
	#IRB.conf[:PROMPT_MODE] = :DEFAULT
	#IRB.conf[:PROMPT_MODE] = :SIMPLE
#	IRB.conf[:PROMPT][:NULL] = {
#		:PROMPT_I => "%N(%m):%03n:%i> ",
#		:PROMPT_N => "%N(%m):%03n:%i> ",
#		:PROMPT_S => "%N(%m):%03n:%i%l ",
#		:PROMPT_C => "%N(%m):%03n:%i* ",
#		:RETURN => "=> %s\n"
#	}
	
	# IRB on windows always uses the :NULL prompt since STDIN.tty? returns 
	# false. The IRB then automatically falls back to the :NULL prompt (see also IRB sourcecode)
	# So, unfortunately, only the :NULL promt is possible on windows (linux and macos use the 
	# :DEFAULT prompt instead)
	# see /ruby/1.8/irb/init.rb
	
	IRB.start
end
  
def display_jirb
	#use separate thread, otherwise execution will be blocked
	irb_thread = Thread.new {
		setup_jirb
	}
end