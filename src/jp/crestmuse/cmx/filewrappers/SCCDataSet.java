package jp.crestmuse.cmx.filewrappers;

import jp.crestmuse.cmx.elements.*;
import java.util.*;
import java.io.*;
import groovy.lang.*;
import javax.xml.transform.*;

public class SCCDataSet implements SCC {

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

    public void addNoteElementWithWord(String word, int onset, int offset, 
                                       int notenum, 
                                       int velocity, int offVelocity) {
      notes.add(new MutableNote(onset, offset, notenum, velocity, offVelocity,
                                word, division));
    }

    public void addControlChange(int time, int ctrl, int value) {
      notes.add(new MutableControlChange(time, ctrl, value, division));
    }

    public void addPitchBend(int time, int value) {
      notes.add(new MutablePitchBend(time, value, division));
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
  }

  private int division;

  private List<HeaderElement> headers = new ArrayList<HeaderElement>();
  private List<Part> parts = new ArrayList<Part>();
  private List<MutableAnnotation> annotations = new ArrayList<MutableAnnotation>();

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
        SCC.Note[] notelist = p.getNoteList();
        for (SCC.Note n : notelist) {
          if (n instanceof MutableControlChange) {
            MutableControlChange cc = (MutableControlChange)n;
            newscc.addControlChange(cc.onset(div), cc.ctrlnum(), cc.value());
          } else if (n instanceof MutablePitchBend) {
            MutablePitchBend pb = (MutablePitchBend)n;
            newscc.addPitchBend(pb.onset(div), pb.value());
          } else {
            if (n.word() == null)
              newscc.addNoteElement(n.onset(div), n.offset(div), n.notenum(), 
                                    n.velocity(), n.offVelocity());
            else 
              newscc.addNoteElementWithWord(n.word(), n.onset(div), 
                                            n.offset(div),
                                            n.notenum(), n.velocity(), 
                                            n.offVelocity());
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
  

}

