require 'java'

# this is to be able to call the static log() method in VSTPluginAdapter
include_class 'jvst.wrapper.VSTPluginAdapter'

class Delay < Java::jvst.wrapper.VSTPluginAdapter
  P_DELAY_TIME = 0
  P_FEEDBACK = 1
  P_LFO_FREQUENCY = 2
  P_LFO_DEPTH = 3
  P_WET_DRY_MIX = 4

  NUM_PARAMS = P_WET_DRY_MIX + 1
  PARAM_NAMES = ["Delay Time","Feedback","LFO Frq","LFO Depth","Wet/Dry Mix"]
  PARAM_LABELS = ["ms","%","Hz","","%"]
  PARAM_PRINT_MUL = [1000,100,1,1,100]

  PROGRAMS = [[0.45, 0.50, 0.0, 0, 0.5],
  [0.01, 0.85, 0.2, 0.5, 0.65],
  [0.99, 0.7, 0.0, 0.02, 0.50],
  [0.3, 0.9, 0.0, 0.0, 0.50],
  [0.004, 0.80, 0.1, 0.8, 0.50],
  [0.4, 0.50, 0.1, 0.5, 0.50],
  [0.1, 0.50, 0.1, 0.6, 0.50],
  [0.1, 0.50, 0.1, 0.7, 0.50]]

  def initialize(wrapper)
	super

	@echo = []
	@echoSize = 0
	@echoPos = 0
	@echoLFODiff = 0
	@echoLFODiffMax = 0
	@echoLFODepth = 0.8
	@echoFeedback = 0
	@echoLFOSpeed = 0
	@echoLFOPos = 0
	@echoDW = 0.8
	@sampleRate = 44100
	@currentProgramIndex = 0

	update

	setNumInputs(1)
	setNumOutputs(1)
	canProcessReplacing(true)
	setUniqueID(9876543)
  end

  #convenience
  def log(msg)
	VSTPluginAdapter.log("JRuby: #{msg}")
  end

  def setSampleRate(sampleRate)
	@sampleRate = sampleRate
  end

  def setEchoTime(millisDelay)
	@echoSize = millisDelay * (@sampleRate / 1000)
	@echo = Array.new(@echoSize) unless @echo.size == @echoSize
  end

  def currentProgram
	PROGRAMS[@currentProgramIndex]
  end

  def update
	setEchoTime(currentProgram[P_DELAY_TIME] * 1000)
	@echoFeedback = currentProgram[P_FEEDBACK]
	@echoLFOSpeed = currentProgram[P_LFO_FREQUENCY] * 2 * 3.1415 / @sampleRate
	@echoLFODepth = currentProgram[P_LFO_DEPTH]
	@echoLFODiffMax = (@echoSize / 2.0) * @echoLFODepth
	@echoLFODiff = 0
	@echoDW = currentProgram[P_WET_DRY_MIX]
	@echoPos = 0
  end

  def canDo(feature)
	ret = CANDO_NO
	ret = CANDO_YES if feature == CANDO_PLUG_1_IN_1_OUT
	ret = CANDO_YES if feature == CANDO_PLUG_PLUG_AS_CHANNEL_INSERT
	ret = CANDO_YES if feature == CANDO_PLUG_PLUG_AS_SEND

	log("canDo: #{feature} = #{ret}")
	ret
  end

  def getProductString
	"Opaz"
  end

  def getEffectname
	"Delay"
  end

  def getProgramNameIndexed(category,index)
	"Prog: cat #{category}, #{index}"
  end

  def getVendorString
	"LoGeek"
  end

  def setBypass(value)
	false
  end

  def string2Parameter(index,value)
	begin
	  setParameter(index, Float(value)) unless value.nil?
	  return true
	rescue
	  return false
	end
  end

  def getNumParams
	NUM_PARAMS
  end

  def getNumPrograms
	PROGRAMS.size
  end

  def getParameter(index)
	if index < currentProgram.size
	  return currentProgram[index]
	else
	  return 0.0
	end
  end

  def getParameterDisplay(index)
	if index < currentProgram.size
	  return ((100 * PARAM_PRINT_MUL[index] * currentProgram[index]) / 100.0).to_s
	else
	  return "0.0"
	end
  end

  def getParameterLabel(index)
	if index < currentProgram.size
	  return PARAM_LABELS[index]
	else
	  return ""
	end
  end

  def getParameterName(index)
	if index < currentProgram.size
	  return PARAM_NAMES[index]
	else
	  return "param: #{index}"
	end
  end

  def getProgram
	@currentProgramIndex
  end

  def getProgramName
	"program #{@currentProgramIndex}"
  end

  def setParameter(index,value)
	currentProgram[index] = value
	update
  end

  def setProgram(index)
	@currentProgramIndex = index
	update
  end

  def setProgramName(name)
	#ignore
  end

  def getPlugCategory
	log("getPlugCategory")
	PLUG_CATEG_EFFECT
  end

  def processReplacing(inputs, outputs, sampleFrames)
	inBuffer = inputs[0]
	outBuffer = outputs[0]
	for i in (0..sampleFrames-1)
	  exVal = inBuffer[i]
	  echoRead = @echoPos + @echoLFODiff
	  if (echoRead >= @echoSize)
		echoRead -= @echoSize
	  end

	  #log("#{exVal} * (1.0 - #{@echoDW}) + #{@echo[echoRead]} * #{@echoDW}")
	  #log("echo[#{echoRead}] = #{@echo[echoRead]}")
	  
	  #error here: @echo[echoRead] = nil
	  #out = exVal * (1.0 - @echoDW) + @echo[echoRead] * @echoDW
	  out = inBuffer[i] #just to hear something
	  outBuffer[i] = out*@echoDW

	  #error here: @echo[echoRead] = nil
	  #exVal = exVal + @echo[echoRead] * @echoFeedback
	
	  @echo[@echoPos] = exVal
	  @echoPos += 1
	  if (@echoPos >= @echoSize)
		@echoPos = 0
	  end
	end

	@echoLFODiff = @echoLFODiff * (1.0 + Math.sin(@echoLFOPos))
	@echoLFOPos += @echoLFOSpeed * sampleFrames
  end

end
 