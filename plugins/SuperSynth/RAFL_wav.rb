#!/usr/bin/env ruby -wKU

#
# RAFL_wav.rb by Aaron Cohen
#
# RAFL - Ruby Audio File Library
# A means of reading, analyzing, and generating audio files from within Ruby
#
# This library is licensed under the LGPL. See COPYING.LESSER.
#
#
# TODO:
# Fix Sine Wave Generator - experiencing significant rounding error
# BEXT writing
# Fix Peak & RMS calculation on 24 bit audio

require 'rexml/document'
include REXML

#noinspection RubyLiteralArrayInspection
class RiffFile

  VALID_RIFF_TYPES = ['WAVE']
  HEADER_PACK_FORMAT = "A4V"
  AUDIO_PACK_FORMAT_16 = "s*"

  #noinspection RubyResolve
  attr_accessor :format, :found_chunks, :bext_meta, :ixml_meta, :raw_audio_data

  def initialize(file, mode)
    @file = File.open(file, mode)
    @file.binmode
    if mode == 'r'
      read_chunks if riff? && VALID_RIFF_TYPES.include?(riff_type)
    end
    if block_given?
      yield self
      close
    end
  end

  def close
    @file.close
  end

  def riff?
    @file.seek(0)
    riff, @file_end = read_chunk_header
    true if riff == 'RIFF'
  end

  def riff_type
    if riff?
      @file.seek(8)
      @file.read(4)
    end
  end

  def read_chunks
    @found_chunks = []
    while @file.tell < @file_end
      @file.seek(@file.tell + @file.tell % 2) #adds padding if last chunk was an odd length
      chunk_name, chunk_length = read_chunk_header
      chunk_position = @file.tell
      identify_chunk(chunk_name, chunk_position, chunk_length)
      @file.seek(chunk_position + chunk_length)
    end
  end

  def read_chunk_header
    @file.read(8).unpack(HEADER_PACK_FORMAT)
  end

  def identify_chunk(chunk_name, chunk_position, chunk_length)
    @found_chunks << chunk_name
    case chunk_name
      when 'fmt' then
        process_fmt_chunk(chunk_position, chunk_length)
      when 'bext' then
        process_bext_chunk(chunk_position, chunk_length)
      when 'data' then
        process_data_chunk(chunk_position, chunk_length)
      when 'iXML' then
        process_ixml_chunk(chunk_position, chunk_length)
      else
        # type code here
    end
  end

  def process_fmt_chunk(chunk_position, chunk_length)
    @file.seek(chunk_position)
    @format = WaveFmtChunk.new(@file.read(chunk_length))
  end

  def process_bext_chunk(chunk_position, chunk_length)
    @file.seek(chunk_position)
    @bext_meta = BextChunk.new(@file.read(chunk_length))
  end

  def process_data_chunk(chunk_position, chunk_length)
    @data_begin, @data_end = chunk_position, chunk_position + chunk_length
    #@file.seek(chunk_position)
    #@raw_audio_data = []
    #for sample in (0..total_samples)
    #  @raw_audio_data << read_sample(sample)
    #end
  end

  def process_ixml_chunk(chunk_position, chunk_length)
    @file.seek(chunk_position)
    @ixml_meta = IxmlChunk.new(@file.read(chunk_length))
  end

  def unpack_samples(samples, bit_depth)
    if bit_depth == 24
      #return samples.scan(/.../).map {|s| (s.reverse + 0.chr ).unpack("V")}.flatten
      samples.scan(/.../).map { |s| (s + 0.chr).unpack("V") }.flatten
    else
      samples.unpack(AUDIO_PACK_FORMAT_16)
    end
  end

  def pack_samples(samples, bit_depth)
    if bit_depth == 24
      samples.map { |s| [s].pack("VX") }.join
    else
      samples.pack(AUDIO_PACK_FORMAT_16)
    end
  end

  def read_sample(sample_number, channel) #returns an individual sample value

    @file.seek(@data_begin + (sample_number * @format.block_align) + (channel * (@format.block_align / @format.num_channels)))

    @file.read(@format.block_align / @format.num_channels)

    #return sample_array
  end

  def simple_read #returns all sample values for entire file
    @file.seek(@data_begin)
    #@file.read(@data_end - @data_begin).unpack(@audio_pack_format)#.join.to_i
    unpack_samples(@file.read(@data_end - @data_begin), @format.bit_depth)
  end

  def read_samples_by_channel(channel) #returns all of the sample values for a single audio channel
    samples = []
    (0..total_samples).each { |sample|
      samples << read_sample(sample, channel)
    }
    #samples.collect! { |samp| sample.unpack(@audio_pack_format).join.to_i } #bug, should be samp.unpack?
    samples.collect! { |samp| unpack_samples(samp, @format.bit_depth).join.to_i }
    samples
  end

  def write(channels, sample_rate, bit_depth, audio_data) #writes to audio file
    write_riff_type
    write_fmt_chunk(channels, sample_rate, bit_depth)
    write_data_chunk audio_data #channels==1 ? [[*audio_data]] : audio_data
    write_riff_header
  end

  def write_riff_header
    @file.seek(0)
    @file.print(["RIFF", @file_end].pack(HEADER_PACK_FORMAT))
  end

  def write_riff_type
    @file.seek(8)
    @file.print(["WAVE"].pack("A4"))
  end

  def write_fmt_chunk(num_channels, sample_rate, bit_depth)
    @file.seek(12)
    @write_format = WaveFmtChunk.new
    @write_format.audio_format = 1
    @write_format.num_channels = num_channels
    @write_format.bit_depth = bit_depth
    @write_format.set_sample_rate(sample_rate)

    @file.print(["fmt ", 16].pack(HEADER_PACK_FORMAT)) # the 16 here means PCM, not bit depth
    @file.print(@write_format.pack_header_data)
  end

  def write_data_chunk(audio_data)
    data_chunk_begin = @file.tell
    @file.seek(8, IO::SEEK_CUR)
    @data_begin = @file.tell

    #interleave arrays
    if @write_format.num_channels > 1 && audio_data.length > 1
      interleaved_audio_data = audio_data[0].zip(*audio_data[1..-1]).flatten
    else
      interleaved_audio_data = audio_data[0]
    end

    puts interleaved_audio_data.length
    interleaved_audio_data.each_index do |sample|
      if interleaved_audio_data[sample].nil?
        puts "Sample number #{sample.to_s} is nil"
      end
    end

    @file.print(pack_samples(interleaved_audio_data, @write_format.bit_depth))
    @data_end = @file.tell
    @file_end = @file.tell
    @file.seek(data_chunk_begin)
    @file.print(["data", @data_end - @data_begin].pack(HEADER_PACK_FORMAT))
  end

  def duration
    (@data_end - @data_begin) / @format.byte_rate
  end

  def total_samples
    (@data_end - @data_begin) / @format.block_align
  end
