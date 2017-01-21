package jp.crestmuse.cmx.elements;
import java.util.*;
import jp.crestmuse.cmx.filewrappers.*;
import javax.sound.midi.*;

public abstract class MutableMusicEvent 
  implements Comparable<MutableMusicEvent>, SCC.Note {

  long onset, offset;
  int value1, value2, value3;
  long onsetInMSec = -1, offsetInMSec = -1;
  int ticksPerBeat;
  Type type;
  enum Type {NOTE, CONTROL_CHANGE, PROGRAM_CHANGE, PITCH_BEND, ANNOTATION};
  MusicXMLWrapper.MusicData obj;
  MidiEvent midievt1 = null, midievt2 = null;

  Map<String,String> attr = new TreeMap<String,String>();


  MutableMusicEvent(Type type, long onset, long offset, int ticksPerBeat) {
    this.type = type;
    this.onset = onset;
    this.offset = offset;
    value1 = value2 = value3 = 0;
    this.ticksPerBeat = ticksPerBeat;
  }

  public void setOnset(long onset) {
    if (midievt1 != null) {
      if (this.onset == midievt1.getTick())
        midievt1.setTick(onset);
      else
        throw new IllegalStateException();
    }
    this.onset = onset;
    onsetInMSec = -1;
  }

  public void setOffset(long offset) {
    if (midievt2 != null) {
      if (this.offset == midievt2.getTick())
        midievt2.setTick(offset);
      else
        throw new IllegalStateException();
    }
    this.offset = offset;
    offsetInMSec = -1;
  }

  public long onset() {
    return onset;
  }

  public long onset(int ticksPerBeat) {
    if (ticksPerBeat == this.ticksPerBeat)
      return onset;
    else
      return onset * ticksPerBeat / this.ticksPerBeat;
  }

  //  /**@deprecated*/
  //  public int onsetInMSec() {
  //    throw new UnsupportedOperationException();
  //  }


  public long onsetInMilliSec() {
    if (onsetInMSec == -1)
      throw new IllegalStateException();
    else
      return onsetInMSec;
  }

  public long offset() {
    return offset;
  }

  public long offsetInMilliSec() {
    if (offsetInMSec == -1)
      throw new IllegalStateException();
    else
      return offsetInMSec;
  }

  //  /**@deprecated*/
  //  public int offsetInMSec() {
  //    throw new UnsupportedOperationException();
  //  }

  public long durationInMilliSec() {
    return offsetInMilliSec() - onsetInMilliSec();
  }
  
  public long offset(int ticksPerBeat) {
    if (ticksPerBeat == this.ticksPerBeat)
      return offset;
    else
      return offset * ticksPerBeat / this.ticksPerBeat;
  }

  public long duration(int ticksPerBeat) {
    return offset(ticksPerBeat) - onset(ticksPerBeat);
  }

  protected int ticksPerBeat() {
    return ticksPerBeat;
  }

  public boolean equals(Object o) {
    MutableMusicEvent another = (MutableMusicEvent)o;
    return (type.equals(another.type) && 
            (onset == another.onset) && (offset == another.offset) && 
            (value1 == another.value1) && (value2 == another.value2) &&
            (value3 == another.value3));
  }

  public int hashCode() {
    return (int)(onset + offset + value1 + value2 + value3);
  }

  public int compareTo(MutableMusicEvent another) {
    return onset == another.onset ? 
      (offset == another.offset ?
       (type.equals(another.type) ?
        (value1 == another.value1 ? 
         (value2 == another.value2 ? 
          value3 - another.value3 : value2 - another.value2)
         : value1 - another.value1)
        : type.ordinal() - another.type.ordinal())
       : (int)(offset - another.offset))
      : (int)(onset - another.onset);
  }

  public int velocity() {
    throw new UnsupportedOperationException();
  }

  public int offVelocity() {
    throw new UnsupportedOperationException();
  }

  public int notenum() {
    throw new UnsupportedOperationException();
  }

  /** @deprecated */
  //  public String word() {
  //    throw new UnsupportedOperationException();
  //  }

  public boolean hasAttribute(String key) {
    return attr.containsKey(key);
  }

  public void setAttribute(String key, String value) {
    attr.put(key, value);
  }

  public void setAttribute(String key, int value) {
    attr.put(key, String.valueOf(value));
  }

  public void setAttribute(String key, long value) {
    attr.put(key, String.valueOf(value));
  }
  
  public void setAttribute(String key, double value) {
    attr.put(key, String.valueOf(value));
  }

  public String getAttribute(String key) {
    return attr.get(key);
  }

  public int getAttributeInt(String key) {
    return Integer.parseInt(attr.get(key));
  }

  public long getAttributeLong(String key) {
    return Long.parseLong(attr.get(key));
  }
  
  public double getAttributeDouble(String key) {
    return Double.parseDouble(attr.get(key));
  }

  public Set<String> getAttributeKeys() {
    return attr.keySet();
  }

  public Map<String,String> getAttributes() {
    return attr;
  }

  public void removeAttribute(String key) {
    attr.remove(key);
  }

  public void setMusicXMLObject(MusicXMLWrapper.MusicData o) {
    obj = o;
  }

  public MusicXMLWrapper.MusicData getMusicXMLObject() {
    return obj;
  }

  /** Do not this method in your application. 
      This is an inner method even though it's public */
  public void setMidiEvents(MidiEvent e1, MidiEvent e2) {
    midievt1 = e1;
    midievt2 = e2;
  }

  /** Do not this method in your application. 
      This is an inner method even though it's public */
  public MidiEvent getMidiEvent1() {
    return midievt1;
  }

  /** Do not this method in your application. 
      This is an inner method even though it's public */
  public MidiEvent getMidiEvent2() {
    return midievt2;
  }

  /** Do not this method in your application. 
      This is an inner method even though it's public */
  public void setOnsetInMilliSec(long onset) {
    onsetInMSec = onset;
  }

  /** Do not this method in your application. 
      This is an inner method even though it's public */
  public void setOffsetInMilliSec(long offset) {
    offsetInMSec = offset;
  }
  
}
