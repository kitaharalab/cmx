package jp.crestmuse.cmx.elements;
import java.util.*;
import jp.crestmuse.cmx.filewrappers.*;

public abstract class MutableMusicEvent 
  implements Comparable<MutableMusicEvent>, NoteCompatible {

  int onset, offset, value1, value2, value3;
  int ticksPerBeat;
  Type type;
  enum Type {NOTE, CONTROL_CHANGE, PROGRAM_CHANGE, PITCH_BEND, ANNOTATION};
  MusicXMLWrapper.MusicData obj;

  Map<String,String> attr = new TreeMap<String,String>();


  MutableMusicEvent(Type type, int onset, int offset, int ticksPerBeat) {
    this.type = type;
    this.onset = onset;
    this.offset = offset;
    value1 = value2 = value3 = 0;
    this.ticksPerBeat = ticksPerBeat;
  }

  public void setOnset(int onset) {
    this.onset = onset;
  }

  public void setOffset(int offset) {
    this.offset = offset;
  }

  public int onset() {
    return onset;
  }

  public int onset(int ticksPerBeat) {
    if (ticksPerBeat == this.ticksPerBeat)
      return onset;
    else
      return onset * ticksPerBeat / this.ticksPerBeat;
  }

  /**@deprecated*/
  public int onsetInMSec() {
    throw new UnsupportedOperationException();
  }


  public int onsetInMilliSec() {
    throw new UnsupportedOperationException();
  }

  public int offset() {
    return offset;
  }

  public int offsetInMilliSec() {
    throw new UnsupportedOperationException();
  }

  /**@deprecated*/
  public int offsetInMSec() {
    throw new UnsupportedOperationException();
  }

  public int offset(int ticksPerBeat) {
    if (ticksPerBeat == this.ticksPerBeat)
      return offset;
    else
      return offset * ticksPerBeat / this.ticksPerBeat;
  }

  public int duration(int ticksPerBeat) {
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
    return onset + offset + value1 + value2 + value3;
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
       : offset - another.offset)
      : onset - another.onset;
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
  public String word() {
    throw new UnsupportedOperationException();
  }

  public boolean hasAttribute(String key) {
    return attr.containsKey(key);
  }

  public void setAttribute(String key, String value) {
    attr.put(key, value);
  }

  public void setAttribute(String key, int value) {
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

}