end

class WaveFmtChunk

  attr_accessor :audio_format, :num_channels,
                :sample_rate, :byte_rate,
                :block_align, :bit_depth

  PACK_FMT = "vvVVvv"

  def initialize(*binary_data)
    unpack_header_data(binary_data[0]) if !binary_data.empty?
  end

  def unpack_header_data(binary_data)
    @audio_format, @num_channels,
        @sample_rate, @byte_rate,
        @block_align, @bit_depth = binary_data.unpack(PACK_FMT)
  end

  def pack_header_data
    [@audio_format, @num_channels,
     @sample_rate, @byte_rate,
     @block_align, @bit_depth].pack(PACK_FMT)
  end

  def set_sample_rate(rate)
    @byte_rate = calc_byte_rate(rate)
    @sample_rate = rate
    @block_align = calc_block_align
  end

  def calc_block_align
    @num_channels * (@bit_depth)
  end

  def calc_byte_rate(sample_rate, num_channels = @num_channels, bit_depth = @bit_depth)
    sample_rate * num_channels * (bit_depth / 8)
  end

end

#noinspection ALL
class BextChunk

  attr_accessor :description, :originator, :originator_reference, :origination_date, :origination_time,
                :time_reference, :version, :umid, :reserved, :coding_history

  PACK_FMT = "A256A32A32A10A8QvB64A190M*"

  def initialize(*binary_data)
    unpack_bext_data(binary_data[0]) if !binary_data.empty?
  end

  def unpack_bext_data(binary_data)
    @description, @originator, @originator_reference, @origination_date, @origination_time,
        @time_reference, @version, @umid, @reserved, @coding_history = binary_data.unpack(PACK_FMT)
  end

  def calc_time_offset(sample_rate)
    time = @time_reference / sample_rate
    [time/3600, time/60 % 60, time % 60].map { |t| t.to_s.rjust(2, '0') }.join(':')
  end
end

