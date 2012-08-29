#noinspection RubyResolve
require 'java'

$LOAD_PATH << 'src'
$LOAD_PATH << 'libs'
$LOAD_PATH << File.dirname(__FILE__)
#noinspection RubyGlobalVariableNamingConvention
$DISABLE_VSTPLUGINADAPTER_INHERIT = true # avoid inheritance in opaz_plug.rb

#noinspection RubyResolve
require 'java'
#noinspection RubyResolve
require 'jVSTwRapper-1.0beta.jar'
#noinspection RubyResolve
require 'jVSTsYstem-1.0beta.jar'
#noinspection RubyResolve
require 'opaz_plug'
require 'dummy_plug'

#noinspection RubyResolve
describe OpazPlug do
  #noinspection RubyResolve
  let(:plugin) { DummyPlug.new(0) }

  context "plugin information methods" do
    #noinspection RubyResolve
    describe "#getEffectName" do
      #noinspection RubyResolve
      subject { plugin.getEffectName }
      #noinspection RubyResolve
      it { should == "DummyPlug" }
    end

    #noinspection RubyResolve
    describe "#getProductString" do
      #noinspection RubyResolve
      subject { plugin.getProductString }
      #noinspection RubyResolve
      it { should == "Opaz" }
    end

    #noinspection RubyResolve
    describe "#getVendorString" do
      #noinspection RubyResolve
      subject { plugin.getVendorString }
      #noinspection RubyResolve
      it { should == "LoGeek" }
    end
  end

  #noinspection RubyResolve
  describe "#unique_id" do
    #noinspection RubyResolve
    subject { plugin.unique_id }
    #noinspection RubyResolve
    it { should == 1869635962 } # hand-coded value, computed using java
  end

  #noinspection RubyResolve
  describe "#setSampleRate" do
    #noinspection RubyResolve
    before { plugin.setSampleRate(44_800) }
    #noinspection RubyResolve
    it "sets the sample rate" do
      plugin.sample_rate.should == 44_800
    end
  end

  #noinspection RubyResolve
  it "responds to setBypass" do
    plugin.setBypass(true).should == false
  end

  #noinspection RubyResolve
  describe "#canDo" do
    context "plugAsSend" do
      #noinspection RubyResolve
      subject { plugin.canDo("plugAsSend") }
      #noinspection RubyResolve
      it { should == 1 }
    end
    context "plugAsChannelInsert" do
      #noinspection RubyResolve
      subject { plugin.canDo("plugAsChannelInsert") }
      #noinspection RubyResolve
      it { should == 1 }
    end
    context "1in1out" do
      #noinspection RubyResolve
      subject { plugin.canDo("1in1out") }
      #noinspection RubyResolve
      it { should == 1 }
    end
    context "8in8out" do
      #noinspection RubyResolve
      subject { plugin.canDo("8in8out") }
      #noinspection RubyResolve
      it { should == -1 }
    end
  end

  #noinspection RubyResolve
  describe "#getNumParams" do
    #noinspection RubyResolve
    it "returns the number of parameters" do
      plugin.getNumParams.should == 3
    end
  end

  #noinspection RubyResolve
  describe "#getNumPrograms" do
    #noinspection RubyResolve
    it "returns the number of programs" do
      plugin.getNumPrograms.should == 0
    end
  end

  #noinspection RubyResolve
  describe "#getParameterName" do
    #noinspection RubyResolve
    it "returns the name of the parameter specified" do
      plugin.getParameterName(0).should == 'Cut Off'
    end
  end

  #noinspection RubyResolve
  describe "#getParameterLabel" do
    context "when no unit is specified" do
      #noinspection RubyResolve
      it "returns an empty string by default" do
        plugin.getParameterLabel(0).should == ""
      end
    end
    context "when a unit is specified" do
      #noinspection RubyResolve
      it "returns the specified unit" do
        plugin.getParameterLabel(1).should == "resograms"
      end
    end
  end

  #noinspection RubyResolve
  describe "#getParameterDisplay" do
    #noinspection RubyResolve
    it "returns the display value of the param" do
      plugin.getParameterDisplay(0).should == "1.00"
    end
  end

  #noinspection RubyResolve
  describe "#getParameterRange" do
    context "without a specified range" do
      #noinspection RubyResolve
      it "returns 0.0..1.0" do
        plugin.getParameterRange(1).should == (0.0..1.0)
      end
    end
    context "with a specified range" do
      #noinspection RubyResolve
      it "returns the range specified" do
        plugin.getParameterRange(2).should == (-60..6)
      end
    end
  end

  #noinspection RubyResolve
  describe "#setParameter" do
    #noinspection RubyResolve
    before { plugin.setParameter(2, 0.0) }
    #noinspection RubyResolve
    it "sets the given param to the given value" do
      plugin.getParameter(2).should == 0.0
    end
    #noinspection RubyResolve
    it "adjust the ratio accordingly" do
      plugin.ratio.should == -60
    end
  end

  #noinspection RubyResolve
  describe "#getParameter" do
    #noinspection RubyResolve
    before { plugin.setParameter(2, 0.0) }
    #noinspection RubyResolve
    it "returns the param value specified" do
      plugin.getParameter(2).should == 0.0
    end
  end

  #noinspection RubyResolve
  describe "when using ranges on parameters" do

    #noinspection RubyResolve
    it "translates the vst 0..1 range to plugin range on setParameter" do

      plugin.setParameter(2, 1.0)
      #noinspection RubyResolve
      plugin.getParameter(2).should == 1.0
      #noinspection RubyResolve
      plugin.ratio.should == +6
    end

    #noinspection RubyResolve
    it "translates the plugin range to vst 0..1 range on attr_writer" do
      #noinspection RubyResolve
      plugin.ratio = -60
      #noinspection RubyResolve
      plugin.getParameter(2).should == 0.0

      #noinspection RubyResolve
      plugin.ratio = +6
      #noinspection RubyResolve
      plugin.getParameter(2).should == 1.0
    end

    #noinspection RubyResolve
    it "translates the plugin range to vst 0..1 range on getParameterDisplay" do
      #noinspection RubyResolve
      plugin.getParameterDisplay(2).should == "6.00"
    end

  end

  #noinspection RubyResolve
  describe "#param" do
    #noinspection RubyResolve
    it "sets the initial value" do
      #noinspection RubyResolve
      plugin.getParameter(0).should == 1.0
    end

    #noinspection RubyResolve
    it "creates an accessor for the param" do
      #noinspection RubyResolve
      plugin.resonance.should == plugin.getParameter(1)
    end

    #noinspection RubyResolve
    it "creates a setter for the param" do
      plugin.resonance = 0.4
      #noinspection RubyResolve
      plugin.resonance.should == 0.4
    end
  end

  #noinspection RubyResolve
  describe "#string2Parameter" do
    #noinspection RubyResolve
    it "returns true when setting a value" do
      #noinspection RubyResolve
      plugin.string2Parameter(1, 0.5).should == true
    end

    #noinspection RubyResolve
    it "sets the params value" do
      #noinspection RubyResolve
      plugin.string2Parameter(1, 0.5).should == true
      #noinspection RubyResolve
      plugin.resonance.should == 0.5
    end
  end

  #noinspection RubyResolve
  describe "#editor" do
    context "with a defined editor" do
      #noinspection RubyResolve
      it "returns the editor object" do
        #noinspection RubyResolve
        plugin.editor.should == DummyEditor
      end
    end
    context "without a defined editor" do
      #noinspection RubyResolve
      let(:plugin) { DummyPlugWithoutEditor.new(0) }
      #noinspection RubyResolve
      it "returns nil" do
        #noinspection RubyResolve
        plugin.editor.should be_nil
      end
    end
  end

  #noinspection RubyResolve
  it "supports VSTEvent constants" do
    #noinspection RubyResolve
    VSTEvent::VST_EVENT_MIDI_TYPE.should == 1
  end

  #noinspection RubyResolve
  it "supports VSTPinProperties constants" do
    #noinspection RubyResolve
    VSTPinProperties::VST_PIN_IS_ACTIVE.should == 1
  end

  #noinspection RubyResolve
  it "has a default, silent setProgramName to avoid errors on set reloading in Live" do
    #noinspection RubyResolve
    plugin.should respond_to(:setProgramName)
  end

end
