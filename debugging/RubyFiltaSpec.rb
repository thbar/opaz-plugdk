# first use of rspec to test out a plugin. Run with
# jruby -S spec -fs debugging/RubyFiltaSpec.rb

require 'java'
$LOAD_PATH << 'plugins/RubyFilta'
$LOAD_PATH << 'libs'

$DISABLE_VSTPLUGINADAPTER_INHERIT = true # avoid inheritance

require 'java'
require 'jVSTwRapper-0.9g.jar'
require 'jVSTsYstem-0.9g.jar'
require 'RubyFilta'

# for tests only - bypass constructor
class RubyFilta
  def initialize(wrapper); end
end

describe RubyFilta do
  attr_reader :plugin
  
  before(:each) do
    @plugin = RubyFilta.new(0)
  end
  
  it "stores and retrieves sample rate" do
    plugin.setSampleRate(44800)
    plugin.sample_rate.should == 44800
  end
  
  it "returns plugin info" do
    plugin.getEffectName.should == "OpazFilta"
    plugin.getProductString.should == "Opaz"
    plugin.getVendorString.should == "LoGeek"
  end
  
  it "responds to setBypass" do
    plugin.setBypass(true).should == false
  end
  
  it "understands setParameter and getParameter" do
    plugin.setParameter(0, 0.4)
    plugin.setParameter(1, 0.2)
    plugin.getParameter(0).should == 0.4
    plugin.getParameter(1).should == 0.2
    
    plugin.cut_off.should == plugin.getParameter(0)
    plugin.resonance.should == plugin.getParameter(1)
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
  
  it "responds to cut-off" do
    plugin.cut_off = 0.4
    plugin.cut_off.should == 0.4
  end
  
  it "responds to cut-off (default value)" do
    plugin.cut_off.should == 1.0
  end

  it "responds to getParameterLabel" do
    plugin.getParameterLabel(0).should == "Cut Off"
  end
  
  it "responds to getParameterName" do
    plugin.getParameterName(0).should == 'cut_off'
  end
  
  it "responds to getParameterDisplay" do
    plugin.getParameterDisplay(0).should == "1.00"
  end

  it "responds to resonance" do
    plugin.resonance = 0.4
    plugin.resonance.should == 0.4
  end

  it "responds to resonance (default value)" do
    plugin.resonance.should == 0.1
  end
  
  it "responds to mode" do
    plugin.mode = 0.2
    plugin.mode.should == 0.2
  end
  
  it "responds to mode (default value)" do
    plugin.mode.should == 0
  end
  
  it "implements string2Parameter" do
    plugin.string2Parameter(1, 0.5).should == true
    plugin.resonance.should == 0.5
  end

end