#noinspection ALL,RubyUnusedLocalVariable
class IxmlChunk
  #see http://www.gallery.co.uk/ixml/object_Details.html

  attr_accessor :raw_xml #, :ixml_version,
                         #                :project, :scene, :take, :tape, :circled, :no_good, :false_start,
                         #                :wild_track, :file_uid, :ubits, :note,
                         #
                         #                :sync_point_count, :sync_points,
                         #
                         #                :speed_note, :speed_master, :speed_current, :speed_timecode_rate, :speed_timecode_flag,
                         #                :speed_file_sample_rate, :speed_bit_depth, :speed_digitizer_sample_rate,
                         #                :speed_timestamp_samples_since_midnight, :speed_timestamp_sample_rate,
                         #
                         #                :history_original_filename, :history_parent_filename, :history_parent_uid,
                         #
                         #                :fileset_total_files, :fileset_family_uid, :fileset_family_name, :fileset_index,
                         #
                         #                :tracklist_count, :tracklist_tracks,
                         #
                         #                :dep_pre_record_samplecount,
                         #
                         #                :bwf_description, :bwf_originator, :bwf_originator_reference, :bwf_origination_date,
                         #                :bwf_origination_time, :bwf_time_reference, :bwf_version, :bwf_umid, :bwf_reserved, :bwf_coding_history,
                         #
                         #                :user

  PACK_FMT = "a*"

  def initialize(*binary_data)
    unpack_ixml_data(binary_data[0]) if !binary_data.empty?
  end

  def unpack_ixml_data(binary_data)
    @raw_xml = Document.new(binary_data.unpack(PACK_FMT)[0])
    #read_xml_values
  end

  #noinspection RubyInstanceVariableNamingConvention
  def read_xml_values #not in use, looking for a better way
    bwfxml = @raw_xml.elements["BWFXML"]

    @ixml_version = bwfxml.elements["IXML_VERSION"].text
    @project = bwfxml.elements["PROJECT"].text
    @scene = bwfxml.elements["SCENE"].text
    @take = bwfxml.elements["TAKE"].text
    @tape = bwfxml.elements["TAPE"].text
    @circled = bwfxml.elements["CIRCLED"].text
    @file_uid = bwfxml.elements["FILE_UID"].text
    @ubits = bwfxml.elements["UBITS"].text
    @note = bwfxml.elements["NOTE"].text

    @speed_note = bwfxml.elements["SPEED"].elements["NOTE"].text
    @speed_master = bwfxml.elements["SPEED"].elements["MASTER_SPEED"].text
    @speed_current = bwfxml.elements["SPEED"].elements["CURRENT_SPEED"].text
    @speed_timecode_rate = bwfxml.elements["SPEED"].elements["TIMECODE_RATE"].text
    @speed_timecode_flag = bwfxml.elements["SPEED"].elements["TIMECODE_FLAG"].text

    @history_original_filename = bwfxml.elements["HISTORY"].elements["ORIGINAL_FILENAME"].text
    @history_parent_filename = bwfxml.elements["HISTORY"].elements["PARENT_FILENAME"].text
    @history_parent_uid = bwfxml.elements["HISTORY"].elements["PARENT_UID"].text

    @fileset_total_files = bwfxml.elements["FILE_SET"].elements["TOTAL_FILES"].text
    @fileset_family_uid = bwfxml.elements["FILE_SET"].elements["FAMILY_UID"].text
    @fileset_family_name = bwfxml.elements["FILE_SET"].elements["FAMILY_NAME"].text
    @fileset_index = bwfxml.elements["FILE_SET"].elements["FILE_SET_INDEX"].text

    @tracklist_count, @tracklist_tracks = read_tracklist

    @bwf_description = bwfxml.elements["BEXT"].elements["BWF_DESCRIPTION"].text
    @bwf_originator = bwfxml.elements["BEXT"].elements["BWF_ORIGINATOR"].text
    @bwf_originator_reference = bwfxml.elements["BEXT"].elements["BWF_ORIGINATOR_REFERENCE"].text
    @bwf_origination_date = bwfxml.elements["BEXT"].elements["BWF_ORIGINATION_DATE"].text
    @bwf_origination_time = bwfxml.elements["BEXT"].elements["BWF_ORIGINATION_TIME"].text
    @bwf_time_reference = bwfxml.elements["BEXT"].elements["BWF_TIME_REFERENCE_HIGH"].text + bwfxml.elements["BEXT"].elements["BWF_TIME_REFERENCE_LOW"].text
    @bwf_version = bwfxml.elements["BEXT"].elements["BWF_VERSION"].text
    @bwf_umid = bwfxml.elements["BEXT"].elements["BWF_UMID"].text
    @bwf_reserved = bwfxml.elements["BEXT"].elements["BWF_RESERVED"].text
    @bwf_coding_history = bwfxml.elements["BEXT"].elements["BWF_CODING_HISTORY"].text

    @user = bwfxml.elements["USER"].text

    #iXML 1.52
    if @ixml_version >= "1.52"
      @no_good = bwfxml.elements["NO_GOOD"].text
      @false_start = bwfxml.elements["FALSE_START"].text
      @wild_track = bwfxml.elements["WILD_TRACK"].text

      #iXML 1.5
    elsif @ixml_version >= "1.5"
      #iXML Readers should ignore this information and instead take the data from the official fmt and bext chunks in the file
      #Generic utilities changing file data might change the fmt or bxt chunk but not update the iXML SPEED tag, and for EBU officially specified BWF data like timestamp, the EBU data takes precedence (unlike the unofficial informal bext metadata which is superceded by iXML)
      @speed_file_sample_rate = bwfxml.elements["SPEED"].elements["FILE_SAMPLE_RATE"].text
      @speed_bit_depth = bwfxml.elements["SPEED"].elements["AUDIO_BIT_DEPTH"].text
      @speed_digitizer_sample_rate = bwfxml.elements["SPEED"].elements["DIGITIZER_SAMPLE_RATE"].text
      @speed_timestamp_samples_since_midnight = bwfxml.elements["SPEED"].elements["TIMESTAMP_SAMPLES_SINCE_MIDNIGHT_HI"].text + bwfxml.elements["SPEED"].elements["TIMESTAMP_SAMPLES_SINCE_MIDNIGHT_HI"].text
      @speed_timestamp_sample_rate = bwfxml.elements["SPEED"].elements["TIMESTAMP_SAMPLE_RATE"].text

      #iXML 1.4
    elsif @ixml_version >= "1.4"
      @sync_point_count, @sync_points = read_sync_points

      #iXML 1.3
    elsif @ixml_version == "1.3"
      @dep_pre_record_samplecount = read_pre_record_samplecount
    end
  end

  def read_sync_points
    #TODO
  end

  def read_tracklist
    #TODO
  end

  def read_pre_record_samplecount
    #TODO
  end

  def translate_frame_rates(frame_rate)
    #TODO
  end
