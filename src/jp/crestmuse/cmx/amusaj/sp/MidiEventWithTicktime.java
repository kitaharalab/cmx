package jp.crestmuse.cmx.amusaj.sp;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.InvalidMidiDataException;

public class MidiEventWithTicktime extends MidiEvent {
  public long music_position;
  public MidiEventWithTicktime(MidiMessage message, long tick, long position) {
    super(message, tick);
    music_position = position;
  }
    //  public String encode() {
    //      return throw UnsupportedOperationException();
    //  }
    //  public boolean hasNext() {
    //    return true;
    //  }

  public static MidiEventWithTicktime createNoteOnEvent(long position, int ch,
                                                        int nn, int vel) 
    throws InvalidMidiDataException {
    ShortMessage msg = new ShortMessage();
    msg.setMessage(ShortMessage.NOTE_ON + ch, nn, vel);
    MidiEventWithTicktime evt 
      = new MidiEventWithTicktime(msg, 0, position);
    return evt;
  }

  public static MidiEventWithTicktime createNoteOffEvent(long position, int ch,
                                                        int nn, int vel) 
    throws InvalidMidiDataException {
    ShortMessage msg = new ShortMessage();
    msg.setMessage(ShortMessage.NOTE_OFF + ch, nn, vel);
    MidiEventWithTicktime evt 
      = new MidiEventWithTicktime(msg, 0, position);
    return evt;
  }




}
