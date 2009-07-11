require 'java'
require 'jruby'

# this is to be able to call the static log() method in VSTPluginAdapter
include_class 'jvst.wrapper.VSTPluginAdapter'
include_class 'jvst.wrapper.communication.VSTV20ToPlug'
include_class 'jvst.wrapper.valueobjects.VSTPinProperties'
include_class 'jvst.wrapper.valueobjects.VSTEvent'

# explicitly disable objectspace
# this improves performance quite substantially
JRuby.objectspace=false

# for some reason, it seems that public static final fields are not accessible from JRuby. This hacks let us access these constants. 
# todo - ask jruby irc channel to see if there is some built-in way to do that ?
# todo - if nothing is available, create a module out of this logic, and include it in relevant classes
class VSTEvent
  class << self
    VSTEvent.constants.each do |constant|
      define_method(constant) { const_get(constant) }
    end
  end
end

class VSTPinProperties
  class << self
    VSTPinProperties.constants.each do |constant|
      define_method(constant) { const_get(constant) }
    end
  end
end

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
        
        # name: ruby symbol for the parameter - a corresponding attr_accessor will be created
        # display_name: the name as shown in the vst host
        # initial_value: the value the parameter will have at plugin startup
        # unit: the unit (eg: %, dB, ms) to display aside the value 
        def param(name, display_name, initial_value, unit = "")
          params << Struct.new(:name, :display_name, :initial_value, :unit).new(name.to_s, display_name, initial_value, unit)
          param_index = params.size - 1
          define_method(name) do
            values[param_index]
          end
          define_method("#{name}=") do |value|
            setParameter(param_index, value)
          end
        end
        
        def editor(editor_class)
          @editor_class = editor_class
        end

        def editor_class
          @editor_class
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

      def editor
        self.class.editor_class
      end

      attr_reader :editor_instance
      def set_gui_instance(inst)
        @editor_instance = inst
      end
      
      def values
        @values ||= self.class.params.map { |e| e.initial_value }
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
        # update gui on parameter change
        if editor_instance!=nil && defined? (editor_instance.setParameter)
          editor_instance.setParameter(index, value)
        end
      end

      def string2Parameter(index,value)
        begin
          setParameter(index, Float(value)) unless value.nil?
          return true
        rescue
          return false
        end
      end
      
      # vst "label" is the unit to be displayed aside the value
      def getParameterLabel(index)
        self.class.params[index].unit
      end
      
      # name is the display name
      # TODO: rename those things
      def getParameterName(index)
        self.class.params[index].display_name
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
      
      attr_accessor :sample_rate      
      def setSampleRate(sample_rate)
        @sample_rate = sample_rate
      end
      
      def setBypass(value)
        false
      end
      
      
      # dummy implementation - avoids error when I reload a set under Live
      def setProgramName(name)
      end
      
      
      # TODO - see how we can inherit static fields like PLUG_CATEG_EFFECT
      # Or (other idea) - recreate these with an idiomatic port (symbols ?)
      def getPlugCategory
        VSTV20ToPlug.PLUG_CATEG_EFFECT
      end
    end
  end
  
  
  def log(msg)
    VSTPluginAdapter.log("JRuby: #{msg}")
  end
  
end

# start defining the class - do not inherit VSTPluginAdapter during tests
if $DISABLE_VSTPLUGINADAPTER_INHERIT
  class OpazPlug; end
else
  class OpazPlug < Java::jvst.wrapper.VSTPluginAdapter; end
end

# carry on with regular class definition
class OpazPlug
  include Plug
  
  def initialize(wrapper)
    super
    log("Booting #{getEffectName}:#{getProductString}:#{getVendorString}")
    setNumInputs(1) # TODO: add a way to override the default (1x1) using declarative statement
    setNumOutputs(1)
    canProcessReplacing(true)
    setUniqueID(unique_id)
  end
  
  # forward calls - TODO: not sure how costly it is, in context. Check if it's worth it. Try making alias_method work.
  def processReplacing(inputs, outputs, sampleFrames)
    process(inputs, outputs, sampleFrames)
  end
  
end