end


#####################
# Standalone methods
#####################

def calc_rms(audio_samples, format)
  #squaresum = 0
  #audio_samples.each { |value| squaresum += (value ** 2) }
  #sample_rms = Math.sqrt(squaresum / audio_samples.length)
  sample_rms = Math.sqrt((audio_samples.inject { |sum, item| sum + (item ** 2) }) / audio_samples.length)

  calc_dbfs(sample_rms, format.bit_depth)
end

def calc_peak(audio_samples, format)
  calc_dbfs(audio_samples.max.to_f, format.bit_depth)
end

def calc_dbfs(sample_value, bit_depth)
  range = (2 ** bit_depth) / 2
  (20*Math.log10(sample_value.to_f / range)).round_to(2)
end

def calc_sample_value(dbfs_value, bit_depth)
  range = (2 ** bit_depth / 2)
  (range * Math::E ** (1/20.0 * dbfs_value * (Math.log(2) + Math.log(5)))) - 1
end

def generate_white_noise(length_secs, peak_db, sample_rate, bit_depth)
  num_samples = (length_secs * sample_rate).to_i
  peak_samples = calc_sample_value(peak_db, bit_depth)
  output = []
  num_samples.times do
    output << (rand(65536) - 32768) * peak_samples
  end
  output
end

#noinspection RubyUnusedLocalVariable
def generate_pink_noise(length_secs, peak_db, sample_rate, bit_depth)
  num_samples = (length_secs * sample_rate).to_i
  peak_samples = calc_sample_value(peak_db, bit_depth)
  output = []
  amplitude_scaling = [3.8024, 2.9694, 2.5970, 3.0870, 3.4006]
  update_probability = [0.00198, 0.01280, 0.04900, 0.17000, 0.68200]
  probability_sum = [0.00198, 0.01478, 0.06378, 0.23378, 0.91578]

  contributed_values = [0, 0, 0, 0, 0]

  num_samples.times do

    ur1 = rand
    5.times do |stage|
      if ur1 <= probability_sum[stage]
        ur2 = rand
        contributed_values[stage] = 2 * (ur2 - 0.5) * amplitude_scaling[stage]
        break
      end
    end

    sample = contributed_values.inject(0) { |sum, item| sum + item }

    output << sample
  end

  scale_amount = peak_samples / output.max
  output.map! { |item| (item * scale_amount).round_to(0).to_i }

  output
end

def generate_sine_wave(length_secs, peak_db, freq, sample_rate, bit_depth) #defective...drifts over time - rounding error?
  peak_samples = calc_sample_value(peak_db, bit_depth)
  output = []
  period = 1.0 / freq
  angular_freq = (2 * Math::PI) / period

  time = 0
  while time <= length_secs do
    output << (Math.sin(angular_freq * time) * peak_samples).round_to(0).to_i
    time += (1.0 / sample_rate)
  end
  output.slice(0..-2) #kludge...need to pick this apart to find extra sample
end


class Float
  def round_to(x)
    (self * 10**x).round.to_f / 10**x
  end

  def ceil_to(x)
    (self * 10**x).ceil.to_f / 10**x
  end

  def floor_to(x)
    (self * 10**x).floor.to_f / 10**x
  end
end
