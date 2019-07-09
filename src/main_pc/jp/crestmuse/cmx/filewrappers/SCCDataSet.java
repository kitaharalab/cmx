package jp.crestmuse.cmx.filewrappers;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Track;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import groovy.lang.Closure;
import jp.crestmuse.cmx.elements.BaseDynamicsEvent;
import jp.crestmuse.cmx.elements.MutableAnnotation;
import jp.crestmuse.cmx.elements.MutableControlChange;
import jp.crestmuse.cmx.elements.MutableMusicEvent;
import jp.crestmuse.cmx.elements.MutableNote;
import jp.crestmuse.cmx.elements.MutablePitchBend;
import jp.crestmuse.cmx.elements.MutableProgramChange;
import jp.crestmuse.cmx.misc.PianoRoll;

import static jp.crestmuse.cmx.amusaj.sp.MidiEventWithTicktime.createControlChangeEvent;
import static jp.crestmuse.cmx.amusaj.sp.MidiEventWithTicktime.createNoteOffEvent;
import static jp.crestmuse.cmx.amusaj.sp.MidiEventWithTicktime.createNoteOnEvent;
import static jp.crestmuse.cmx.amusaj.sp.MidiEventWithTicktime.createProgramChangeEvent;

public class SCCDataSet implements SCC,Cloneable {

