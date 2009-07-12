module MidiTools
  def notes(ev)
    for i in (0..ev.getNumEvents()-1)
      next if (ev.getEvents()[i].getType() != VSTEvent::VST_EVENT_MIDI_TYPE)

      event = ev.getEvents()[i]
      midiData = event.getData()
      status = midiData[0] & 0xf0 # ignore channel

      if (status == 0x90 || status == 0x80)
        note = midiData[1] & 0x7f # we only look at notes
        velocity = (status == 0x80) ? 0 : midiData[2] & 0x7f
        yield :note_on, note, velocity, event.getDeltaFrames()
      elsif (status == 0xb0) && (midiData[1] == 0x7e || midiData[1] == 0x7b)
        yield :all_notes_off
      end
    end
  end
end