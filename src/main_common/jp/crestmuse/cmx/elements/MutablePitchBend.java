package jp.crestmuse.cmx.elements;

public class MutablePitchBend extends MutableMusicEvent {
  public MutablePitchBend(long timestamp, int value, int ticksPerBeat) {
    super(Type.PITCH_BEND, timestamp, timestamp, ticksPerBeat);
    value1 = value;
  }
  public void setValue(int value) {
    value1 = value;
  }
  public int value() {
    return value1;
  }
}
