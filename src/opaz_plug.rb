require 'java'
require 'jruby'
require 'irb'
require 'irb/completion'

# this is to be able to call the static log() method in VSTPluginAdapter
include_class 'jvst.wrapper.VSTPluginAdapter'
include_class 'jvst.wrapper.communication.VSTV20ToPlug'

module Plug
  def self.included(base)
    base.class_eval do
      class << self
        def plugin(effect, product, vendor)
          define_method :getEffectName do effect end
          define_method :getProductString do product end
          define_method :getVendorString do vendor end
        end

        def unique_id(unique_id)
          if unique_id.is_a?(String)
            raise "unique_id must be a 4-characters string, or an int" unless unique_id.size == 4
            unique_id = (unique_id[0] << 24) | (unique_id[1] << 16) | (unique_id[2] << 8) | unique_id[3]
          end
          define_method :unique_id do unique_id end
        end
        
        def param(name, label, default_value)
          params << Struct.new(:name, :label, :default_value).new(name.to_s, label, default_value)
          param_index = params.size - 1
          define_method(name) do
            values[param_index]
          end
          define_method("#{name}=") do |value|
            values[param_index] = value
          end
        end
        
        def can_do(*abilities)
          @abilities = abilities
        end
        
        def abilities
          @abilities ||= []
        end
        
        def params
          @params ||= []
        end
      end

      def values
        @values ||= self.class.params.map { |e| e.default_value }
      end
      
      def canDo(feature)
        self.class.abilities.include?(feature) ? +1 : -1
      end
      
      def getParameter(index)
        values[index]
      end
      
      def getProgramNameIndexed(category, index)
        "Prog: cat #{category}, #{index}"
      end
      
      def getParameterDisplay(index)
        sprintf("%1.2f", getParameter(index))
      end

      def setParameter(index, value)
        values[index] = value
      end

      def string2Parameter(index,value)
        begin
          setParameter(index, Float(value)) unless value.nil?
          return true
        rescue
          return false
        end
      end
      
      def getParameterLabel(index)
        self.class.params[index].label
      end
      
      def getParameterName(index)
        self.class.params[index].name
      end
      
      def getNumParams
        self.class.params.size
      end
      
      def getNumPrograms
        0 # TODO : implement programs support
      end
      
      def getProgram
        0
      end

    	def getProgramName
    		"Default"
    	end
      
      def setSampleRate(sample_rate)
        @sample_rate = sample_rate
      end

      attr_accessor :sample_rate
      
      def setBypass(value)
        false
      end
      
      def log(msg)
        VSTPluginAdapter.log("JRuby: #{msg}")
      end

      # TODO - see how we can inherit static fields like PLUG_CATEG_EFFECT
      # Or (other idea) - recreate these with an idiomatic port (symbols ?)
      def getPlugCategory
        VSTV20ToPlug.PLUG_CATEG_EFFECT
      end
    end
  end
end

# start defining the class - do not inherit VSTPluginAdapter during tests
if $DISABLE_VSTPLUGINADAPTER_INHERIT
  class OpazPlug; end
else
  class OpazPlug < Java::jvst.wrapper.VSTPluginAdapter; end
end

def find_font(otherwise, style, size, *families)
  avail_families = java.awt.GraphicsEnvironment.local_graphics_environment.available_font_family_names
  fontname = families.find(proc {otherwise}) { |name| avail_families.include? name }
  java.awt.Font.new(fontname, style, size)
end

class FrameBringer
  include java.lang.Runnable
  def initialize(frame)
    @frame = frame
  end
  def run
    @frame.visible = true
  end
end

# carry on with regular class definition
class OpazPlug
  include Plug
  
  def jirb_swing()
	text = javax.swing.JTextPane.new
	text.font = find_font('Monospaced', java.awt.Font::PLAIN, 14, 'Monaco', 'Andale Mono')
	text.margin = java.awt.Insets.new(8,8,8,8)
	text.caret_color = java.awt.Color.new(0xa4, 0x00, 0x00)
	text.background = java.awt.Color.new(0xf2, 0xf2, 0xf2)
	text.foreground = java.awt.Color.new(0xa4, 0x00, 0x00)
	pane = javax.swing.JScrollPane.new
	pane.viewport_view = text
	frame = javax.swing.JFrame.new('JRuby IRB Console (tab will autocomplete)')
	frame.default_close_operation = javax.swing.JFrame::DO_NOTHING_ON_CLOSE
	frame.set_size(700, 600)
	frame.content_pane.add(pane)
	tar = org.jruby.demo.TextAreaReadline.new(text,
	      " Welcome to the JRuby IRB Console [#{JRUBY_VERSION}] \n\n")
	JRuby.objectspace = false # useful for code completion --> BIG performance hit! --> set to false
	tar.hook_into_runtime_with_streams(JRuby.runtime)
	java.awt.EventQueue.invoke_later(FrameBringer.new(frame))
  end

  def initialize(wrapper)
    super
    log("Booting #{getEffectName}:#{getProductString}:#{getVendorString}")
    setNumInputs(1) # TODO: add a way to override the default (1x1) using declarative statement
    setNumOutputs(1)
    canProcessReplacing(true)
    setUniqueID(unique_id)
    irb_thread = Thread.new {
    	jirb_swing()
    	IRB.start()
    }
  end
  
  # forward calls - TODO: not sure how costly it is, in context. Check if it's worth it. Try making alias_method work.
  def processReplacing(inputs, outputs, sampleFrames)
    process(inputs, outputs, sampleFrames)
  end
  
end