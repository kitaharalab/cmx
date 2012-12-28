package jp.crestmuse.cmx.filewrappers;

import jp.crestmuse.cmx.elements.*;
import java.util.*;
import java.io.*;
import groovy.lang.*;
import javax.xml.transform.*;

public class SCCDataSet implements SCC,Cloneable {

  public class HeaderElement implements SCC.HeaderElement {
    String content;
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
    public boolean equals(SCC.HeaderElement e) {
      return time == e.time() && name.equals(e.name()) && 
        content.equals(e.content());
    }
    public int compareTo(SCC.HeaderElement e) {
      if (time != e.time())
        return time - e.time();
      else if (!name.equals(e.name()))
        return name.compareTo(e.name());
      else if (!content.equals(e.content()))
        return content.compareTo(e.content());
      else
        return 0;
    }
  }

  private class MyNumber {
    int value = 0;
  }

  public class Part implements SCC.Part {
    private List<MutableMusicEvent> notes = new ArrayList<MutableMusicEvent>();
    private Map<MutableMusicEvent,MyNumber> nOverlaps = 
      new HashMap<MutableMusicEvent,MyNumber>();
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
    
    private void checkOverlap(MutableMusicEvent note) {
      if (!nOverlaps.containsKey(note))
        nOverlaps.put(note, new MyNumber());
      nOverlaps.get(note).value++;
      note.setAttribute("number", nOverlaps.get(note).value);
    }
    
    public boolean remove(MutableMusicEvent e) {
      return notes.remove(e);
    }
    
    MutableNote addNoteElement(int onset, int offset, int notenum, 
                                      int velocity, int offVelocity, 
                                      MusicXMLWrapper.MusicData md) {
      MutableNote note = addNoteElement(onset, offset, notenum, 
                                        velocity, offVelocity);
      note.setMusicXMLObject(md);
      return note;
    }

    public MutableNote addNoteElement(int onset, int offset, int notenum, 
                               int velocity, int offVelocity) {
      MutableNote note = new MutableNote(onset, offset, notenum, velocity, 
                                         offVelocity, division);
      notes.add(note);
      checkOverlap(note);
      return note;
    }
    
    public MutableNote addNoteElement(int onset, int offset, int notenum, 
                                      int velocity, int offVelocity, 
                                      Map<String,String> attr) {
      MutableNote note = new MutableNote(onset, offset, notenum, velocity, 
                                         offVelocity, division, attr);
      notes.add(note);
      checkOverlap(note);
      return note;
    }

    /** @deprecated */
    public MutableNote addNoteElementWithWord(String word, int onset, 
                                              int offset, int notenum, 
                                       int velocity, int offVelocity) {
      MutableNote note = new MutableNote(onset, offset, notenum, velocity, 
                                         offVelocity, word, division);
      notes.add(note);
      checkOverlap(note);
      return note;
    }


    public MutableControlChange addControlChange(int time, int ctrl, int value) {
      MutableControlChange cc = new MutableControlChange(time, ctrl, value, division);
      notes.add(cc);
      checkOverlap(cc);
      return cc;
    }

    public MutablePitchBend addPitchBend(int time, int value) {
      MutablePitchBend pb = new MutablePitchBend(time, value, division);
      notes.add(pb);
      checkOverlap(pb);
      return pb;
    }

    BaseDynamicsEvent addBaseDynamics(int time, double value) {
      BaseDynamicsEvent e = new BaseDynamicsEvent(time, value, division);
      notes.add(e);
      return e;
    }

    public void eachnote(Closure closure) throws TransformerException {
      SCCUtils.eachnote(this, closure);
    }

/*
    public void eachnote(Closure closure) throws TransformerException {
      MutableMusicEvent[] notelist = getNoteList();
      for (MutableMusicEvent note : notelist) {
        closure.call(new Object[]{note});
      }
    }
*/

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

    int division() {
      return division;
    }
  }

  private int division;

  private Set<HeaderElement> headers = new TreeSet<HeaderElement>();
  private List<Part> parts = new ArrayList<Part>();
  private Set<MutableAnnotation> annotations = new TreeSet<MutableAnnotation>();

  public SCCDataSet(int division) {
    this.division = division;
  }

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

  public void addHeaderElement(int time, java.lang.String name, java.lang.String content) {
    headers.add(new HeaderElement(time, name, content));
  }

  public void addHeaderElement(int time, String name, int content) {
    addHeaderElement(time, name, String.valueOf(content));
  }

  public void addHeaderElement(int time, String name, double content) {
    addHeaderElement(time, name, String.valueOf(content));
  }

  public Part addPart(int serial, int ch, int pn, int vol) {
    return addPart(serial, ch, pn, vol, (String)null);
  }
  
  public Part addPart(int serial, int ch, int pn, int vol, String name) {
    Part part = new Part(serial, (byte)ch, pn, vol, name);
    parts.add(part);
    return part;
  }

  public void addPart(int serial, int ch, int pn, int vol, Closure closure) {
    addPart(serial, ch, pn, vol, null, closure);
  }

  public void addPart(int serial, int ch, int pn, int vol, String name, 
                      Closure closure) {
    Part part = addPart(serial, ch, pn, vol, name);
    closure.call(new Object[]{part});
  }

  public Part getPart(String name) {
    for (Part p : parts)
      if (name.equals(p.name()))
        return p;
    return null;
  }

