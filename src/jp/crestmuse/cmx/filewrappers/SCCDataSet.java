package jp.crestmuse.cmx.filewrappers;

import jp.crestmuse.cmx.elements.*;
import java.util.*;
import groovy.lang.*;
import javax.xml.transform.*;

public class SCCDataSet {

  public class HeaderElement implements SCC.HeaderElement {
    private String content;
    private String name;
    private int time;
    private HeaderElement(int time, String name, String content) {
      this.time = time;
      this.name = name;
      this.content = content;
    }
    public String content() {
      return content;
    }
    public String name() {
      return name;
    }
    public int time() {
      return time;
    }
  }

  public class Part implements SCC.Part {
    private List<MutableMusicEvent> notes = new ArrayList<MutableMusicEvent>();
    private byte channel;
    private int panpot, prognum, serial, volume;
    String name;
    
    private Part(int serial, byte ch, int pn, int vol, String name) {
      this.serial = serial;
      channel = ch;
      prognum = pn;
      volume = vol;
      this.name = name;
    }
    
    public void addNoteElement(int onset, int offset, int notenum, 
                               int velocity, int offVelocity) {
      notes.add(new MutableNote(onset, offset, notenum, velocity, offVelocity, 
                                division));
    }

    public void addControlChange(int time, int ctrl, int value) {
      notes.add(new MutableControlChange(time, ctrl, value, division));
    }

    public void addPitchBend(int time, int value) {
      notes.add(new MutablePitchBend(time, value, division));
    }

    public void eachnote(Closure closure) throws TransformerException {
      MutableMusicEvent[] notelist = getNoteList();
      for (MutableMusicEvent note : notelist) {
        closure.call(new Object[]{note});
      }
    }

    public MutableMusicEvent[] getNoteList() {
      return notes.toArray(new MutableMusicEvent[notes.size()]);
    }

    public MutableNote[] getNoteOnlyList() {
      List<MutableNote> l = new ArrayList<MutableNote>();
      for (MutableMusicEvent e : notes) {
        if (e instanceof MutableNote) 
          l.add((MutableNote)e);
      }
      return l.toArray(new MutableNote[l.size()]);
    }

    public MutableMusicEvent[] getSortedNoteList() {
      MutableMusicEvent[] l = (MutableMusicEvent[])(getNoteList().clone());
      Arrays.sort(l);
      return l;
    }

    public MutableNote[] getSortedNoteOnlyList() {
      MutableNote[] l = (MutableNote[])(getNoteOnlyList().clone());
      Arrays.sort(l);
      return l;
    }

    public int serial() {
      return serial;
    }

    public byte channel() {
      return channel;
    }

    public int prognum() {
      return prognum;
    }

    public int volume() {
      return volume;
    }

    public int panpot() {
      return panpot;
    }

    public String name() {
      return name;
    }
  }

  private int division;

  private List<HeaderElement> headers = new ArrayList<HeaderElement>();
  private List<Part> parts = new ArrayList<Part>();
  private List<MutableAnnotation> annotations = new ArrayList<MutableAnnotation>();

  public void addAnnotation(String type, int onset, int offset, String content) {
    annotations.add(new MutableAnnotation(onset, offset, type, content, 
                                          division));
  }

  public void addBarline(int time, String details) {
    addAnnotation("barline", time, time, details);
  }

  public void addChord(int onset, int offset, String content) {
    addAnnotation("chord", onset, offset, content);
  }

  public Part addPart(int serial, byte ch, int pn, int vol, java.lang.String name) {
    Part part = new Part(serial, ch, pn, vol, name);
    parts.add(part);
    return part;
  }

  
}

