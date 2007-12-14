package jp.crestmuse.cmx.misc;
import java.util.*;

public class SimpleNoteList implements Iterable<NoteCompatible> {
  private int serial = 0; 
  private String name = "";
  private ArrayList<NoteCompatible> notelist;
  private int lastOffset = 0;
  private int nnBottom = 128;
  private int nnTop = 0;
  private int ticksPerBeat;

  public SimpleNoteList(int serial, int ticksPerBeat) {
    this.serial = serial;
    this.ticksPerBeat = ticksPerBeat;
    notelist = new ArrayList<NoteCompatible>();
  }

  public SimpleNoteList(String name, int ticksPerBeat) {
    this.name = name;
    this.ticksPerBeat = ticksPerBeat;
    notelist = new ArrayList<NoteCompatible>();
  }

  public SimpleNoteList(int serial, String name, int ticksPerBeat) {
    this.serial = serial;
    this.name = name;
    this.ticksPerBeat = ticksPerBeat;
    notelist = new ArrayList<NoteCompatible>();
  }

  public void add(NoteCompatible note) {
    notelist.add(note);
    if (note.offset(ticksPerBeat) > lastOffset)
      lastOffset = note.offset(ticksPerBeat);
    if (note.notenum() > nnTop)
      nnTop = note.notenum();
    if (note.notenum() < nnBottom)
      nnBottom = note.notenum();
  }

  public Iterator<NoteCompatible> iterator() {
    return notelist.iterator();
  }

  public int size() {
    return notelist.size();
  }

  public NoteCompatible get(int i) {
    return notelist.get(i);
  }

  public int serial() {
    return serial;
  }

  public String name() {
    return name;
  }

  public int lastOffset() {
    return lastOffset;
  }

  public int bottomNoteNum() {
    return nnBottom;
  }

  public int topNoteNum() {
    return nnTop;
  }
}