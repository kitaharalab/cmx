package jp.crestmuse.cmx.elements;

public class MutableProgramChange extends MutableMusicEvent {
  public MutableProgramChange(int timestamp, int value, 
                              int ticksPerBeat) {
    super(Type.PROGRAM_CHANGE, timestamp, timestamp, ticksPerBeat);
    value1 = value;
  }
  public void setValue(int value) {
    value1 = value;
  }
  public int value() {
    return value1;
  }
}