package jp.crestmuse.cmx.filewrappers;
import java.util.*;
import java.io.*;
import org.w3c.dom.*;
import javax.xml.transform.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import jp.crestmuse.cmx.handlers.*;
import jp.crestmuse.cmx.misc.*;
import jp.crestmuse.cmx.misc.MIDIConst;
import jp.crestmuse.cmx.misc.MIDIEventList;

public class SCCXMLWrapper extends CMXFileWrapper implements PianoRollCompatible {
	/** newOutputData()に指定するトップタグ名．スペルミス防止．
	 * 
	 * @author Hashida
	 * @since 2007.7.18
	 */
  public static final String TOP_TAG = "scc";
  
  private int division = 0;
  private Part[] partlist = null;
  
  private boolean headerStarted = false;
  private boolean partStarted = false;

  private Map<NumberedNote,MusicXMLWrapper.Note> notemap = 
    new HashMap<NumberedNote,MusicXMLWrapper.Note>();
 
  private Map<NumberedNote,Byte> nEqualNotes = 
    new HashMap<NumberedNote,Byte>();

    // added 2008.02.21
  private Map<MutableNote,Note> notemap2 = new HashMap<NumberedNote,Note>();

  private int currentPart = 0;

  public int getDivision() {
    if (division == 0)
      division = NodeInterface.getAttributeInt
        (getDocument().getDocumentElement(),  "division");
    return division;
  }

  public void setDivision(int div) {
    getDocument().getDocumentElement().setAttribute("division", 
                                                    String.valueOf(div));
    division = div;
  }

  public void beginHeader() {
    checkElementAddition(!headerStarted);
    addChild("header");
    headerStarted = true;
  }

  public void addHeaderElement(int time, String name, String content) {
    checkElementAddition(headerStarted);
    addChild("meta");
    setAttribute("name", name);
    setAttribute("content", content);
    setAttribute("time", time);
    returnToParent();
  }

  public void addHeaderElement(int time, String name, double value) {
    addHeaderElement(time, name, String.valueOf(value));
  }

  public void addHeaderElement(int time, String name, int value) {
    addHeaderElement(time, name, String.valueOf(value));
  }

  public void endHeader() {
    checkElementAddition(headerStarted);
    returnToParent();
    headerStarted = false;
  }

  public void newPart(int serial, int ch, int pn, int vol) {
    checkElementAddition(!partStarted);
    addChild("part");
    setAttribute("serial", serial);
    setAttribute("ch", ch);
    setAttribute("pn", pn);
    setAttribute("vol", vol);
    currentPart = serial;
    partStarted = true;
  }

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


  public void addControlChange(int onset, int offset, 
                               int ctrlnum, int value) {
    checkElementAddition(partStarted);
    addChild("control");
    addText(onset + " " + offset + " " + ctrlnum + " " + value);
    returnToParent();
  }

  public void addNoteElement(int onset, int offset,
                             int notenum, int velocity) {
    addNoteElement(onset, offset, notenum, velocity, null);
  }

