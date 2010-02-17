require 'java'

$LOAD_PATH << 'src'
$LOAD_PATH << 'libs'
$LOAD_PATH << File.dirname(__FILE__)
$DISABLE_VSTPLUGINADAPTER_INHERIT = true # avoid inheritance in opaz_plug.rb

require 'java'
require 'jVSTwRapper-1.0beta.jar'
require 'jVSTsYstem-1.0beta.jar'
require 'opaz_plug'
require 'dummy_plug'

describe OpazPlug do
  attr_reader :plugin
  before(:each) { @plugin = DummyPlug.new(0) }

  it "returns plugin info" do
    plugin.getEffectName.should == "DummyPlug"
    plugin.getProductString.should == "Opaz"
    plugin.getVendorString.should == "LoGeek"
  end
  
  it "memorizes its unique_id" do
    expected_id = 'opaz'
    plugin.unique_id.should == 1869635962 # hand-coded value, computed using java 
  end

  it "stores and retrieves sample rate" do
    plugin.setSampleRate(44800)
    plugin.sample_rate.should == 44800
  end
  
  it "responds to setBypass" do
    plugin.setBypass(true).should == false
  end
  
  it "responds to canDo" do
    plugin.canDo("plugAsSend").should == 1
    plugin.canDo("plugAsChannelInsert").should == 1
    plugin.canDo("1in1out").should == 1
    
    plugin.canDo("8in8out").should == -1
  end
  
  it "responds to getNumParams" do
    plugin.getNumParams.should == 3
  end
  
  it "responds to getNumPrograms" do
    plugin.getNumPrograms.should == 0
  end
  
  it "responds to getParameterName" do
    plugin.getParameterName(0).should == 'Cut Off'
  end

  it "defaults to empty for getParameterLabel" do
    plugin.getParameterLabel(0).should == ""
  end
      
  it "responds to getParameterDisplay" do
    plugin.getParameterDisplay(0).should == "1.00"
  end

  it "support specifying unit type for getParameterLabel" do
    plugin.getParameterLabel(1).should == "resograms"
  end
  
  it "memorize parameter range if specified" do
    plugin.getParameterRange(1).should == (0.0..1.0)
    plugin.getParameterRange(2).should == (-60..6)
  end

  describe "when using ranges on parameters" do
    
    it "translates the vst 0..1 range to plugin range on setParameter" do
      plugin.setParameter(2, 0.0)
      plugin.getParameter(2).should == 0.0
      plugin.ratio.should == -60

      plugin.setParameter(2, 1.0)
      plugin.getParameter(2).should == 1.0
      plugin.ratio.should == +6
    end
  
    it "translates the plugin range to vst 0..1 range on attr_writer" do
      plugin.ratio = -60
      plugin.getParameter(2).should == 0.0

      plugin.ratio = +6
      plugin.getParameter(2).should == 1.0
    end
  
    it "set initial value properly" do
      plugin.getParameter(2).should == 1.0
    end
    
  end

  it "set initial value properly" do
    plugin.getParameter(0).should == 1.0
  end
  
  it "understands setParameter and getParameter" do
    plugin.setParameter(0, 0.4)
    plugin.setParameter(1, 0.2)
    plugin.getParameter(0).should == 0.4
    plugin.getParameter(1).should == 0.2
    
    plugin.cut_off.should == plugin.getParameter(0)
    plugin.resonance.should == plugin.getParameter(1)
  end

  it "implements string2Parameter" do
    plugin.string2Parameter(1, 0.5).should == true
    plugin.resonance.should == 0.5
  end

  it "responds to cut-off" do
    plugin.cut_off = 0.4
    plugin.cut_off.should == 0.4
  end
  
  it "responds to cut-off (default value)" do
    plugin.cut_off.should == 1.0
  end

  it "responds to resonance" do
    plugin.resonance = 0.4
    plugin.resonance.should == 0.4
  end

  it "responds to resonance (default value)" do
    plugin.resonance.should == 0.1
  end
  
  it "remembers its editor" do
    plugin.editor.should == DummyEditor
  end
  
  it "supports VSTEvent constants" do
    VSTEvent::VST_EVENT_MIDI_TYPE.should == 1
  end
  
  it "supports VSTPinProperties constants" do
    VSTPinProperties::VST_PIN_IS_ACTIVE.should == 1
  end
  
  it "has a default, silent setProgramName to avoid errors on set reloading in Live" do
    plugin.should respond_to(:setProgramName)
  end
  
  # todo - add a testcase for no editor defined
end