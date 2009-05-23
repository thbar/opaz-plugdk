require 'java'

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
        
        def param(name, label, default_value)
          @params ||= []
          @params << Struct.new(:name, :label, :default_value).new(name.to_s, label, default_value)
          param_index = @params.size - 1
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
          @abilities
        end
        
        def params
          @params
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

if $DISABLE_VSTPLUGINADAPTER_INHERIT # temp hack for testing
  class OpazPlug
    include Plug
  end
else
  class OpazPlug < Java::jvst.wrapper.VSTPluginAdapter
    include Plug
  end
end
