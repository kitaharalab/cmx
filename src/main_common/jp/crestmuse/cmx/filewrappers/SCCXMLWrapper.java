package jp.crestmuse.cmx.filewrappers;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import groovy.lang.Closure;
import jp.crestmuse.cmx.elements.MutableNote;
import jp.crestmuse.cmx.handlers.SCCHandler;
import jp.crestmuse.cmx.handlers.SCCHandlerAdapter;
import jp.crestmuse.cmx.misc.KeySymbol;
import jp.crestmuse.cmx.misc.MIDIConst;
import jp.crestmuse.cmx.misc.MIDIEventList;
import jp.crestmuse.cmx.misc.ProgramBugException;

public class SCCXMLWrapper extends CMXFileWrapper
  implements SCC {
  /** newOutputData()に指定するトップタグ名．スペルミス防止．
   *
   * @author Hashida
   * @since 2007.7.18
   */
  public static final String TOP_TAG = "scc";
  
  private int division = 0;
  private Part[] partlist = null;
  private HeaderElement[] headlist = null;
  private Annotation[] annotations = null;
  //  private Annotation[] chords = null;
  //  private Annotation[] barlines = null;
  //  private ChordprogElement[] chordproglist = null;
  
  private boolean headerStarted = false;
  private boolean partStarted = false;
  private boolean annotationsStarted = false;
  //  private boolean chordprogStarted = false;

  private Map<NumberedNote,MusicXMLWrapper.Note> notemap =
    new HashMap<NumberedNote,MusicXMLWrapper.Note>();

  private Map<NumberedNote,Byte> nEqualNotes =
    new HashMap<NumberedNote,Byte>();

  // added 2008.02.21
  private Map<MutableNote,Note> notemap2 = new HashMap<MutableNote,Note>();

  private int currentPart = 0;

  protected void analyze() {
    try {
      final HeaderElement[] headers = getHeaderElementList();
      final int div = getDivision();
      processNotes(new SCCHandlerAdapter() {
          private double currentInSec;
          private long lastTempoChangeInTick;
          private double lastTempoChangeInSec;
          private double currentTempo;
          private long nextTempoChangeInTick;
          private double nextTempoChangeInSec;
          private double nextTempo;
          private int nextHeader;
          public void beginPart(Part part, SCCXMLWrapper w) {
            currentInSec = 0.0;
            lastTempoChangeInTick = 0;
            lastTempoChangeInSec = 0.0;
            currentTempo = 120;   // kari
            nextTempoChangeInTick = -1;
            nextHeader = 0;
            searchNextTempo();
          }
          public void processNote(Note note, SCCXMLWrapper w) {
            if (nextTempoChangeInTick >= 0
                && nextTempoChangeInTick < note.onset()) {
              lastTempoChangeInTick = nextTempoChangeInTick;
              lastTempoChangeInSec = nextTempoChangeInSec;
              currentTempo = nextTempo;
              searchNextTempo();
            }
            note.onsetInMSec = (long)(1000 * calcSec(note.onset()));
            if (nextTempoChangeInTick >= 0
                && nextTempoChangeInTick < note.offset()) {
              lastTempoChangeInTick = nextTempoChangeInTick;
              lastTempoChangeInSec = nextTempoChangeInSec;
              currentTempo = nextTempo;
              searchNextTempo();
            }
            note.offsetInMSec = (long)(1000 * calcSec(note.offset()));
          }
          private void searchNextTempo() {
            int i;
            for (i = nextHeader; i < headers.length; i++) {
//              System.err.println(headers[i].name() + headers[i].content());
              if (headers[i].name().equals("TEMPO")) {
                nextTempoChangeInTick = headers[i].time();
                nextTempoChangeInSec = calcSec(nextTempoChangeInTick);
                nextTempo = Double.parseDouble(headers[i].content());
                break;
              }
            }
            nextHeader = i + 1;
          }
          private double calcSec(long tick) {
            return (tick - lastTempoChangeInTick) * 60 / (div * currentTempo)
              + lastTempoChangeInSec;
          }
        });
    } catch (TransformerException e) {
      throw new XMLException(e);
    }
  }
  
  public int getDivision() {
    if (division == 0)
      division = NodeInterface.getAttributeInt
        (getDocument().getDocumentElement(), "division");
    return division;
  }
  
  @Deprecated
  public void setDivision(int div) {
    getDocument().getDocumentElement().setAttribute("division",
                                                    String.valueOf(div));
    division = div;
  }
  
  @Deprecated
  public void beginHeader() {
    checkElementAddition(!headerStarted);
    addChild("header");
    headerStarted = true;
  }
  
  @Deprecated
  public void addHeaderElement(long time, String name, String content) {
    checkElementAddition(headerStarted);
    addChild("meta");
    setAttribute("name", name);
    setAttribute("content", content);
    setAttribute("time", time);
    returnToParent();
  }
  
  @Deprecated
  public void addHeaderElement(long time, String name, double value) {
    addHeaderElement(time, name, String.valueOf(value));
  }
  
  @Deprecated
  public void addHeaderElement(long time, String name, int value) {
    addHeaderElement(time, name, String.valueOf(value));
  }
  
  @Deprecated
  public void endHeader() {
    checkElementAddition(headerStarted);
    returnToParent();
    headerStarted = false;
  }
  
  @Deprecated
  public void newPart(int serial, int ch, int pn, int vol) {
    newPart(serial, ch, pn, vol, null);
  }
  
  @Deprecated
  public void newPart(int serial, int ch, int pn, int vol, String name){
    checkElementAddition(!partStarted);
    addChild("part");
    setAttribute("serial", serial);
    setAttribute("ch", ch);
    setAttribute("pn", pn);
    setAttribute("vol", vol);
    if(name != null){
      setAttribute("name", name);
    }
    currentPart = serial;
    partStarted = true;
  }
  
  @Deprecated
  public void endPart() {
    checkElementAddition(partStarted);
    returnToParent();
    partStarted = false;
  }
  
  /*
    public void addNoteElement(int onset, int offset,
    int notenum, int velocity) {
    checkElementAddition(partStarted);
    addChild("note");
    addText(onset + " " + offset + " " + notenum + " " + velocity);
    //    addChild("onset");
    //    addText(String.valueOf(onset));
    //    addSibling("offset");
    //    addText(String.valueOf(offset));
    //    addSibling("notenum");
    //    addText(String.valueOf(notenum));
    //    addSibling("velocity");
    //    addText(String.valueOf(velocity));
    //    returnToParent();
    returnToParent();
    }
  */

  public void eachnote(Closure closure) throws TransformerException {
    SCCUtils.eachnote(this, closure);
  }

  /*
    public void eachnote(Closure closure) throws TransformerException {
    Part[] partlist = getPartList();
    for (Part part : partlist) {
    Note[] notelist = part.getNoteList();
    for (Note note : notelist) {
    closure.call(new Object[]{note});
    }
    }
    }
  */

  public void eachpart(Closure closure) throws TransformerException {
    SCCUtils.eachpart(this, closure);
  }

  /*
    public void eachpart(Closure closure) throws TransformerException {
    Part[] partlist = getPartList();
    for (Part part : partlist) {
    closure.call(new Object[]{part});
    }
    }
  */

  public void eachchord(Closure closure) throws TransformerException {
    SCCUtils.eachchord(this, closure);
  }
  
  public void eachbarline(Closure closure) throws TransformerException {
    SCCUtils.eachbarline(this, closure);
  }

  @Deprecated
  public void addPitchBend(long time, int value) {
    checkElementAddition(partStarted);
    addChild("pitch-bend");
    addText(time + " " + time + " " + value);
    returnToParent();
  }

  @Deprecated
  public void addPitchBend(long onset, long offset, int value) {
    checkElementAddition(partStarted);
    addChild("pitch-bend");
    addText(onset + " " + offset + " " + value);
    returnToParent();
  }

  void addProgramChange(long time, int value) {
    checkElementAddition(partStarted);
    addChild("program");
    addText(time + " " + time + " " + value);
    returnToParent();
  }

  @Deprecated
  public void addControlChange(long time, int ctrlnum, int value) {
    checkElementAddition(partStarted);
    addChild("control");
    addText(time + " " + time + " " + ctrlnum + " " + value);
    returnToParent();
  }

  @Deprecated
  public void addControlChange(long onset, long offset,
                               int ctrlnum, int value) {
    checkElementAddition(partStarted);
    addChild("control");
    addText(onset + " " + offset + " " + ctrlnum + " " + value);
    returnToParent();
  }

  @Deprecated
  public void addNoteElement(long onset, long offset,
                             int notenum, int velocity) {
    addNoteElement(onset, offset, notenum, velocity, velocity);
  }

  public void addNoteElement(long onset, long offset,
                             int notenum, int velocity,
                             MusicXMLWrapper.Note note) {
    checkElementAddition(partStarted);
    addChild("note");
    addText(onset + " " + offset + " " + notenum + " " + velocity);
    if (note != null)
      putNoteMap(onset, offset, notenum, velocity, velocity, getDivision(),
                 currentPart, note);
    //      notemap.put(new MutableNote(onset, offset, notenum, velocity,
    //                                  getDivision()), note);
    returnToParent();
  }

  @Deprecated
  public void addNoteElement(long onset, long offset, int notenum,
                             int velocity, int offVelocity) {
    addNoteElement(onset, offset, notenum, velocity, offVelocity, null);
  }
  
  @Deprecated
  public void addNoteElement(long onset, long offset, int notenum,
                             int velocity, int offVelocity,
                             Map<String,String> attr) {
    addNoteElement(onset, offset, notenum, velocity, offVelocity, attr, null);
  }
  
  @Deprecated
  public void addNoteElement(long onset, long offset, int notenum,
                             int velocity, int offVelocity,
                             Map<String,String> attr,
                             MusicXMLWrapper.Note note) {
    checkElementAddition(partStarted);
    addChild("note");
    if (attr != null) {
      for (Map.Entry<String,String> e : attr.entrySet()) {
        setAttribute(e.getKey(), e.getValue());
      }
    }
    if (note != null)
      setAttribute("voice", note.voice());
    addText(onset + " " + offset + " " + notenum + " " + velocity
            + " " + offVelocity);
    if (note != null)
      putNoteMap(onset, offset, notenum, velocity, offVelocity, getDivision(),
                 currentPart, note);
    //      notemap.put(new MutableNote(onset, offset, notenum, velocity,
    //                                  offVelocity), note);
    returnToParent();
  }
  
  @Deprecated
  public void addNoteElementWithWord(String word, long onset, long offset,
                                     int notenum, int velocity, int offVelocity){
    checkElementAddition(partStarted);
    addChild("note");
    setAttribute("word", word);
    addText(onset+" "+offset+" "+notenum+" "+ velocity+" "+offVelocity);
    returnToParent();
  }
  
  private void putNoteMap(long onset, long offset, int notenum, int velocity,
                          int offVelocity, int ticksPerBeat, int partid,
                          MusicXMLWrapper.Note note) {
    NumberedNote note2 = new NumberedNote(onset, offset, notenum, velocity,
                                          offVelocity, ticksPerBeat, partid);
    if (nEqualNotes.containsKey(note2)) {
      byte n = nEqualNotes.get(note2);
      NumberedNote note3 = new NumberedNote(note2);
      note3.number = ++n;
      notemap.put(note3, note);
      nEqualNotes.put(note2, n);
      setAttribute("number", n);
    } else {
      notemap.put(note2, note);
      nEqualNotes.put(note2, (byte)1);
    }
  }
  
  @Deprecated
  public void beginAnnotations() {
    addChild("annotations");
    annotationsStarted = true;
  }
  
  //  public void beginChordprog() {
  //    checkElementAddition(!chordprogStarted);
  //    addChild("chord-prog");
  //    chordprogStarted = true;
  //  }
  
  @Deprecated
  public void addAnnotation(String type, long onset, long offset,
                            String content) {
    if (content != null && content.trim().length() > 0) {
      checkElementAddition(annotationsStarted);
      addChildAndText(type, onset + " " + offset + " " +
                      (content==null ? "" : content));
    }
  }
  
  @Deprecated
  public void addChord(long onset, long offset, String content) {
    addAnnotation("chord", onset, offset, content);
  }
  
  // Added: 2014/11/18, Author: tama
  @Deprecated
  public void addLyric(long onset, long offset, String content) {
    addAnnotation("lyric", onset, offset, content);
  }

  // Added: 2014/11/18, Author: tama
  @Deprecated
  public void addCuePoint(long onset, long offset, String content) {
    addAnnotation("cue", onset, offset, content);
  }

  @Deprecated
  public void addMarker(long onset, long offset, String content) {
    addAnnotation("marker", onset, offset, content);
  }

  @Deprecated
  public void addBarline(long time, String details) {
    addAnnotation("barline", time, time, details);
  }

  //  public void addChordElement(int onset, int offset, String chord) {
  //    checkElementAddition(chordprogStarted);
  //    addChild("chord");
  //    addText(onset + " " + offset + " " + chord);
  //    returnToParent();
  //}

  @Deprecated
  public void endAnnotations() {
    checkElementAddition(annotationsStarted);
    returnToParent();
    annotationsStarted = false;
  }

  //  public void endChordprog() {
  //    checkElementAddition(chordprogStarted);
  //    returnToParent();
  //    chordprogStarted = false;
  //  }

  /** lyricイベントとcuePointに対応させるために、TextからContenを抜き出す処理を正規表現に変更 */
  public class Annotation extends NodeInterface implements SCC.Annotation {
    private long onset;
    private long offset;
    private String content;
    // Modified: 2014/11/18 by tama
    private Annotation(Node node) {
      super(node);
      if (type() != "lyric" && type() != "cuePoint") {
        String[] data = getText(node()).split("\\s");
        onset = Integer.parseInt(data[0]);
        offset = data.length >= 2 ? Integer.parseInt(data[1]) : onset;
//        content = data.length >= 3 ? getText(node()) : null;
        /* contentからonsetとoffsetを除去。marker以外で問題ないか未確認 */
        content = "";
        for (int i = 2; i < data.length; i++)
          content += data[i];
      } else {
        String[] data = getText(node()).split("\\s");
        onset = Integer.parseInt(data[0]);
        offset = data.length >= 2 ? Integer.parseInt(data[1]) : onset;
        String nodeCnt = getText(node());
        Pattern contentPtn = Pattern.compile("\\s.*$");
        Matcher con = contentPtn.matcher(nodeCnt);
        if (con.find() && data.length >= 3) {
          Pattern rep = Pattern.compile("\\s\\d*\\s");
          Matcher repM = rep.matcher(con.group(0));
          content = repM.replaceFirst("");
        } else {
          content = "";
        }
      }


//			onset = Integer.parseInt(data[0]);
//			offset = data.length >= 2 ? Integer.parseInt(data[1]) : onset;
////			content = data.length >= 3 ? data[2] : null;

//			String data = getText(node());
//			String tmp;
//			for (int i = 0; i < data.length(); i++) {
//				tmp +=
//			}

      /*
       * Modified: 2014/11/18 by tama
       * Lyricに対応
       */
//			if (data.length >= 3) {
//				System.out.println(data.length);
//				for (int i = 2; i < data.length; i++) {
//					System.out.println(data[i]);
//					if (data[i] == "") {
//						content += " ";
//					} else {
//                        content += data[i];
//					}
//				}
//			} else {
//				System.out.println("ffdasfdsa");
//				content = null;
//			}
    }
    protected String getSupportedNodeName() {
      return "chord|barline|lyrics|cuePoint|marker";
    }
    public final long onset(){ return onset; }
    public final long offset(){ return offset; }
    public final String content(){ return content; }

    public final long onset(int ticksPerBeat) {
      if (ticksPerBeat == getDivision())
        return onset;
      else
        return onset * ticksPerBeat / getDivision();
    }

    public final long offset(int ticksPerBeat) {
      if (ticksPerBeat == getDivision())
        return offset;
      else
        return offset * ticksPerBeat / getDivision();
    }

    public final String type() {
      return getNodeName();
    }

  }

  public Annotation[] getAnnotationList() {
    if (annotations != null) {
      return annotations;
    } else {
      Node ann = selectSingleNode("/scc/annotations");
      if (ann == null) {
        annotations = new Annotation[0];
      } else {
        NodeList nl = ann.getChildNodes();
        int size = nl.getLength();
        annotations = new Annotation[size];
        for (int i = 0; i < size; i++)
          annotations[i] = new Annotation(nl.item(i));
      }
      return annotations;
    }
  }

  public SCC.Annotation[] getChordList() {
    return SCCUtils.getAnnotationListOf("chord", this);
//    return SCCUtils.getChordList(this);
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

  /*
    public Annotation[] getChordList() {
    if (chords != null) {
    return chords;
    } else {
    Annotation[] ann = getAnnotationList();
    int n = 0;
    for (int i = 0; i < ann.length; i++)
    if (ann[i].getNodeName().equals("chord"))
    n++;
    chords = new Annotation[n];
    int k = 0;
    for (int i = 0; i < ann.length; i++)
    if (ann[i].getNodeName().equals("chord")) {
    chords[k] = ann[i];
    k++;
    }
    return chords;
    }
    }
  */


  /*
    public Annotation[] getChordList() {
    if (chords != null) {
    return chords;
    } else {
    Node ann = selectSingleNode("/scc/annotations");
    if (ann == null) return null;
    NodeList nl = selectNodeList(ann, "chord");
    int size = nl.getLength();
    chords = new Annotation[size];
    for (int i = 0; i < size; i++)
    chords[i] = new Annotation(nl.item(i));
    return chords;
    }
    }
  */

  public SCC.Annotation[] getBarlineList() {
    return SCCUtils.getAnnotationListOf("barline", this);
//    return SCCUtils.getBarlineList(this);
  }

  /*

    public Annotation[] getBarlineList() {
    if (chords != null) {
    return chords;
    } else {
    Annotation[] ann = getAnnotationList();
    int n = 0;
    for (int i = 0; i < ann.length; i++)
    if (ann[i].getNodeName().equals("barline"))
    n++;
    barlines = new Annotation[n];
    int k = 0;
    for (int i = 0; i < ann.length; i++)
    if (ann[i].getNodeName().equals("barline")) {
    barlines[k] = ann[i];
    k++;
    }
    return barlines;
    }
    }
  */


  /*
    public Annotation[] getBarlineList() {
    if (barlines != null) {
    return barlines;
    } else {
    Node ann = selectSingleNode("/scc/annotations");
    if (ann == null) return null;
    NodeList nl = selectNodeList(ann, "barline");
    int size = nl.getLength();
    barlines = new Annotation[size];
    for (int i = 0; i < size; i++)
    barlines[i] = new Annotation(nl.item(i));
    return barlines;
    }
    }
  */

  /*
    public Annotation[] getChordList() {
    if(chordproglist == null){
    Node chordprognode = selectSingleNode("/scc/chord-prog");
    if(chordprognode == null) return null;
    else{
    NodeList chordlist = selectNodeList(chordprognode, "chord");
    int size = chordlist.getLength();
    chordproglist = new ChordprogElement[size];
    for(int i=0; i<size; i++) chordproglist[i] = new ChordprogElement(chordlist.item(i));
    }
    }
    return chordproglist;
    }
  */

  public class HeaderElement extends NodeInterface
    implements SCC.HeaderElement {
    private long timestamp;
    private String name;
    private String content;
    private HeaderElement(Node node) {
      super(node);
      timestamp = getAttributeLong(node, "time");
      name = getAttribute(node, "name");
      content = getAttribute(node, "content");
    }
    protected String getSupportedNodeName() {
      return "meta";
    }
    public long time() {
      return timestamp;
    }
    public String name() {
      return name;
    }
    public String content() {
      return content;
    }
    public boolean equals(SCC.HeaderElement e) {
      return timestamp == e.time() && name.equals(e.name()) &&
        content.equals(e.content());
    }
    public int compareTo(SCC.HeaderElement e) {
      if (timestamp != e.time())
        return (int)(timestamp - e.time());
      else if (!name.equals(e.name()))
        return name.compareTo(e.name());
      else if (!content.equals(e.content()))
        return content.compareTo(e.content());
      else
        return 0;
    }
  }

  public HeaderElement[] getHeaderElementList() {
    if (headlist == null) {
      Node headnode = selectSingleNode("/scc/header");
      if (headnode == null) {
        headlist = new HeaderElement[0];
      } else {
        NodeList metalist = selectNodeList(headnode, "meta");
        int size = metalist.getLength();
        headlist = new HeaderElement[size];
        for (int i = 0; i < size; i++)
          headlist[i] = new HeaderElement(metalist.item(i));
      }
    }
    return headlist;
  }

  /*
    public void processNotes(CommonNoteHandler h) throws TransformerException {
    Part[] partlist = getPartList();
    for (Part part : partlist) {
    String id = String.valueOf(part.serial());
    h.beginPart(id, this);
    Note[] notelist = part.getNoteOnlyList();
    for (Note note : notelist)
    h.processNote(note, this);
    h.endPart(id, this);
    }
    }
  */

  public void processNotes(SCCHandler h) throws TransformerException {
    HeaderElement[] headlist = getHeaderElementList();
    if (headlist != null) {
      h.beginHeader(this);
      for (HeaderElement head : headlist)
        h.processHeaderElement(head.time(), head.name(), head.content(), this);
      h.endHeader(this);
    }
    Part[] partlist = getPartList();
    for (Part part : partlist) {
      h.beginPart(part, this);
      Note[] notelist = part.getNoteList();
      for (Note note : notelist)
        h.processNote(note, this);
      h.endPart(part, this);
    }
    //    int size = partlist.getLength();
    //    for (int i = 0; i < size; i++) {
    //      Part part = new Part(partlist.item(i));
    //      h.beginPart(part, this);
    //      NodeList notelist = part.getNoteList();
    //      int size2 = notelist.getLength();
    //      for (int j = 0; j < size2; j++) {
    //        Note note = new Note(notelist.item(j));
    //        h.processNote(note, part, this);
    //      }
    //      h.endPart(part, this);
    //    }
  }

  public void processSortedNotes(SCCHandler h) throws TransformerException {
    Node headnode = selectSingleNode("/scc/header");
    if (headnode != null) {
      h.beginHeader(this);
      NodeList metalist = selectNodeList(headnode, "meta");
      int size = metalist.getLength();
      for (int i = 0; i < size; i++) {
        Node node = metalist.item(i);
        String name = NodeInterface.getAttribute(node, "name");
        String content = NodeInterface.getAttribute(node, "content");
        long timestamp = NodeInterface.getAttributeLong(node, "time");
        h.processHeaderElement(timestamp, name, content, this);
      }
      h.endHeader(this);
    }
    Part[] partlist = getPartList();
    for (Part part : partlist) {
      h.beginPart(part, this);
      //      SortedSet<Note> notelist = part.getSortedNoteSet();
      //      for (Note note : notelist)
      Note[] notelist = part.getSortedNoteList();
      for (Note note : notelist)
        h.processNote(note, this);
      h.endPart(part, this);
    }
  }
  /*
    public SCCXMLWrapper getSortedSCCXML(int range) throws
    SAXException, ParserConfigurationException, TransformerException,
    IOException {
    SCCXMLWrapper result =
    (SCCXMLWrapper)CMXFileWrapper.createDocument(TOP_TAG);
    result.setDivision(getDivision());
    Part[] parts = getPartList();
    for (Part part : parts) {
    result.newPart(part.serial(), part.channel(), part.prognum(),
    part.volume());
    SortedSet<Note> notes = part.getSortedNoteSet(range);
    for (Note note : notes)
    if (note instanceof ControlChange)
    result.addControlChange(note.onset(), note.offset(),
    note.notenum(), note.velocity());
    else
    result.addNoteElement(note.onset(), note.offset(),
    note.notenum(), note.velocity(),
    note.getMusicXMLWrapperNote());
    result.endPart();
    }
    result.finalizeDocument();
    return result;
    }
  */


  public Part[] getPartList() throws TransformerException {
    if (partlist == null) {
      NodeList nl = selectNodeList("/scc/part");
      int size = nl.getLength();
      partlist = new Part[size];
      for (int i = 0; i < size; i++) {
        partlist[i] = new Part(nl.item(i));
        partlist[i].xpath = "/scc/part[" + i + "]";
      }
    }
    return partlist;
  }

  public Part getPartWithSerial(int serial) throws TransformerException {
    Part[] parts = getPartList();
    for (Part part : parts) {
      if (part.serial() == serial)
        return part;
    }
    return null;
  }

  public Part[] getPartsWithChannel(int ch) throws TransformerException {
    Part[] parts = getPartList();
    List<Part> l = new ArrayList<Part>();
    for (Part part : parts) {
      if (part.channel() == ch) {
        l.add(part);
      }
    }
    return l.toArray(new Part[l.size()]);
  }
  
  public Part getFirstPartWithChannel(int ch) throws TransformerException {
    Part[] parts = getPartList();
    for (Part part : parts) {
      if (part.channel() == ch) 
        return part;
    }
    return null;
  }


  //    public int getNumOfParts() throws TransformerException {
  //	return selectNodeList("/scc/part").getLength();
  //    }


  /*
    public List<SimpleNoteList> getPartwiseNoteList
    (final int ticksPerBeat) throws TransformerException {
    final List<SimpleNoteList> l = new ArrayList<SimpleNoteList>();
    processNotes(new SCCHandler() {
    private SimpleNoteList nl;
    public void beginHeader(SCCXMLWrapper w) {}
    public void endHeader(SCCXMLWrapper w) {}
    public void processHeaderElement(int timestamp, String name,
    String content, SCCXMLWrapper w) {}
    public void beginPart(Part part, SCCXMLWrapper wrapper) {
    nl = new SimpleNoteList(part.serial(), ticksPerBeat);
    }
    public void endPart(Part part, SCCXMLWrapper wrapper) {
    l.add(nl);
    }
    public void processNote(Note note, SCCXMLWrapper wrapper) {
    if (!(note instanceof ControlChange))
    nl.add(note);
    }
    });
    return l;
    }
  */

  public InputStream getMIDIInputStream() throws IOException,
    TransformerException,ParserConfigurationException,SAXException {
    return toMIDIXML().getMIDIInputStream();
  }

  /** このSCCXMLドキュメントをMIDIXML形式に変換して、MIDIXMLWrapperオブジェクトを返します。
   * ただし、XFフォーマットで拡張された歌詞イベントには対応していません。
   */
  public MIDIXMLWrapper toMIDIXML() {
    try {
      MIDIXMLWrapper dest =
        (MIDIXMLWrapper)CMXFileWrapper.createDocument(MIDIXMLWrapper.TOP_TAG);
      toMIDIXML(dest);
      return dest;
    } catch (TransformerException e) {
      throw new XMLException(e);
    } catch (ParserConfigurationException e) {
      throw new XMLException(e);
    } catch (SAXException e) {
      throw new XMLException(e);
    } catch (InvalidFileTypeException e) {
      throw new ProgramBugException(e.toString());
    }
  }

  /** @deprecated
      このSCCXMLドキュメントをMIDIXML形式に変換し、指定されたMIDIXMLWrapperオブジェクトに要素を追加します。
      ただし、XFフォーマットで拡張された歌詞イベントには対応していません。
  */
  public void toMIDIXML(final MIDIXMLWrapper dest)
    throws ParserConfigurationException,TransformerException,
    SAXException {
    dest.addElementsFirstForFormat1(getPartList().length + 1, getDivision());
    processNotes(new SCCHandler() {
        private int currentTrack = 1;
        private long currentTime = 0;
        public final void beginHeader(SCCXMLWrapper w) {
          dest.newTrack(currentTrack);
        }
        public final void endHeader(SCCXMLWrapper w) {
          dest.endTrack();
          currentTrack++;
        }
        public final void processHeaderElement(long timestamp, String name,
                                               String content, SCCXMLWrapper w) {
          if (name.equals("TEMPO"))
            dest.addMetaEvent("SetTempo", (int)(timestamp - currentTime),
                              (int)(1000*1000*60/Double.parseDouble(content)));
          else if (name.equals("KEY")) {
            KeySymbol key = KeySymbol.parse(content);
            int mode = key.mode().equals(KeySymbol.Mode.MIN) ? 1 : 0;
            dest.addMetaEvent("KeySignature", (int)(timestamp - currentTime),
                              key.toInteger(), mode);

            //            String[] data = content.trim().split(" ");
            //            dest.addMetaEvent("KeySignature", timestamp - currentTime,
            //                              //                              Integer.parseInt(data[0]),
            //                              data[0].startsWith("+") ?
            //                              Integer.parseInt(data[0].substring(1)) :
            //                              Integer.parseInt(data[0]),
            //                              data[1].toLowerCase().startsWith("min") ?
            //                              1 : 0);
          }
          currentTime = timestamp;
        }
        public final void beginPart(Part part, SCCXMLWrapper w) {
          if (part.channel() < 1 || part.channel() > 16)
            throw new InvalidElementException
              ("Channel should be between 1 and 16.");
          dest.newTrack(currentTrack);
          MIDIEventList el = part.toMIDIEventList();
          dest.addMIDIChannelMessages(el);
        }
        public final void endPart(Part part, SCCXMLWrapper w) {
          dest.endTrack();
          currentTrack++;
        }
        public final void processNote(Note note, SCCXMLWrapper w) {
        }
      });

/*
    Annotation[] annlist = getAnnotationList();
    if (annlist != null) {
      dest.newTrack(3);
      for (Annotation a : annlist ) {
//				dest.addChildAndText(a.type(), a.content());
        dest.addMetaEvent(a.type(), a.onset);
      }
      dest.endTrack();
    }
*/

    try {
      dest.finalizeDocument();
    } catch (IOException e) {
      throw new ProgramBugException(e.toString());
    }
  }

  @Deprecated
  public SCCXMLWrapper replaceVelocity(List<List<Byte>> vellist,
                                       boolean sorted)
    throws TransformerException, InvalidFileTypeException,
    ParserConfigurationException, SAXException, IOException   {
    final SCCXMLWrapper newscc =
      (SCCXMLWrapper)CMXFileWrapper.createDocument(TOP_TAG);
    newscc.setDivision(getDivision());
    final Iterator<List<Byte>> it1 = vellist.iterator();
    SCCHandler h = new SCCHandler() {
        Iterator<Byte> it2;
        public final void beginHeader(SCCXMLWrapper w) {
          newscc.beginHeader();
        }
        public final void endHeader(SCCXMLWrapper w) {
          newscc.endHeader();
        }
        public final void processHeaderElement(long timestamp, String name,
                                               String content, SCCXMLWrapper w) {
          newscc.addHeaderElement(timestamp, name, content);
        }
        public final void beginPart(Part part, SCCXMLWrapper w) {
          newscc.newPart(part.serial(), part.channel(),
                         part.prognum(), part.volume());
          it2 = it1.next().iterator();
        }
        public final void endPart(Part part, SCCXMLWrapper w) {
          newscc.endPart();
        }
        public final void processNote(Note note, SCCXMLWrapper w) {
          if (note instanceof ControlChange) {
            ControlChange cc = (ControlChange)note;
            newscc.addControlChange(cc.onset(), cc.offset(),
                                    cc.ctrlnum(), cc.value());
          } else {
            byte velocity = it2.next();
            newscc.addNoteElement(note.onset(), note.offset(),
                                  note.notenum(), velocity, velocity,
                                  note.getAttributes(),
                                  note.getMusicXMLWrapperNote());
          }
        }
      };
    if (sorted)
      processSortedNotes(h);
    else
      processNotes(h);
    newscc.finalizeDocument();
    return newscc;
  }

  /** @Deprecated */
  public SCCXMLWrapper changeVelocity(List<List<Byte>> diff, boolean sorted)
    throws TransformerException, InvalidFileTypeException,
    ParserConfigurationException, SAXException, IOException {
    final SCCXMLWrapper newscc =
      (SCCXMLWrapper)CMXFileWrapper.createDocument(TOP_TAG);
    newscc.setDivision(getDivision());
    final Iterator<List<Byte>> it1 = diff.iterator();
    SCCHandler h = new SCCHandler() {
        Iterator<Byte> it2;
        public final void beginHeader(SCCXMLWrapper w) {
          newscc.beginHeader();
        }
        public final void endHeader(SCCXMLWrapper w) {
          newscc.endHeader();
        }
        public final void processHeaderElement(long timestamp, String name,
                                               String content, SCCXMLWrapper w) {
          newscc.addHeaderElement(timestamp, name, content);
        }
        public final void beginPart(Part part, SCCXMLWrapper w) {
          newscc.newPart(part.serial(), part.channel(),
                         part.prognum(), part.volume());
          it2 = it1.next().iterator();
        }
        public final void endPart(Part part, SCCXMLWrapper w) {
          newscc.endPart();
        }
        public final void processNote(Note note, SCCXMLWrapper w) {
          if (note instanceof ControlChange) {
            ControlChange cc = (ControlChange)note;
            newscc.addControlChange(cc.onset(), cc.offset(),
                                    cc.ctrlnum(), cc.value());
          } else {
            byte diffvel = it2.next();
            int vel = Math.max(Math.min(note.velocity + diffvel, 127), 0);
            newscc.addNoteElement(note.onset(), note.offset(),
                                  note.notenum(), vel, vel, note.getAttributes(),
                                  note.getMusicXMLWrapperNote());
          }
        }
      };
    if (sorted)
      processSortedNotes(h);
    else
      processNotes(h);
    newscc.finalizeDocument();
    return newscc;
  }

  public Document getXMLDocument() {
    return getDocument();
  }

  public class Part extends NodeInterface implements SCC.Part {
    private Note[] notelist = null;
    private List<Note> noteonlylist = null;
    private String xpath;

    private Part(Node node) {
      super(node);
    }

    @Override
    protected final String getSupportedNodeName() {
      return "part";
    }

    public void eachnote(Closure closure) throws TransformerException {
      SCCUtils.eachnote(this, closure);
    }


    /*
      public void eachnote(Closure closure) throws TransformerException {
      Note[] notelist = getNoteList();
      for (Note note : notelist) {
      closure.call(new Object[]{note});
      }
      }
    */

    //    public NodeList getNoteList() {
    //      return selectNodeList(node(), "note");
    //    }

    public Note[] getNoteList() {
      if (notelist == null) {
        //        NodeList nl = selectNodeList(node(), "note");
        NodeList nl = getChildNodes();
        int size = nl.getLength();
        notelist = new Note[size];
        noteonlylist = new ArrayList<Note>();
        int iNote = 0, iCC = 0, iPB = 0, iPC = 0;
        for (int i = 0; i < size; i++) {
          Node node = nl.item(i);
          if (node.getNodeName().equals("note")) {
            notelist[i] = new Note(node, this);
            noteonlylist.add(notelist[i]);
            notelist[i].xpath = getXPathExpression() + "/note[" + iNote + "]";
            iNote++;
          } else if (node.getNodeName().equals("control")) {
            notelist[i] = new ControlChange(node, this);
            notelist[i].xpath = getXPathExpression() + "/control[" + iCC + "]";
            iCC++;
          } else if (node.getNodeName().equals("pitch-bend")) {
            notelist[i] = new PitchBend(node, this);
            notelist[i].xpath = getXPathExpression()+"/pitch-bend["+iPB+"]";
            iPB++;
          } else if (node.getNodeName().equals("program")) {
            notelist[i] = new ProgramChange(node, this);
            notelist[i].xpath = getXPathExpression() + "/program[" + iPC + "]";
            iPC++;
          } else {
            throw new IllegalArgumentException("Unsupported Node Name: " + node.getNodeName());
          }
        }
      }
      return notelist;
    }

    public Note[] getNoteOnlyList() {
      if (noteonlylist == null)
        getNoteList();
      return noteonlylist.toArray(new Note[noteonlylist.size()]);
    }

    public Note[] getSortedNoteList() {
      Note[] l = (Note[])(getNoteList().clone());
      Arrays.sort(l, new Comparator<Note>() {
          public int compare(Note n1, Note n2) {
            return
              n1.onset() == n2.onset() ?
              (n1.offset() == n2.offset() ?
               (n1.notenum() == n2.notenum() ?
                n1.velocity() - n2.velocity()
                : n1.notenum() - n2.notenum()
                ) : (int)(n1.offset() - n2.offset())
               ) : (int)(n1.onset() - n2.onset());
          }
        });
      return l;
    }

    public Note[] getSortedNoteOnlyList() {
      Note[] l = (Note[])(getNoteOnlyList().clone());
      Arrays.sort(l, new Comparator<Note>() {
          public int compare(Note n1, Note n2) {
            return
              n1.onset() == n2.onset() ?
              (n1.offset() == n2.offset() ?
               (n1.notenum() == n2.notenum() ?
                n1.velocity() - n2.velocity()
                : n1.notenum() - n2.notenum()
                ) : (int)(n1.offset() - n2.offset())
               ) : (int)(n1.onset() - n2.onset());
          }
        });
      return l;
    }

    public Note[] getSortedNoteList(final int range) {
      Note[] l = (Note[])(getNoteList().clone());
      Arrays.sort(l, new Comparator<Note>() {
          public int compare(Note n1, Note n2) {
            return
              Math.abs(n1.onset() - n2.onset()) < range ?
              (n1.notenum() == n2.notenum() ?
               n1.velocity() - n2.velocity()
               : n1.notenum() - n2.notenum()
               ) : (int)(n1.onset() - n2.onset());
          }
        });
      return l;
    }

    public Note[] getSortedNoteOnlyList(final int range) {
      Note[] l = (Note[])(getNoteOnlyList().clone());
      Arrays.sort(l, new Comparator<Note>() {
          public int compare(Note n1, Note n2) {
            return
              Math.abs(n1.onset() - n2.onset()) < range ?
              (n1.notenum() == n2.notenum() ?
               n1.velocity() - n2.velocity()
               : n1.notenum() - n2.notenum()
               ) : (int)(n1.onset() - n2.onset());
          }
        });
      return l;
    }

    /*
      public SortedSet<Note> getSortedNoteSet() {
      SortedSet<Note> s
      = new TreeSet<Note>(new Comparator<Note>() {
      public int compare(Note n1, Note n2) {
      return
      n1.onset() == n2.onset() ?
      (n1.offset() == n2.offset() ?
      (n1.notenum() == n2.notenum() ?
      n1.velocity() - n2.velocity()
      : n1.notenum() - n2.notenum()
      ) : n1.offset() - n2.offset()
      ) : n1.onset() - n2.onset();
      }
      });
      Note[] notelist = getNoteList();
      for (Note note : notelist)
      s.add(note);
//      int size = nl.getLength();
//      for (int i = 0; i < size; i++)
//        s.add(new Note(nl.item(i)));
return s;
}
    */
    /*
      public SortedSet<Note> getSortedNoteSet(final int range) {
      SortedSet<Note> s
      = new TreeSet<Note>(new Comparator<Note>() {
      public int compare(Note n1, Note n2) {
      return
      Math.abs(n1.onset() - n2.onset()) < range ?
      (n1.notenum() == n2.notenum() ?
      n1.velocity() - n2.velocity()
      : n1.notenum() - n2.notenum()
      ) : n1.onset() - n2.onset();
      }
      });
      Note[] notelist = getNoteList();
      for (Note note : notelist)
      s.add(note);
//      System.err.println(s);
return s;
}
    */

    public MIDIEventList toMIDIEventList() {
      byte ch = channel();
      MIDIEventList el = new MIDIEventList();
      Note[] notelist = getNoteList();
//      el.addEvent(0, MIDIConst.PROGRAM_CHANGE, ch, prognum(), 0);
//      el.addEvent(0, MIDIConst.CONTROL_CHANGE, ch, 7, volume());
      for (Note note : notelist) {
        if (note instanceof ControlChange) {
          el.addEvent(note.onset(), MIDIConst.CONTROL_CHANGE, ch,
                      ((ControlChange)note).ctrlnum(),
                      ((ControlChange)note).value());
        } else if (note instanceof PitchBend) {
          el.addEvent(note.onset(), MIDIConst.PITCH_BEND_CHANGE, ch,
                      ((PitchBend)note).value(), 0);
        } else if (note instanceof ProgramChange) {
          el.addEvent(note.onset(), MIDIConst.PROGRAM_CHANGE, ch, 
                      ((ProgramChange)note).value(), 0);
        } else {
          el.addEvent(note.onset(), MIDIConst.NOTE_ON, ch,
                      note.notenum(), note.velocity());
          el.addEvent(note.offset(), MIDIConst.NOTE_OFF, ch,
                      note.notenum(), note.velocity());
        }
      }
      return el;
    }

    public final int serial() {
      return getAttributeInt(node(), "serial");
    }

    public final byte channel() {
      return Byte.parseByte(getAttribute(node(), "ch"));
    }

    public final int prognum() {
      Note[] notes = getNoteList();
      for (int i = 0; i < notes.length; i++) {
        if (notes[i] instanceof ProgramChange) {
          return ((ProgramChange)notes[i]).value();
        }
      }
      return getAttributeInt(node(), "pn");
//      return getAttributeInt(node(), "pn");
    }

    private int getFirstControlChange(int ctrlnum, int defaultvalue) {
      Note[] notes = getNoteList();
      for (int i = 0; i < notes.length; i++) {
        if (notes[i] instanceof ControlChange) {
          ControlChange c = (ControlChange)notes[i];
          if (c.ctrlnum() == ctrlnum)
            return c.value();
        }
      }
      return defaultvalue;
    }

    public final int volume() {
      return getFirstControlChange(7, getAttributeInt(node(), "vol"));
//      return getAttributeInt(node(), "vol");
    }

    public final int panpot() {
      return getFirstControlChange(10, getAttributeInt(node(), "pan"));
//      return getAttributeInt(node(), "pan");
    }

    public final String name() {
      return getAttribute(node(), "name");
    }

    public final String getXPathExpression() {
      return xpath;
    }
  }

  public class Note extends NodeInterface implements SCC.Note {
    private Part part;
    private long onset, offset;
    private int notenum, velocity, offVelocity;
    private long onsetInMSec, offsetInMSec;
    private String xpath;

    private Note(Node node, Part part) {
      super(node);
      this.part = part;
      String[] data = getText(node()).split("\\s");
      onset = Integer.parseInt(data[0]);
      offset = Integer.parseInt(data[1]);
      notenum = Integer.parseInt(data[2]);
      if (data.length >= 4)
        velocity = Integer.parseInt(data[3]);
      if (data.length >= 5)
        offVelocity = Integer.parseInt(data[4]);
      else
        offVelocity = velocity;
      //	    onset = getTextInt(getChildByTagName("onset"));
      //	    offset = getTextInt(getChildByTagName("offset"));
      //	    notenum = getTextInt(getChildByTagName("notenum"));
      //	    velocity = getTextInt(getChildByTagName("velocity"));
    }

    @Override
    protected String getSupportedNodeName() {
      return "note|control|program|pitch-bend";
    }

    public final long onset() {
      return onset;
    }

    public final long onset(int ticksPerBeat) {
      if (ticksPerBeat == getDivision())
        return onset;
      else
        return onset * ticksPerBeat / getDivision();
    }

    public final long offset() {
      return offset;
    }

    public final long offset(int ticksPerBeat) {
      if (ticksPerBeat == getDivision())
        return offset;
      else
        return offset * ticksPerBeat / getDivision();
    }

    public final long duration(int ticksPerBeat) {
      return offset(ticksPerBeat) - onset(ticksPerBeat);
    }

    public final int notenum() {
      return notenum;
    }

    public final int velocity() {
      return velocity;
    }

    public final int offVelocity() {
      return offVelocity;
    }

    public final Part part() {
      return part;
    }

    //    /** Obsolete. use onsetInMilliSec() instead. */
    //    public final int onsetInMSec() {
    //      return onsetInMSec;
    //    }

    public final long onsetInMilliSec() {
      return onsetInMSec;
    }

    //    /** Obsolete. use offsetInMilliSec() instead. */
    //    public final int offsetInMSec() {
    //      return offsetInMSec;
    //}

    public final long offsetInMilliSec() {
      return offsetInMSec;
    }

    public final long durationInMilliSec() {
      return offsetInMSec - onsetInMSec;
    }

    public MusicXMLWrapper.Note getMusicXMLWrapperNote() {
      NumberedNote note = new NumberedNote(onset, offset, notenum, velocity,
                                           offVelocity, getDivision(),
                                           part.serial());
      if (hasAttribute("number"))
        note.number = Byte.parseByte(getAttribute("number"));
      return notemap.get(note);
      //      return notemap.get(new MutableNote(onset, offset, notenum, velocity,
      //                                         getDivision()));
    }

    public boolean isEqualNoteTo(Note another) {
      return onset == another.onset && offset == another.offset &&
        notenum == another.notenum && velocity == another.velocity &&
        offVelocity == another.offVelocity;
    }

    public String toString() {
      return "Note[" + onset + ", " + offset + ", " + notenum + ", "
        + velocity + ", " + offVelocity + "]";
    }

    public String getXPathExpression() {
      return xpath;
    }

    public String word() {
      if (hasAttribute("word"))
        return getAttribute("word");
      else
        return null;
    }

  }

  public class ProgramChange extends Note {
    private ProgramChange(Node node, Part part) {
      super(node, part);
    }
    protected String getSupportedNodeName() {
      return "program";
    }
    public final int value() {
      return notenum();
    }
  }

  public class ControlChange extends Note {
    private ControlChange(Node node, Part part) {
      super(node, part);
    }
    protected String getSuppotedNodeName() {
      return "control";
    }
    public final int ctrlnum() {
      return notenum();
    }
    public final int value() {
      return velocity();
    }
  }

  public class PitchBend extends Note {
    private PitchBend(Node node, Part part) {
      super(node, part);
    }
    protected String getSupportedNodeName() {
      return "pitch-bend";
    }
    public final int value() {
      return notenum();
    }
  }


  private class NumberedNote extends MutableNote {
    private int partid;
    private byte number = 1;
    private NumberedNote(long onset, long offset, int notenum, int velocity,
                         int ticksPerBeat, int partid) {
      this(onset, offset, notenum, velocity, velocity, ticksPerBeat, partid);
    }
    private NumberedNote(long onset, long offset, int notenum, int velocity,
                         int offVelocity, int ticksPerBeat, int partid) {
      super(onset, offset, notenum, velocity, offVelocity, ticksPerBeat);
      this.partid = partid;
    }
    private NumberedNote(NumberedNote note) {
      this(note.onset(), note.offset(), note.notenum(), note.velocity(),
           note.offVelocity(), note.ticksPerBeat(), note.partid);
      number = note.number;
    }
    public boolean equals(Object o) {
      if (o instanceof NumberedNote) {
        NumberedNote another = (NumberedNote)o;
        return super.equals(another) && number == another.number
          && partid == another.partid;
      } else {
        return false;
      }
    }

  }

  class EasyChord{
    public long onset;
    public long offset;
    public String chord;

    public EasyChord(long onset, long offset, String chord){
      this.onset = onset;
      this.offset = offset;
      this.chord = chord;
    }
  }


  /*
    static class EasyNote {
    int onset, offset, notenum, velocity, offVelocity;
    EasyNote(int onset, int offset, int notenum, int velocity) {
    this(onset, offset, notenum, velocity, velocity);
    }
    EasyNote(int onset, int offset, int notenum, int velocity,
    int offVelocity) {
    this.onset = onset;
    this.offset = offset;
    this.notenum = notenum;
    this.velocity = velocity;
    this.offVelocity = offVelocity;
    }
    public boolean equals(Object o) {
    EasyNote en = (EasyNote)o;
    return ((onset == en.onset) && (offset == en.offset) &&
    (notenum == en.notenum) && (velocity == en.velocity) &&
    (offVelocity == offVelocity));
    }
    public int hashCode() {
    return onset + offset + notenum + velocity + offVelocity;
    }
    }
  */

  /*
    public static void main(String[] args){
    try {
    DeviationInstanceWrapper dev =
    (DeviationInstanceWrapper)CMXFileWrapper.readfile("deviation.xml");
    SCCXMLWrapper scc = dev.toSCCXML(480);
    //scc.write(System.out);
    scc.beginHeader();
    scc.addHeaderElement(10, "hoge", "foo");
    scc.endHeader();
    scc.write(System.out);
    } catch (Exception e) {
    // TODO 自動生成された catch ブロック
    e.printStackTrace();
    }
    }

  */

  public SCCXMLWrapper toWrapper() {
    return this;
  }

  public SCCDataSet toDataSet() throws TransformerException {
    int div = getDivision();
    SCCDataSet newscc = new SCCDataSet(div);
    HeaderElement[] headerlist = getHeaderElementList();
    for (HeaderElement h : headerlist) {
      newscc.addHeaderElement(h.time(), h.name(), h.content());
    }
    Part[] partlist = getPartList();
    for (Part p : partlist) {
      SCCDataSet.Part newpart = newscc.addPart(p.serial(), p.channel(), p.prognum(),
                                               p.volume(), p.name());
      SCC.Note[] notelist = p.getNoteList();
      for (SCC.Note n : notelist) {
        if (n instanceof ControlChange) {
          ControlChange cc = (ControlChange)n;
          newpart.addControlChange(cc.onset(div), cc.ctrlnum(), cc.value());
        } else if (n instanceof ProgramChange) {
          ProgramChange pc = (ProgramChange)n;
          newpart.addProgramChange(pc.onset(), pc.value());
        } else if (n instanceof PitchBend) {
          PitchBend pb = (PitchBend)n;
          newpart.addPitchBend(pb.onset(), pb.value());
          //        } else if (n.word() == null) {
          //          newpart.addNoteElement(n.onset(div), n.offset(div), n.notenum(),
          //                                 n.velocity(), n.offVelocity());
        } else {
          newpart.addNoteElement(n.onset(div), n.offset(div),
                                 n.notenum(), n.velocity(),
                                 n.offVelocity(), n.getAttributes());
          //          newpart.addNoteElementWithWord(n.word(), n.onset(div), n.offset(div),
          //                                         n.notenum(), n.velocity(),
          //                                         n.offVelocity());
        }
      }
    }
    Annotation[] annlist = getAnnotationList();
    if (annlist != null) {
      for (Annotation a : annlist ) {
        newscc.addAnnotation(a.type(), a.onset(div), a.offset(div),
                             a.content());
      }
    }
    return newscc;
  }


  public SCC.HeaderElement getFirstTempo() {
    return SCCUtils.getFirstHeader(this, "TEMPO");
  }

  public SCC.HeaderElement getFirstKey() {
    return SCCUtils.getFirstHeader(this, "KEY");
  }


  //  public SCCDataSet toDataSet() throws TransformerException {
  //    return SCCUtils.toDataSet(this);
  //  }


}
