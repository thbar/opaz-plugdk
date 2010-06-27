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
  let(:plugin) { DummyPlug.new(0) }

  context "plugin information methods" do
    describe "#getEffectName" do
      subject { plugin.getEffectName }
      it { should == "DummyPlug" }
    end

    describe "#getProductString" do
      subject { plugin.getProductString }
      it { should == "Opaz" }
    end

    describe "#getVendorString" do
      subject { plugin.getVendorString }
      it { should == "LoGeek" }
    end
  end

  describe "#unique_id" do
    subject { plugin.unique_id }
    it { should == 1869635962 } # hand-coded value, computed using java
  end

  describe "#setSampleRate" do
    before { plugin.setSampleRate(44_800) }
    it "sets the sample rate" do
      plugin.sample_rate.should == 44_800
    end
  end

  it "responds to setBypass" do
    plugin.setBypass(true).should == false
  end

  describe "#canDo" do
    context "plugAsSend" do
      subject { plugin.canDo("plugAsSend") }
      it { should == 1 }
    end
    context "plugAsChannelInsert" do
      subject { plugin.canDo("plugAsChannelInsert") }
      it { should == 1 }
    end
    context "1in1out" do
      subject { plugin.canDo("1in1out") }
      it { should == 1 }
    end
    context "8in8out" do
      subject { plugin.canDo("8in8out") }
      it { should == -1 }
    end
  end

  describe "#getNumParams" do
    it "returns the number of parameters" do
      plugin.getNumParams.should == 3
    end
  end

  describe "#getNumPrograms" do
    it "returns the number of programs" do
      plugin.getNumPrograms.should == 0
    end
  end

  describe "#getParameterName" do
    it "returns the name of the parameter specified" do
      plugin.getParameterName(0).should == 'Cut Off'
    end
  end

  describe "#getParameterLabel" do
    context "when no unit is specified" do
      it "returns an empty string by default" do
        plugin.getParameterLabel(0).should == ""
      end
    end
    context "when a unit is specified" do
      it "returns the specified unit" do
        plugin.getParameterLabel(1).should == "resograms"
      end
    end
  end

  describe "#getParameterDisplay" do
    it "returns the display value of the param" do
      plugin.getParameterDisplay(0).should == "1.00"
    end
  end

  describe "#getParameterRange" do
    context "without a specified range" do
      it "returns 0.0..1.0" do
        plugin.getParameterRange(1).should == (0.0..1.0)
      end
    end
    context "with a specified range" do
      it "returns the range specified" do
        plugin.getParameterRange(2).should == (-60..6)
      end
    end
  end

  describe "#setParameter" do
    before { plugin.setParameter(2, 0.0) }
    it "sets the given param to the given value" do
      plugin.getParameter(2).should == 0.0
    end
    it "adjust the ratio accordingly" do
      plugin.ratio.should == -60
    end
  end

  describe "#getParameter" do
    before { plugin.setParameter(2, 0.0) }
    it "returns the param value specified" do
      plugin.getParameter(2).should == 0.0
    end
  end

  describe "when using ranges on parameters" do

    it "translates the vst 0..1 range to plugin range on setParameter" do

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

    it "translates the plugin range to vst 0..1 range on getParameterDisplay" do
      plugin.getParameterDisplay(2).should == "6.00"
    end

  end

  describe "#param" do
    it "sets the initial value" do
      plugin.getParameter(0).should == 1.0
    end

    it "creates an accessor for the param" do
      plugin.resonance.should == plugin.getParameter(1)
    end

    it "creates a setter for the param" do
      plugin.resonance = 0.4
      plugin.resonance.should == 0.4
    end
  end

  describe "#string2Parameter" do
    it "returns true when setting a value" do
      plugin.string2Parameter(1, 0.5).should == true
    end

    it "sets the params value" do
      plugin.string2Parameter(1, 0.5).should == true
      plugin.resonance.should == 0.5
    end
  end

  describe "#editor" do
    context "with a defined editor" do
      it "returns the editor object" do
        plugin.editor.should == DummyEditor
      end
    end
    context "without a defined editor" do
      let(:plugin) { DummyPlugWithoutEditor.new(0) }
      it "returns nil" do
        plugin.editor.should be_nil
      end
    end
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

end
