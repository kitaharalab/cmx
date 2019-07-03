package jp.crestmuse.cmx.amusaj.sp;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.InvalidMidiDataException;
import java.util.*;
import static jp.crestmuse.cmx.misc.MIDIConst.*;

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

  public int status() {
    return getMessage().getStatus() & 0xF0;
  }

  public int channel() {
    return getMessage().getStatus() & 0x0F;
  }

  public boolean hasData1() {
    return getMessage().getMessage().length >= 2;
  }

  public int data1() {
    return getMessage().getMessage()[1];
  }

  public boolean hasData2() {
    return getMessage().getMessage().length >= 3;
  }

  public int data2() {
    return getMessage().getMessage()[2];
  }

  public boolean equals(MidiEventWithTicktime another) {
    if (music_position != another.music_position)
      return false;
    if (getTick() != another.getTick())
      return false;
    byte[] bb1 = getMessage().getMessage();
    byte[] bb2 = another.getMessage().getMessage();
    if (bb1.length != bb2.length)
      return false;
    for (int i = 0; i < bb1.length; i++) {
      if (bb1[i] != bb2[i])
        return false;
    }
    return true;
  }

  public static MidiEventWithTicktime 
  createShortMessageEvent(byte[] message, long tick, long position) {
    try {
      ShortMessage msg = new ShortMessage();
      msg.setMessage(message[0], message[1], message[2]);
      return new MidiEventWithTicktime(msg, tick, position);
    } catch (InvalidMidiDataException e) {
      throw new IllegalArgumentException("Invalid value for MIDI");
    }
  }

  public static MidiEventWithTicktime createShortMessageEvent
  (List<? extends Number> message, long tick, long position) {
    int length = message.size();
    byte[] bb = new byte[length];
    for (int i = 0; i < length; i++)
      bb[i] = message.get(i).byteValue();
    return createShortMessageEvent(bb, tick, position);
  }


  public static MidiEventWithTicktime createNoteOnEvent(long position, int ch,
                                                        int nn, int vel) {
    try {
      ShortMessage msg = new ShortMessage();
      msg.setMessage(ShortMessage.NOTE_ON + ch, nn, vel);
      return new MidiEventWithTicktime(msg, position, position);  //2016.11.04
      //return new MidiEventWithTicktime(msg, 0, position);
    } catch (InvalidMidiDataException e) {
      throw new IllegalArgumentException("Invalid value for MIDI");
    }
  }

  public static MidiEventWithTicktime createNoteOffEvent(long position, int ch,
                                                         int nn, int vel) {
    try {
      ShortMessage msg = new ShortMessage();
      msg.setMessage(ShortMessage.NOTE_OFF + ch, nn, vel);
      return new MidiEventWithTicktime(msg, position, position); //2016.11.04
      //return new MidiEventWithTicktime(msg, 0, position);
    } catch (InvalidMidiDataException e) {
      throw new IllegalArgumentException("Invalid value for MIDI");
    }
  }

  public static MidiEventWithTicktime createShortMessageEvent(long position, 
                                                              int ch, int st,
                                                              int d1, int d2){
    try {
      ShortMessage msg = new ShortMessage();
      msg.setMessage(st + ch, d1, d2);
      return new MidiEventWithTicktime(msg, position, position); //2016.11.04
      //return new MidiEventWithTicktime(msg, 0, position);
    } catch (InvalidMidiDataException e) {
      throw new IllegalArgumentException("Invalid value for MIDI");
    }
  }

  public static MidiEventWithTicktime createControlChangeEvent(long position,
                                                               int ch, int type,
                                                               int value) {
    return createShortMessageEvent(position, ch, ShortMessage.CONTROL_CHANGE,
                                   type, value);
  }

  public static MidiEventWithTicktime createProgramChangeEvent(long position,
                                                               int ch, 
                                                               int value) {
    return createShortMessageEvent(position, ch, ShortMessage.PITCH_BEND, 
                                   value, 0);
  }
}
