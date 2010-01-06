package jp.crestmuse.cmx.misc;

public abstract class MutableMusicEvent 
  implements Comparable<MutableMusicEvent> {

  int onset, offset, value1, value2, value3;
  int ticksPerBeat;
  Type type;
  enum Type {NOTE, CONTROL_CHANGE, PITCH_BEND} 

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

  public int onsetInMSec() {
    throw new UnsupportedOperationException();
  }

  public int offset() {
    return offset;
  }

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
}