package jp.crestmuse.cmx.misc;

public class MutableControlChange extends MutableMusicEvent {
  public MutableControlChange(int timestamp, int ctrlnum, int value, 
                              int ticksPerBeat) {
    super(Type.CONTROL_CHANGE, timestamp, timestamp, ticksPerBeat);
    value1 = ctrlnum;
    value2 = value;
  }
  public void setCtrlNum(int ctrlnum) {
    value1 = ctrlnum;
  }
  public int ctrlnum() {
    return value1;
  }
  public void setValue(int value) {
    value2 = value;
  }
  public int value() {
    return value2;
  }
}