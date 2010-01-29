package jp.crestmuse.cmx.amusaj.sp;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.InvalidMidiDataException;
import java.util.*;

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

    public byte[] getMessageInByteArray() {
	return getMessage().getMessage();
    }

    public static MidiEventWithTicktime createShortMessageEvent
	(byte[] message, long tick, long position) 
	throws InvalidMidiDataException {
	ShortMessage msg = new ShortMessage();
	msg.setMessage(message[0], message[1], message[2]);
	return new MidiEventWithTicktime(msg, tick, position);
    }

    public static MidiEventWithTicktime createShortMessageEvent
	(List<? extends Number> message, long tick, long position) 
	throws InvalidMidiDataException {
	int length = message.size();
	byte[] bb = new byte[length];
	for (int i = 0; i < length; i++)
	    bb[i] = message.get(i).byteValue();
	return createShortMessageEvent(bb, tick, position);
    }


  public static MidiEventWithTicktime createNoteOnEvent(long position, int ch,
                                                        int nn, int vel) 
    throws InvalidMidiDataException {
      ShortMessage msg = new ShortMessage();
      msg.setMessage(ShortMessage.NOTE_ON + ch, nn, vel);
    return new MidiEventWithTicktime(msg, 0, position);
  }

  public static MidiEventWithTicktime createNoteOffEvent(long position, int ch,
                                                        int nn, int vel) 
    throws InvalidMidiDataException {
      ShortMessage msg = new ShortMessage();
      msg.setMessage(ShortMessage.NOTE_OFF + ch, nn, vel);
      return new MidiEventWithTicktime(msg, 0, position);
  }
}
