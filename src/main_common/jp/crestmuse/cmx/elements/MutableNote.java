package jp.crestmuse.cmx.elements;
import java.util.*;
import javax.sound.midi.*;

public class MutableNote extends MutableMusicEvent {


//  private String word = null;

  public MutableNote(long onset, long offset, int notenum, int velocity, 
                     int ticksPerBeat) {
    this(onset, offset, notenum, velocity, velocity, ticksPerBeat);
  }
  public MutableNote(long onset, long offset, int notenum, int velocity, 
                     int offVelocity, int ticksPerBeat) {
    super(Type.NOTE, onset, offset, ticksPerBeat);
    value1 = notenum;
    value2 = velocity;
    value3 = offVelocity;
  }
  public MutableNote(long onset, long offset, int notenum, int velocity, 
                     int offVelocity, int ticksPerBeat, 
                     Map<String,String> attr) {
    this(onset, offset, notenum, velocity, offVelocity, ticksPerBeat);
    if (attr != null)
      this.attr.putAll(attr);
    //    this.attr = attr;
  }

  /*
  public MutableNote(int onset, int offset, int notenum, int velocity, 
                     int offVelocity, String word, int ticksPerBeat) {
    this(onset, offset, notenum, velocity, offVelocity, ticksPerBeat);
//    this.word = word;
    setAttribute("word", word);
    }*/


  private void changeData(int value1, int value2, MidiEvent evt) {
    try {
      MidiMessage msg = evt.getMessage();
      if (msg instanceof ShortMessage) {
        ShortMessage sm = (ShortMessage)msg;
        sm.setMessage(sm.getStatus(),
                      value1 >= 0 ? value1 : sm.getData1(),
                      value2 >= 0 ? value2 : sm.getData2());
      } else {
        throw new IllegalStateException();
      }
    } catch (InvalidMidiDataException e) {
      e.printStackTrace();
      throw new IllegalStateException(e.toString());
    }
  }
  
  public void setNoteNum(int notenum) {
    value1 = notenum;
    if (midievt1 != null) {
      changeData(notenum, -1, midievt1);
    }
    if (midievt2 != null) {
      changeData(notenum, -1, midievt2);
    }
  }
  
  public int notenum() {
    return value1;
  }
  
  public void setVelocity(int vel) {
    value2 = vel;
    if (midievt1 != null) {
      changeData(-1, vel, midievt1);
    }
  }
  
  public int velocity() {
    return value2;
  }
  
  public void setOffVelocity(int offvel) {
    value3 = offvel;
    if (midievt2 != null) {
      changeData(-1, offvel, midievt2);
    }
  }
  
  public int offVelocity() {
    return value3;
  }
  
  //  /** @deprecated */
  //  public String word() {
  //    return getAttribute("word");
  //  }
  //  /** @deprecated */
  //  public void setWord(String w) {
  //    setAttribute("word", w);
  //  }
  
  public String toString() {
    return "[onset: " + onset() + ", offset: " + offset() + 
      ", notenum: " + notenum() + ", velocity: " + velocity() + 
      ", offVelocity: " + offVelocity() + 
//      (word == null ? "" : (", word: " + word)) + 
"]";
  }
}