  public class HeaderElement implements SCC.HeaderElement {
    String content;
    private String name;
    private long time;
    private HeaderElement(long time, String name, String content) {
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
    public long time() {
      return time;
    }
    public boolean equals(SCC.HeaderElement e) {
      return time == e.time() && name.equals(e.name()) &&
        content.equals(e.content());
    }
    public int compareTo(SCC.HeaderElement e) {
      if (time != e.time())
        return (int)(time - e.time());
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
    //    private List<MutableMusicEvent> notes;
    private SortedSet<MutableMusicEvent> notes;
    //private List<MutableMusicEvent> notes = new ArrayList<MutableMusicEvent>();
    private Map<MutableMusicEvent,MyNumber> nOverlaps =
      new HashMap<MutableMusicEvent,MyNumber>();
    private byte channel;
    private int panpot, prognum, serial, volume;
    String name;
    private int index;

    private Part(int serial, byte ch, int pn, int vol, String name,
                 int index) {
      //      notes = Collections.synchronizedList(new ArrayList<MutableMusicEvent>());
      //      notes = new ArrayList<MutableMusicEvent>();
      notes = new TreeSet<MutableMusicEvent>();
      this.serial = serial;
      channel = ch;
      prognum = pn;
      volume = vol;
      this.name = name;
      this.index = index;
    }

    private void checkOverlap(MutableMusicEvent note) {
      if (!nOverlaps.containsKey(note))
        nOverlaps.put(note, new MyNumber());
      nOverlaps.get(note).value++;
      note.setAttribute("number", nOverlaps.get(note).value);
    }

    public synchronized void remove(MutableMusicEvent e) {
      boolean result = notes.remove(e);
      if (tracks != null) {
        if (e.getMidiEvent1() != null) {
          tracks[index].remove(e.getMidiEvent1());
        }
        if (e.getMidiEvent2() != null) {
          // 仮
          //          tracks[index].remove(e.getMidiEvent2());
        }
      }
      //      return result;
    }

    public void remove(List<MutableMusicEvent> l) {
      for (MutableMusicEvent e : l)
        remove(e);
    }
    
    synchronized void add(MutableMusicEvent e) {
      notes.add(e);
    }


    synchronized MutableNote 
      addNoteElement(long onset, long offset, int notenum,
                     int velocity, int offVelocity,
                     MusicXMLWrapper.MusicData md,
                     Map<String,String> attr) {
      MutableNote note = new MutableNote(onset, offset, notenum, velocity,
                                         offVelocity, division, attr);
      notes.add(note);
      checkOverlap(note);
      if (md != null)
        note.setMusicXMLObject(md);
      if (tracks != null) {
        MidiEvent e1 = createNoteOnEvent(onset, channel-1,
                                         notenum, velocity);
        MidiEvent e2 = createNoteOffEvent(offset, channel-1,
                                         notenum, offVelocity);
        note.setMidiEvents(e1, e2);
        tracks[index].add(e1);
        tracks[index].add(e2);
      }
      return note;
    }

    MutableNote addNoteElement(long onset, long offset, int notenum,
                               int velocity, int offVelocity,
                               MusicXMLWrapper.MusicData md) {
      return addNoteElement(onset, offset, notenum, velocity, offVelocity,
                            md, null);
    }
                               

    public MutableNote addNoteElement(long onset, long offset, int notenum,
                                      int velocity, int offVelocity) {
      return addNoteElement(onset, offset, notenum, velocity, offVelocity,
                            null, null);
    }

    public MutableNote addNoteElement(long onset, long offset, int notenum,
                                      int velocity, int offVelocity,
                                      Map<String,String> attr) {
      return addNoteElement(onset, offset, notenum, velocity,
                            offVelocity, null, attr);
    }

    /*
    public MutableNote addNoteElementWithWord(String word, int onset,
                                              int offset, int notenum,
                                              int velocity, int offVelocity) {
      MutableNote note = new MutableNote(onset, offset, notenum, velocity,
                                         offVelocity, word, division);
      notes.add(note);
      checkOverlap(note);
      if (tracks != null) {
        tracks[index].add(createNoteOnEvent(onset, channel-1,
                                            notenum, velocity));
        
        tracks[index].add(createNoteOffEvent(offset, channel-1,
                                             notenum, offVelocity));
      }
      return note;
    }
    */

    public synchronized MutableControlChange addControlChange(long time, int ctrl, int value) {
      MutableControlChange cc = new MutableControlChange(time, ctrl, value, division);
      notes.add(cc);
      checkOverlap(cc);
      if (tracks != null) {
        MidiEvent e = createControlChangeEvent(time, channel-1, ctrl, value);
        cc.setMidiEvents(e, null);
        tracks[index].add(e);
      }
      return cc;
    }

    public  synchronized MutableProgramChange addProgramChange(long time, int value) {
      MutableProgramChange pc = new MutableProgramChange(time, value, division);
      notes.add(pc);
      checkOverlap(pc);
      if (tracks != null) {
        MidiEvent e = createProgramChangeEvent(time, channel-1, value);
        pc.setMidiEvents(e, null);
        tracks[index].add(e);
      }
      return pc;
    }

    public synchronized MutablePitchBend addPitchBend(long time, int value) {
      MutablePitchBend pb = new MutablePitchBend(time, value, division);
      notes.add(pb);
      checkOverlap(pb);
      // TODO
      // tracksにMidiEventを追加
      return pb;
    }

    synchronized BaseDynamicsEvent addBaseDynamics(long time, double value) {
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

    public synchronized MutableMusicEvent[] getNoteList() {
      return notes.toArray(new MutableMusicEvent[notes.size()]);
    }

    public synchronized  MutableNote[] getNoteOnlyList() {
      List<MutableNote> l = new ArrayList<MutableNote>();
      for (MutableMusicEvent e : notes) {
        if (e instanceof MutableNote)
          l.add((MutableNote)e);
      }
        return l.toArray(new MutableNote[l.size()]);
    }

    public MutableMusicEvent[] getSortedNoteList() {
      return getNoteList();
      //      MutableMusicEvent[] l = (MutableMusicEvent[])(getNoteList().clone());
      //      Arrays.sort(l);
      //      return l;
    }

    public MutableNote[] getSortedNoteOnlyList() {
      return getNoteOnlyList();
      //      MutableNote[] l = (MutableNote[])(getNoteOnlyList().clone());
      //      Arrays.sort(l);
      //      return l;
    }

    /*
    public synchronized List<MutableMusicEvent> getNotesBetween1(int from, int thru) {
      List<MutableMusicEvent> notes2 = new ArrayList<MutableMusicEvent>();
      for (MutableMusicEvent e : notes) {
        if (e.onset() >= from && e.offset() <= thru)
          notes2.add(e);
      }
      return notes2;
    }

    public synchronized List<MutableMusicEvent> getNotesBetween2(int from, int thru) {
      List<MutableMusicEvent> notes2 = new ArrayList<MutableMusicEvent>();
      for (MutableMusicEvent e : notes) {
        if (e.onset() >= from && e.onset() < thru)
          notes2.add(e);
      }
      return notes2;
    }
    */

    public void calcMilliSec() {
      double currentSec = 0.0;
      int currentTick = 0;
      TempoSearch ts = new TempoSearch(getHeaderElementList());
      for (MutableMusicEvent e : notes) {
        if (e.onset() >= ts.nextTempoChangeInTick) 
          ts.nextTempo();
        e.setOnsetInMilliSec((long)(ts.calcSec(e.onset()) * 1000));
        if (e.offset() >= ts.nextTempoChangeInTick)
          ts.nextTempo();
        e.setOffsetInMilliSec((int)(ts.calcSec(e.offset()) * 1000));
      }
    }

    private class TempoSearch {
      HeaderElement[] headers;
      int nextIndex = 0;
      long nextTempoChangeInTick = 0;
      double nextTempoChangeInSec = 0.0;
      long lastTempoChangeInTick = 0;
      double lastTempoChangeInSec = 0.0;
      double nextTempo = 120;
      double lastTempo = 120;
      boolean finished = false;
      TempoSearch(HeaderElement[] headers) {
        this.headers = headers;
      }
      void nextTempo() {
        if (!finished) {
          do {
            if (headers[nextIndex].name().equals("TEMPO")) {
              lastTempoChangeInTick = nextTempoChangeInTick;
              nextTempoChangeInTick = headers[nextIndex].time();
              lastTempoChangeInSec = nextTempoChangeInSec;
              nextTempoChangeInSec = calcSec(nextTempoChangeInTick);
              lastTempo = nextTempo;
              nextTempo = Double.parseDouble(headers[nextIndex].content());
              return;
            }
            nextIndex++;
          } while (nextIndex < headers.length);
          finished = true;
          lastTempoChangeInTick = nextTempoChangeInTick;
          lastTempoChangeInSec = nextTempoChangeInSec;
          lastTempo = nextTempo;
        }
      }
      double calcSec(long tick) {
        long dur_tick = tick - lastTempoChangeInTick;
        double dur_sec = (60.0 * dur_tick) / (division * lastTempo);
        return lastTempoChangeInSec + dur_sec;
      }
    }
    
    
    public int serial() {
      return serial;
    }

    public byte channel() {
      return channel;
    }

    public synchronized int prognum() {
      Note[] notes = getNoteList();
      for (int i = 0; i < notes.length; i++) {
        if (notes[i] instanceof MutableProgramChange) {
          return ((MutableProgramChange)notes[i]).value();
        }
      }
      return prognum;
    }

    private synchronized int getFirstControlChange(int ctrlnum, int defaultvalue) {
      Note[] notes = getNoteList();
      for (int i = 0; i < notes.length; i++) {
        if (notes[i] instanceof MutableControlChange) {
          MutableControlChange c = (MutableControlChange)notes[i];
          if (c.ctrlnum() == ctrlnum)
            return c.value();
        }
      }
      return defaultvalue;
    }
      

    public int volume() {
      return getFirstControlChange(7, volume);
    }

    public int panpot() {
      return getFirstControlChange(10, panpot);
    }

    public String name() {
      return name;
    }

    int division() {
      return division;
    }

    /** temporary */
    public PianoRoll.DataModel
      getPianoRollDataModel(int fromMeasure, int thruMeasure) {
      return new PianoRollDataModel(fromMeasure, thruMeasure);
    }

    class PianoRollDataModel implements PianoRoll.DataModel {
      private int fromMeasure, thruMeasure;
      PianoRollDataModel(int from, int thru) {
        fromMeasure = from;
        thruMeasure = thru;
      }
      public int getMeasureNum() {
        return thruMeasure - fromMeasure;
      }
      public int getBeatNum() {
        return 4;    // temporary
      }
      public int getFirstMeasure() {
        return fromMeasure;
      }
      public void setFirstMeasure(int measure) {
        thruMeasure = measure + (thruMeasure - fromMeasure);
        fromMeasure = measure;
      }
      public boolean isSelectable() {
        return false;
      }
      public boolean isEditable() {
        return false;
      }
      public void selectNote(int measure, double beat, int notenum) {
      }
      public boolean isSelected(int measure, double beat, int notenum) {
        return false;
      }
      public void drawData(PianoRoll pianoroll) {
        MutableNote[] notes = getNoteOnlyList();
        for (MutableNote note : notes) {
          long onset = note.onset();
          if (onset >= fromMeasure * getBeatNum() * division &&
              onset < thruMeasure * getBeatNum() * division) {
            int measure = (int)(onset / division / getBeatNum());
            double beat = (double)onset / division - measure * getBeatNum();
            double duration = (double)(note.offset() - onset) / division;
            pianoroll.drawNote(measure - fromMeasure, beat, duration,
                               note.notenum(), false, this);
          }
        }
      }
      public void shiftMeasure(int measure) {
        fromMeasure += measure;
        thruMeasure += measure;
      }
      public int tick2measure(long tick) {
        return (int)(tick / division / getBeatNum()) - fromMeasure;
      }
      public double tick2beat(long tick) {
        return (double)tick / division -
          tick / division / getBeatNum() * getBeatNum();
      }
    }
  }

  private int division;

  private Set<HeaderElement> headers = new TreeSet<HeaderElement>();
  private List<Part> parts = new ArrayList<Part>();
  private Set<MutableAnnotation> annotations = new TreeSet<MutableAnnotation>();

  private javax.sound.midi.Sequence seq = null;
  private Track[] tracks = null;

  public SCCDataSet(int division) {
    this.division = division;
  }

  public void addAnnotation(String type, long onset, long offset, String content) {
    annotations.add(new MutableAnnotation(onset, offset, type, content,
                                          division));
  }

  public void addBarline(long time, String details) {
    addAnnotation("barline", time, time, details);
  }

  public void addChord(long onset, long offset, String content) {
    addAnnotation("chord", onset, offset, content);
  }

  // Added: 2014/11/18, Author: tama
  public void addLyric(long onset, long offset, String content) {
    addAnnotation("lyric", onset, offset, content);
  }

  public void addCuePoint(long onset, long offset, String content) {
    addAnnotation("cue", onset, offset, content);
  }

  public void addMarker(long onset, long offset, String content) {
    addAnnotation("marker", onset, offset, content);
  }

  public void addHeaderElement(long time, java.lang.String name, java.lang.String content) {
    headers.add(new HeaderElement(time, name, content));
  }

  public void addHeaderElement(long time, String name, int content) {
    addHeaderElement(time, name, String.valueOf(content));
  }

  public void addHeaderElement(long time, String name, double content) {
    addHeaderElement(time, name, String.valueOf(content));
  }

  public Part addPart(int serial, int ch) {
    return addPart(serial, ch, (String)null);
  }

  @Deprecated
  public Part addPart(int serial, int ch, int pn, int vol) {
    return addPart(serial, ch, pn, vol, (String)null);
  }

  public Part addPart(int serial, int ch, String name) {
    return addPart(serial, ch, 0, 100, name);
  }

  @Deprecated
  public Part addPart(int serial, int ch, int pn, int vol, String name) {
    Part part = new Part(serial, (byte)ch, pn, vol, name, parts.size());
    parts.add(part);
    if (seq != null) {
      seq.createTrack();
      tracks = seq.getTracks();
    }
    return part;
  }

  public void addPart(int serial, int ch, Closure closure) {
    addPart(serial, ch, null, closure);
  }

  @Deprecated
  public void addPart(int serial, int ch, int pn, int vol, Closure closure) {
    addPart(serial, ch, pn, vol, null, closure);
  }

  public void addPart(int serial, int ch, String name, Closure closure) {
    addPart(serial, ch, 0, 100, name, closure);
  }

  @Deprecated
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
    return SCCUtils.getAnnotationListOf("chord", this);
//    return SCCUtils.getChordList(this);
  }

  public SCC.Annotation[] getBarlineList() {
    return SCCUtils.getAnnotationListOf("barline", this);
//    return SCCUtils.getBarlineList(this);
  }

  // Added: 2014/11/18, Author: tama
  public SCC.Annotation[] getLyricList() {
    return SCCUtils.getAnnotationListOf("lyric", this);
//    return SCCUtils.getLyricList(this);
  }

  public SCC.Annotation[] getMarkerList() {
    return SCCUtils.getAnnotationListOf("marker", this);
//    return SCCUtils.getMarkerList(this);
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

  // Modified: 2014/11/18 by tama
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
        //MutableMusicEvent[] notelist = p.getNoteOnlyList();        // modified on 2012.11.19
        MutableMusicEvent[] notelist = p.getNoteList();
        for (MutableMusicEvent n : notelist) {
          if (n instanceof MutableControlChange) {
            MutableControlChange cc = (MutableControlChange)n;
            newscc.addControlChange(cc.onset(div), cc.ctrlnum(), cc.value());
          } else if (n instanceof MutableProgramChange) {
            MutableProgramChange pc = (MutableProgramChange)n;
            newscc.addProgramChange(pc.onset(div), pc.value());
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
      // XXX:
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

  public MIDIXMLWrapper toMIDIXML() throws TransformerException {
    return toWrapper().toMIDIXML();
  }

  public InputStream getMIDIInputStream()
    throws IOException, TransformerException,ParserConfigurationException,
           SAXException {
    return toWrapper().getMIDIInputStream();
  }

  public javax.sound.midi.Sequence getMIDISequence()
    throws IOException, TransformerException, ParserConfigurationException,
           SAXException, InvalidMidiDataException {
    seq = MidiSystem.getSequence(getMIDIInputStream());
    tracks = seq.getTracks();
    return seq;
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

  public String toString() {
    try {
      return toWrapper().toString();
    } catch (TransformerException e) {
      throw new InvalidElementException(e.toString());
    }
  }

  public void repeat(long from, long thru, int times) {
    long len = thru - from;
    for (Part part : getPartList()) {
      for (MutableMusicEvent e : part.getNoteList()) {
        for (int k = 1; k <= times; k++) {
          if (e instanceof MutableNote) {
            part.addNoteElement(e.onset()+k*len, e.offset()+k*len,
                                ((MutableNote)e).notenum(),
                                ((MutableNote)e).velocity(),
                                ((MutableNote)e).offVelocity(),
                                e.getAttributes());
          } else if (e instanceof MutableControlChange) {
            part.addControlChange(e.onset()+k*len,
                                  ((MutableControlChange)e).ctrlnum(),
                                  ((MutableControlChange)e).value());
          } else if (e instanceof MutableProgramChange) {
            part.addProgramChange(e.onset()+k*len,
                                  ((MutableProgramChange)e).value());
          }
        }
      }
    }
  }
}