  public Part[] getPartList() {
    return parts.toArray(new Part[parts.size()]);
  }
  
  public Part getPart(int i) {
    return parts.get(i);
  }

  public Part getPartWithSerial(int serial) {
    for (Part p : parts) 
      if (p.serial() == serial)
        return p;
    return null;
  }

  public Part[] getPartsWithChannel(int ch) {
    List<Part> l = new ArrayList<Part>();
    for (Part p : parts) 
      if (p.channel() == ch)
        l.add(p);
    return l.toArray(new Part[l.size()]);
  }

  public Part getFirstPartWithChannel(int ch) {
    for (Part p : parts)
      if (p.channel() == ch)
        return p;
    return null;
  }

  public HeaderElement[] getHeaderElementList() {
    return headers.toArray(new HeaderElement[headers.size()]);
  }

  public MutableAnnotation[] getAnnotationList() {
    return annotations.toArray(new MutableAnnotation[annotations.size()]);
  }

  public SCC.Annotation[] getChordList() {
    return SCCUtils.getChordList(this);
  }

  public SCC.Annotation[] getBarlineList() {
    return SCCUtils.getBarlineList(this);
  }


  public boolean removeAnnotation(MutableAnnotation a) {
    return annotations.remove(a);
  }

  public boolean removeHeaderElement(HeaderElement e) {
    return headers.remove(e);
  }

  public boolean removePart(Part p) {
    return parts.remove(p);
  }

  public void sortParts(Comparator<Part> c) {
    Collections.sort(parts, c);
  }

  public int getDivision() {
    return division;
  }
    
  public void eachpart(Closure closure) throws TransformerException {
    SCCUtils.eachpart(this, closure);
  }

  public void eachnote(Closure closure) throws TransformerException {
    SCCUtils.eachnote(this, closure);
  }
  
  public void eachchord(Closure closure) throws TransformerException {
    SCCUtils.eachchord(this, closure);
  }

  public void eachbarline(Closure closure) throws TransformerException {
    SCCUtils.eachbarline(this, closure);
  }

//  public SCCXMLWrapper toWrapper() throws TransformerException {
//    return SCCUtils.toWrapper(this);
//  }

  public SCCDataSet toDataSet() throws TransformerException {
    return this;
  }

  public SCCXMLWrapper toWrapper() throws TransformerException {
    try {
      SCCXMLWrapper newscc = 
        (SCCXMLWrapper)CMXFileWrapper.createDocument(SCCXMLWrapper.TOP_TAG);
      int div = getDivision();
      newscc.setDivision(div);
      HeaderElement[] headerlist = getHeaderElementList();
      newscc.beginHeader();
      for (HeaderElement h : headerlist) {
        newscc.addHeaderElement(h.time(), h.name(), h.content());
      }
      newscc.endHeader();
      Part[] partlist = getPartList();
      for (Part p : partlist) {
        newscc.newPart(p.serial(), p.channel(), p.prognum(), 
                       p.volume(), p.name());
        MutableMusicEvent[] notelist = p.getSortedNoteList();        // modified on 2012.11.19
//////        SCC.Note[] notelist = p.getNoteList();
        for (MutableMusicEvent n : notelist) {
          if (n instanceof MutableControlChange) {
            MutableControlChange cc = (MutableControlChange)n;
            newscc.addControlChange(cc.onset(div), cc.ctrlnum(), cc.value());
          } else if (n instanceof MutablePitchBend) {
            MutablePitchBend pb = (MutablePitchBend)n;
            newscc.addPitchBend(pb.onset(div), pb.value());
          } else {
            if (n.getMusicXMLObject() != null)
              newscc.addNoteElement(
                n.onset(div), n.offset(div), n.notenum(), n.velocity(), 
                n.offVelocity(), n.getAttributes(), 
                (MusicXMLWrapper.Note)n.getMusicXMLObject());
            else
              newscc.addNoteElement(
                n.onset(div), n.offset(div), n.notenum(), n.velocity(), 
                n.offVelocity(), n.getAttributes()
              );
//            if (n.word() == null)
//              newscc.addNoteElement(n.onset(div), n.offset(div), n.notenum(), 
//                                    n.velocity(), n.offVelocity());
//            else 
//              newscc.addNoteElementWithWord(n.word(), n.onset(div), 
//                                            n.offset(div),
//                                            n.notenum(), n.velocity(), 
//                                            n.offVelocity());
          }
        }
        newscc.endPart();
      }
      newscc.beginAnnotations();
      SCC.Annotation[] annlist = getAnnotationList();
      for (SCC.Annotation a : annlist) {
        newscc.addAnnotation(a.type(), a.onset(div), a.offset(div), 
                             a.content());
      }
      newscc.endAnnotations();
      newscc.finalizeDocument();
      return newscc;
    } catch (InvalidFileTypeException e) {
      throw new IllegalStateException();
    } catch (IOException e) {
      e.printStackTrace();
      throw new TransformerException(e.toString());
    }
  }

  public SCCDataSet clone() {
    try {
      return (SCCDataSet)super.clone();
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
      throw new InternalError(e.toString());
    }
  }

  public SCC.HeaderElement getFirstTempo() {
    return SCCUtils.getFirstHeader(this, "TEMPO");
  }

  public SCC.HeaderElement getFirstKey() {
    return SCCUtils.getFirstHeader(this, "KEY");
  }


}

