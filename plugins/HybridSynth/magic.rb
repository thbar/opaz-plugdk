# compressed version of magic - do not modify


# content for lib/magic/classifier.rb

# ability to transform from snake_case (ruby) to CamelCase (.Net)
# maybe there is an IronRuby built-in we could rely on for that ?
module Classifier
  def classify(string)
    string.gsub(/(^|_)(.)/) { $2.upcase } # simplified version of Rails inflector
  end
end

# content for lib/magic/enum_support.rb

# temporary code to support flagged enums (AnchorStyles.Bottom | AnchorStyles.Top)
# this code will be removed when | is implemented on enums in IronRuby
module EnumSupport
  def enum_to_int(enval)
    entype = enval.GetType()
    undertype = Enum.GetUnderlyingType(entype)
    Convert.ChangeType(enval, undertype)
  end
  
  def int_to_enum(entype, value)
    Enum.ToObject(entype, value)
  end
end

# content for lib/magic/instance_creator.rb

# disabled: require 'magic/classifier'
# disabled: require 'magic/enum_support'

# internal sugar to instanciate a given CLR type - apart from enum handling, the code is not CLR specific
module InstanceCreator
  include Classifier
  include EnumSupport
  
  # if klass.property_name is a CLR enum, parse the value to translate it into the CLR enum value
  def parse_enum_if_enum(klass,property_name,value)
    is_dotnet_enum = klass.respond_to?(:to_clr_type) && (type = klass.to_clr_type.get_property(classify(property_name.to_s)).property_type) && (type.is_enum)
    if is_dotnet_enum
      if value.is_a?(Array)
        int_values = value.map { |e| enum_to_int(parse_enum(type,e)) }
        or_combination = int_values.inject { |result,e| result | e }
        int_to_enum(type, or_combination)
      elsif value.is_a?(Symbol)
        parse_enum(type,value)
      else
        value
      end
    else
      value
    end
  end
    
  def parse_enum(type,value)
    System::Enum.parse(type, classify(value.to_s))
  end
  
  # return the method name to call to set the property, if there is one. Can be either:
  # - a .Net property setter
  # - or a Java setProperty(x) setter
  def setter?(instance, property_name)
    # detect set_x first for java - the second one seems to be detected but fails to work, for some reason
    ["set_#{property_name}", "#{property_name}="].find { |m| instance.respond_to?(m) }
  end
  
  # instanciate the given class and set the properties passed as options
  # support both values and procs for options
  def build_instance_with_properties(klass,*args)
    properties = args.last.is_a?(Hash) ? args.delete_at(args.size-1) : {}
    instance = klass.new(*args)
    properties.keys.inject(instance) { |instance,k| set_property(instance, k, properties[k]) }
  end
  
  # sugarized property setter - allows symbols for enums
  def set_property(instance, k, v)
    setter = setter?(instance, k)
    v.is_a?(Proc) ? instance.send(k,&v) : instance.send(setter, *parse_enum_if_enum(instance.class,k,v))
    instance
  end

end


# content for lib/magic.rb

# disabled: require File.dirname(__FILE__) + "/magic/instance_creator"
# disabled: require File.dirname(__FILE__) + "/magic/classifier"

# DSL-like object creation. Not that much .Net related, except for the Control/MenuItem specifics
# which could be extracted and made configurable. This is likely to happen.
class Magic
  include InstanceCreator
  include Classifier

  class << self
    def build(&description)
      self.new.instance_eval(&description)
    end
  end

  def method_missing(method,*args)
    # push stuff recursively on a stack so that we can add the item to its parents children collection
    @stack ||= []
    parent = @stack.last

    if setter?(parent, method)
      set_property(parent, method, args)
    else
      clazz = Object.const_get(classify(method.to_s))
      instance = build_instance_with_properties(clazz, *args)
      # add to the parent control only if it's a well known kind
      # todo - extract configurable mappings ?
      if parent
        # serve ourselves, first
        parent.send(method,args) if parent.respond_to?(method)
        # Windows Forms Control and MenuItem support
        parent.controls.add(instance) if (defined?(System::Windows::Forms::Control) && instance.is_a?(System::Windows::Forms::Control))
        parent.menu_items.add(instance) if (defined?(System::Windows::Forms::MenuItem) && instance.is_a?(System::Windows::Forms::MenuItem))
        # Silverlight/WPF support
        if defined?(System::Windows::UIElement)
          if instance.is_a?(System::Windows::UIElement)
            if parent.respond_to?(:content)
              parent.content = instance
            else
              parent.children.add(instance)
            end
          end
        end
        # Swing support
        parent.add(instance) if (defined?(Java::JavaAwt::Component) && instance.is_a?(Java::JavaAwt::Component))
      end
      @stack.push(instance)
      yield instance if block_given?
      @stack.pop
    end
  end
end
