package jp.crestmuse.cmx.elements;

public class MutableNote extends MutableMusicEvent 
  implements NoteCompatible  {

  private String word = null;

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
  public MutableNote(int onset, int offset, int notenum, int velocity, 
                     int offVelocity, String word, int ticksPerBeat) {
    this(onset, offset, notenum, velocity, offVelocity, ticksPerBeat);
    this.word = word;
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
  public String word() {
    return word;
  }
  public void setWord(String w) {
    word = w;
  }
  public String toString() {
    return "[onset: " + onset() + ", offset: " + offset() + 
      ", notenum: " + notenum() + ", velocity: " + velocity() + 
      ", offVelocity: " + offVelocity() + 
      (word == null ? "" : (", word: " + word)) + "]";
  }
}
