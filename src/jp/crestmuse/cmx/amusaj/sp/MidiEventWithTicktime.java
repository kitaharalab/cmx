package jp.crestmuse.cmx.amusaj.sp;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;

public class MidiEventWithTicktime extends MidiEvent implements SPElement {
  long music_position;
  public MidiEventWithTicktime(MidiMessage message, long tick, long position) {
    super(message, tick);
    music_position = position;
  }
  public String encode() {
    return null;
  }
  public boolean hasNext() {
    return true;
  }
}
