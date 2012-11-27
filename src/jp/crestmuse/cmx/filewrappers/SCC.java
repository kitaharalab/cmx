package jp.crestmuse.cmx.filewrappers;
import groovy.lang.*;
import javax.xml.transform.*;
import jp.crestmuse.cmx.elements.*;
import java.util.*;

public interface SCC {
  public int getDivision();
  public void eachnote(Closure c) throws TransformerException;
  public void eachpart(Closure c) throws TransformerException;
  public void eachchord(Closure c) throws TransformerException;
  public Annotation[] getAnnotationList();
  public Annotation[] getBarlineList();
  public Annotation[] getChordList();
  public HeaderElement[] getHeaderElementList();
  public Part[] getPartList() throws TransformerException;
  public SCCXMLWrapper toWrapper() throws TransformerException;
  public SCCDataSet toDataSet() throws TransformerException;

  public interface HeaderElement extends Comparable<HeaderElement> {
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
    public Note[] getNoteList();
    public Note[] getNoteOnlyList();
    public Note[] getSortedNoteList();
    public Note[] getSortedNoteOnlyList();
    public void eachnote(Closure c) throws TransformerException;
  }

  public interface Note {
    int onset(int ticksPerBeat);
    int onsetInMilliSec();
    int offset(int ticksPerBeat);
    int offsetInMilliSec();
    int duration(int ticksPerBeat);
    int notenum();
    int velocity();
    int offVelocity();
    /** @deprecated */
    String word();
    boolean hasAttribute(String key);
    String getAttribute(String key);
    int getAttributeInt(String key);
    double getAttributeDouble(String key);
    Set<String> getAttributeKeys();
    Map<String,String> getAttributes();
  }

  public interface Annotation {
    int onset(int ticksPerBeat);
    int offset(int ticksPerBeat);
    String type();
    String content();
}

}