  public void addNoteElement(int onset, int offset, 
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

  public void addNoteElement(int onset, int offset, int notenum, 
                             int velocity, int offVelocity) {
    addNoteElement(onset, offset, notenum, velocity, offVelocity, null);
  }

  public void addNoteElement(int onset, int offset, int notenum, 
                             int velocity, int offVelocity, 
                             MusicXMLWrapper.Note note) {
    checkElementAddition(partStarted);
    addChild("note");
    addText(onset + " " + offset + " " + notenum + " " + velocity
            + " " + offVelocity);
    if (note != null)
      putNoteMap(onset, offset, notenum, velocity, offVelocity, getDivision(), 
                 currentPart, note);
//      notemap.put(new MutableNote(onset, offset, notenum, velocity, 
//                                  offVelocity), note);
    returnToParent();
  }

  private void putNoteMap(int onset, int offset, int notenum, int velocity,
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

  public class HeaderElement extends NodeInterface {
    private int timestamp;
    private String name;
    private String content;
    private HeaderElement(Node node) {
      super(node);
      timestamp = getAttributeInt(node, "time");
      name = getAttribute(node, "name");
      content = getAttribute(node, "content");
    }
    protected String getSupportedNodeName() {
      return "meta";
    }
    public int time() {
      return timestamp;
    }
    public String name() {
      return name;
    }
    public String content() {
      return content;
    }
  }

  public HeaderElement[] getHeaderElementList() {
    Node headnode = selectSingleNode("/scc/header");
    if (headnode == null) {
      return null;
    } else {
      NodeList metalist = selectNodeList(headnode, "meta");
      int size = metalist.getLength();
      HeaderElement[] headlist = new HeaderElement[size];
      for (int i = 0; i < size; i++) 
        headlist[i] = new HeaderElement(metalist.item(i));
      return headlist;
    }
  }

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
        int timestamp = NodeInterface.getAttributeInt(node, "time");
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
      for (int i = 0; i < size; i++)
        partlist[i] = new Part(nl.item(i));
    }
    return partlist;
  }

//    public int getNumOfParts() throws TransformerException {
//	return selectNodeList("/scc/part").getLength();
//    }


