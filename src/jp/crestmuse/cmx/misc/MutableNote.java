package jp.crestmuse.cmx.misc;

public class MutableNote extends MutableMusicEvent 
  implements NoteCompatible  {

  public MutableNote(int onset, int offset, int notenum, int velocity, 
                     int ticksPerBeat) {
    this(onset, offset, notenum, velocity, velocity, ticksPerBeat);
  }
  public MutableNote(int onset, int offset, int notenum, int velocity, 
                     int offVelocity, int ticksPerBeat) {
    super(Type.NOTE, onset, offset, ticksPerBeat);
    value1 = notenum;
    value2 = velocity;
    value3 = offVelocity;
  }
  public void setNoteNum(int notenum) {
    value1 = notenum;
  }
  public int notenum() {
    return value1;
  }
  public void setVelocity(int vel) {
    value2 = vel;
  }
  public int velocity() {
    return value2;
  }
  public void setOffVelocity(int offvel) {
    value3 = offvel;
  }
  public int offVelocity() {
    return value3;
  }
}
