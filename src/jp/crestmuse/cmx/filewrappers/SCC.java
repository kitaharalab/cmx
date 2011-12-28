package jp.crestmuse.cmx.filewrappers;
import groovy.lang.*;
import javax.xml.transform.*;
import jp.crestmuse.cmx.elements.*;

public interface SCC {
  public int getDivision();
  public void eachnote(Closure c) throws TransformerException;
  public void eachpart(Closure c) throws TransformerException;
  public MusicAnnotation[] getBarlineList();
  public MusicAnnotation[] getChordList();
  public HeaderElement[] getHeaderElementList();
  public Part[] getPartList() throws TransformerException;

  public interface HeaderElement {
    String content();
    String name();
    int time();
  }

  public interface Part {
    public String name();
    public byte channel();
    public int panpot();
    public int prognum();
    public int serial();
    public int volume();
    public NoteCompatible[] getNoteList();
    public NoteCompatible[] getNoteOnlyList();
    public NoteCompatible[] getSortedNoteList();
    public NoteCompatible[] getSortedNoteOnlyList();
    public void eachnote(Closure c) throws TransformerException;
  }
}