  public ArrayList<SimpleNoteList> getPartwiseNoteList
  (final int ticksPerBeat) throws TransformerException {
    final ArrayList<SimpleNoteList> l = new ArrayList<SimpleNoteList>();
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

  public InputStream getMIDIInputStream() throws IOException,
  TransformerException,ParserConfigurationException,SAXException {
    return toMIDIXML().getMIDIInputStream();
  }

  public MIDIXMLWrapper toMIDIXML() 
    throws ParserConfigurationException,TransformerException,
    SAXException,IOException {
    MIDIXMLWrapper dest = 
      (MIDIXMLWrapper)CMXFileWrapper.createDocument(MIDIXMLWrapper.TOP_TAG);
    toMIDIXML(dest);
    return dest;
  }

  public void toMIDIXML(final MIDIXMLWrapper dest)
    throws ParserConfigurationException,TransformerException,
    SAXException,IOException {
    dest.addElementsFirstForFormat1(getPartList().length + 1, getDivision());
    processNotes(new SCCHandler() {
        private int currentTrack = 1;
        private int currentTime = 0;
        public final void beginHeader(SCCXMLWrapper w) {
          dest.newTrack(currentTrack);
        }
        public final void endHeader(SCCXMLWrapper w) {
          dest.endTrack();
          currentTrack++;
        }
        public final void processHeaderElement(int timestamp, String name, 
                                         String content, SCCXMLWrapper w) {
          if (name.equals("TEMPO"))
            dest.addMetaEvent("SetTempo", timestamp - currentTime, 
                              (int)(1000*1000*60/Double.parseDouble(content)));
          currentTime = timestamp;
        }
        public final void beginPart(Part part, SCCXMLWrapper w) {
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
    dest.finalizeDocument();
  }

  public SCCXMLWrapper replaceVelocity(List<List<Byte>> vellist, boolean sorted) 
    throws TransformerException, InvalidFileTypeException, ParserConfigurationException,
    SAXException, IOException   {
    final SCCXMLWrapper newscc = (SCCXMLWrapper)CMXFileWrapper.createDocument(TOP_TAG);
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
      public final void processHeaderElement(int timestamp, String name, 
                                             String content, SCCXMLWrapper w) {
        newscc.addHeaderElement(timestamp, name, content);
      }
      public final void beginPart(Part part, SCCXMLWrapper w) {
        newscc.newPart(part.serial(), part.channel(), part.prognum(), part.volume());
        it2 = it1.next().iterator();
      }
      public final void endPart(Part part, SCCXMLWrapper w) {
        newscc.endPart();
      }
      public final void processNote(Note note, SCCXMLWrapper w) {
        byte velocity = it2.next();
        newscc.addNoteElement(note.onset(), note.offset(), note.notenum(), velocity, 
                              note.getMusicXMLWrapperNote());
      }
    };
    if (sorted)
      processSortedNotes(h);
    else
      processNotes(h);
    newscc.finalizeDocument();
    return newscc;
  }

  public SCCXMLWrapper changeVelocity(List<List<Byte>> diff, boolean sorted)
    throws TransformerException, InvalidFileTypeException, ParserConfigurationException,
    SAXException, IOException {
    final SCCXMLWrapper newscc = (SCCXMLWrapper)CMXFileWrapper.createDocument(TOP_TAG);
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
      public final void processHeaderElement(int timestamp, String name,
                                             String content, SCCXMLWrapper w) {
        newscc.addHeaderElement(timestamp, name, content);
      }
      public final void beginPart(Part part, SCCXMLWrapper w) {
        newscc.newPart(part.serial(), part.channel(), part.prognum(), part.volume());
        it2 = it1.next().iterator();
      }
      public final void endPart(Part part, SCCXMLWrapper w) {
        newscc.endPart();
      }
      public final void processNote(Note note, SCCXMLWrapper w) {
        byte diffvel = it2.next();
        int vel = Math.max(Math.min(note.velocity + diffvel, 127), 0);
        newscc.addNoteElement(note.onset(), note.offset(), note.notenum(), vel, 
                              note.getMusicXMLWrapperNote());
      }
    };
    if (sorted)
      processSortedNotes(h);
    else
      processNotes(h);
    newscc.finalizeDocument();
    return newscc;
  }
    
  public class Part extends NodeInterface {
    private Note[] notelist = null;
    private List<Note> noteonlylist = null;

    private Part(Node node) {
      super(node);
    }

    @Override
    protected final String getSupportedNodeName() {
      return "part";
    }

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
        for (int i = 0; i < size; i++) {
          Node node = nl.item(i);
          if (node.getNodeName().equals("note")) {
            notelist[i] = new Note(node, this);
            noteonlylist.add(notelist[i]);
          } else if (node.getNodeName().equals("control")) {
            notelist[i] = new ControlChange(node, this);
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
                         ) : n1.offset() - n2.offset()
                        ) : n1.onset() - n2.onset();
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
                         ) : n1.offset() - n2.offset()
                        ) : n1.onset() - n2.onset();
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
                                  ) : n1.onset() - n2.onset();
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
                                  ) : n1.onset() - n2.onset();
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
      for (Note note : notelist) {
        if (note instanceof ControlChange) {
          el.addEvent(note.onset(), MIDIConst.CONTROL_CHANGE, ch, 
                      ((ControlChange)note).ctrlnum(), 
                      ((ControlChange)note).value());
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
      return getAttributeInt(node(), "pn");
    }

    public final int volume() {
      return getAttributeInt(node(), "vol");
    }
  }

  public class Note extends NodeInterface implements NoteCompatible {
    private Part part;
    private int onset, offset, notenum, velocity, offVelocity;

    private Note(Node node, Part part) {
      super(node);
      this.part = part;
      String[] data = getText(node()).split("\\s");
      onset = Integer.parseInt(data[0]);
      offset = Integer.parseInt(data[1]);
      notenum = Integer.parseInt(data[2]);
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
      return "note|control";
    }

    public final int onset() {
      return onset;
    }

    public final int onset(int ticksPerBeat) {
      if (ticksPerBeat == getDivision())
        return onset;
      else
        return onset * ticksPerBeat / getDivision();
    }

    public final int offset() {
      return offset;
    }

    public final int offset(int ticksPerBeat) {
      if (ticksPerBeat == getDivision())
        return offset;
      else
        return offset * ticksPerBeat / getDivision();
    }

    public final int duration(int ticksPerBeat) {
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

  private class NumberedNote extends MutableNote {
    private int partid;
    private byte number = 1;
    private NumberedNote(int onset, int offset, int notenum, int velocity, 
                     int ticksPerBeat, int partid) {
      this(onset, offset, notenum, velocity, velocity, ticksPerBeat, partid);
    }
    private NumberedNote(int onset, int offset, int notenum, int velocity, 
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
}