package jp.crestmuse.cmx.filewrappers;
import jp.crestmuse.cmx.handlers.*;
import jp.crestmuse.cmx.misc.*;
import static jp.crestmuse.cmx.misc.MIDIConst.*;
import java.util.*;
import java.io.*;
import java.nio.*;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class MIDIXMLWrapper extends CMXFileWrapper implements PianoRollCompatible {
	/** newOutputData() トップタグの追加 
	 * @author Hashida
	 * @since 2007.7.18
	 */
  public static final String TOP_TAG = "MIDIFile";

  private byte format;
  private int ticksPerBeat;
  private byte timestampType;
  private int nTracks;
  public static final byte TIMESTAMP_DELTA = 1;
  public static final byte TIMESTAMP_ABSOLUTE = 2;

  private boolean firstElementsAdded = false;
  private boolean trackStarted = false;

  private String smfFileName = null;

  private Track[] tracks = null;

  public String getSMFFileName() {
    return smfFileName;
  }

  public void addElementsFirstForFormat0(int ticksPerBeat) {
    format = 0;
    nTracks = 1;
    this.ticksPerBeat = ticksPerBeat;
    timestampType = TIMESTAMP_DELTA;
    addChild("Format");
    addText("0");
    addSibling("TrackCount");
    addText("1");
    addSibling("TicksPerBeat");
    addText(ticksPerBeat);
    addSibling("TimestampType");
    addText("Delta");
    returnToParent();
    firstElementsAdded = true;
  }

  public void addElementsFirstForFormat1(int nTracks, int ticksPerBeat) {
    format = 1;
    this.nTracks = nTracks;
    this.ticksPerBeat = ticksPerBeat;
    timestampType = TIMESTAMP_DELTA;
    addChild("Format");
    addText("1");
    addSibling("TrackCount");
    addText(nTracks);
    addSibling("TicksPerBeat");
    addText(ticksPerBeat);
    addSibling("TimestampType");
    addText("Delta");
    returnToParent();
    firstElementsAdded = true;
  }

  public void newTrack(int number) {
    checkElementAddition(firstElementsAdded);
    checkElementAddition(!trackStarted);
    addChild("Track");
    setAttribute("Number", number);
    trackStarted = true;
  }

  public void endTrack(boolean needsMetaEvent) {
    checkElementAddition(trackStarted);
    if (needsMetaEvent)
      addMetaEvent("EndOfTrack", 4 * ticksPerBeat);
    returnToParent();
    trackStarted = false;
  }

  public void endTrack() {
    endTrack(true);
  }

  public void addMIDIChannelMessages(MIDIEventList el) {
    int currentTime = 0;
    for (MIDIEventList.MIDIEvent e : el) {
      addMIDIChannelMessage(e.msgname(), 
                            e.time() - currentTime, 
                            e.channel(), 
                            e.value1(), 
                            e.value2());
      currentTime = e.time();
    }
  }

  public void addMIDIChannelMessage(String type, int timestamp, 
                                    byte channel, int... attrs) {
    checkElementAddition(trackStarted);
    addChild("Event");
    if (timestampType == TIMESTAMP_DELTA)
      addChild("Delta");
    else if (timestampType == TIMESTAMP_ABSOLUTE)
      addChild("Absolute");
    else
      throw new InvalidElementException("TimestampType is invalid");
    addText(String.valueOf(timestamp));
    if (isSupportedMessage(type)) {
      addSibling(type);
      setAttribute("Channel", String.valueOf(channel));
      String[] attlist = getAttributeList(type);
      //      if (attlist.length == attrs.length) 
        for (int i = 0; i < attlist.length; i++)
          if (isOnOffMessage(type)) 
            setAttribute(attlist[i], 
                         onOffMsgByteToString((byte)attrs[i]));
          else
            setAttribute(attlist[i], attrs[i]);
	//      else
	//  throw new InvalidElementException("# of values is invalid");
    } else {
      throw new InvalidElementException
        ("Unsupported channel message: " + type);
    }
    returnToParent();
    returnToParent();
  }

  public void addMetaEvent(String type, int timestamp, int... content) {
    checkElementAddition(trackStarted);
    addChild("Event");
    if (timestampType == TIMESTAMP_DELTA)
      addChild("Delta");
    else if (timestampType == TIMESTAMP_ABSOLUTE)  // not supported
      addChild("Absolute");
    else
      throw new InvalidElementException("TimestampType is invalid");
    addText(String.valueOf(timestamp));
    if (isSupportedMetaEvent(type)) {
      addSibling(type);
      String[] attlist = getAttributeList(type);
      if (attlist.length == content.length) 
        for (int i = 0; i < content.length; i++) 
          setAttribute(attlist[i], content[i]);
      else
        throw new InvalidElementException("# of values is invalid");
    } else {
      throw new InvalidElementException
        ("Unsupported meta event: " + type);
    }
    returnToParent();
    returnToParent();
  }

  public void writefileAsSMF(String filename) throws IOException {
    DataOutputStream dataout = new DataOutputStream 
      (new BufferedOutputStream(new FileOutputStream(filename)));
    smfFileName = filename;
    writeAsSMF(dataout);
  }

  public void writefileAsSMF(File file) throws IOException {
    DataOutputStream dataout = new DataOutputStream 
      (new BufferedOutputStream(new FileOutputStream(file)));
    smfFileName = file.getName();
    writeAsSMF(dataout);
  }

  void writeAsSMF(DataOutputStream dataout) throws IOException {
    // write header chunk
    dataout.writeBytes("MThd");
    dataout.writeInt(6);
    dataout.writeShort(format);
    dataout.writeShort(nTracks);
    dataout.writeShort(ticksPerBeat);
    // write track chunks
    Track[] tracks = getTrackList();
    if (tracks.length != nTracks) 
      throw new InvalidElementException("TrackCount is wrong");
//    for (int i = 1; i <= nTracks; i++) {
//      Track track = getTrackNodeInterface(i);
    for (Track track : tracks) {
      byte[] msg = track.toSMFFormat();
      dataout.writeBytes("MTrk");
      dataout.writeInt(msg.length);
      dataout.write(msg, 0, msg.length);
    }
    dataout.close();
  }

  public static MIDIXMLWrapper readSMF(String filename) 
    throws IOException,javax.xml.parsers.ParserConfigurationException,
    org.xml.sax.SAXException,javax.xml.transform.TransformerException  {
    MIDIXMLWrapper midixml = 
      (MIDIXMLWrapper)createDocument("MIDIFile");
    DataInputStream datain = new DataInputStream
      (new BufferedInputStream(new FileInputStream(filename)));
    midixml.readSMF(datain);
    midixml.finalizeDocument();
    midixml.smfFileName = filename;
    return midixml;
  }

  private void readSMF(DataInputStream datain) throws IOException {
    byte[] headbuff = new byte[4];
    byte[] buff = new byte[256];
    int trk = 0;
    do {
      datain.read(headbuff);
      int chunklength = datain.readInt();
      if (chunklength > buff.length) 
        buff = new byte[chunklength];
      datain.read(buff, 0, chunklength);
      if (headbuff[0] == 'M' && headbuff[1] == 'T') {
        if (headbuff[2] == 'h' && headbuff[3] == 'd') {
          readSMFHeaderChunk(buff, chunklength);
        } else if (headbuff[2] == 'r' && headbuff[3] == 'k') {
          readSMFTrackChunk(buff, chunklength, trk++);
        } else {
          throw new InvalidFileTypeException("Invalid SMF");
        } 
      } else {
        throw new InvalidFileTypeException("Invalid SMF");
      }
    } while (datain.available() > 0);
  }

  private void readSMFHeaderChunk(byte[] buff, int length) {
    ByteBuffer buff2 = ByteBuffer.wrap(buff, 0, length);
    format = (byte)buff2.getShort();
    nTracks = buff2.getShort();
    ticksPerBeat = buff2.getShort();
    timestampType = TIMESTAMP_DELTA;
    if (format == 1)
      addElementsFirstForFormat1(nTracks, ticksPerBeat);
    else if (format == 0)
      addElementsFirstForFormat0(ticksPerBeat);
    else
      throw new IllegalStateException("Unsupported");
  }

  private int ignoredDelta = 0;

  private void readSMFTrackChunk(byte[] buff, int length, int number ) {
    ByteBuffer buff2 = ByteBuffer.wrap(buff, 0, length);
    newTrack(number);
    short running = 0;
    while (buff2.hasRemaining()) {
      int delta = 0;
      byte b;
      do {
        b = buff2.get();
        delta = delta * 0x80 + (b & (byte)0x7F);
      } while (b < 0);
      b = buff2.get();
      short status = (b > 0) ? b : (short)(b & 0x7F + 0x80);
      if (status == 0xFF)
        readMetaEvent(buff2, delta);
      else if (status > 0xF0)
        ;
      else if (status == 0xF0)
        readMIDIExclusiveMessage(buff2, delta);
      else if (status >= 0x80)
        readMIDIChannelMessage((running = status), buff2, delta);
      else {
        buff2.position(buff2.position() - 1);
        readMIDIChannelMessage(running, buff2, delta);
      }
    }
    endTrack(false);
  }

  private void readMetaEvent(ByteBuffer buff, int delta) {
    byte evttype = buff.get();
    byte len = buff.get();
    byte[] bytedata = new byte[len];
    ByteBuffer buff2 = ByteBuffer.wrap(bytedata);
    buff.get(bytedata);
    if (isSupportedMetaEvent(evttype)) {
      String evtname = metaEventTypeToName(evttype);
      String[] attlist = getAttributeList(evtname);
      byte l = getByteLength(evtname);
      int[] content = new int[attlist.length];
      for (int i = 0; i < attlist.length; i++) {
        for (byte j = 0; j < l; j++) {
          int b = buff2.get();
          if (b < 0) b = b & 0x7F + 0x80;
          content[i] = content[i] * 256 + b;
        }
      }
      addMetaEvent(evtname, delta + ignoredDelta, content);
      ignoredDelta = 0;
    } else {
      ignoredDelta += delta;
    }
  }

  private void readMIDIExclusiveMessage(ByteBuffer buff, int delta) {
    byte len = buff.get();
    for (byte i = 0; i < len; i++)
      buff.get();
    ignoredDelta += delta;
  }  

  private void readMIDIChannelMessage(short status, ByteBuffer buff, 
                                      int delta) {
    byte ch = (byte)(status & 0x0F);
    ch++;
    status &= 0xF0;
    String msgtype = statusNoToMsgName(status);
    String[] attlist = getAttributeList(msgtype);
    int[] values = new int[attlist.length];
    byte length = getByteLength(msgtype);
    for (int i = 0; i < attlist.length; i++) {
      values[i] = 0;
      for (byte j = 0; j < length; j++) 
        values[i] = values[i] * 128 + (buff.get() & 0x7F);
    }
    addMIDIChannelMessage(msgtype, delta + ignoredDelta, ch, values);
    ignoredDelta = 0;
  }

//  public Track getTrackNodeInterface(int number) {
//    return new Track
//      (selectSingleNode("/MIDIFile/Track[" + number + "]"));
//  }
  
//  public MIDIEvent getEventNodeInterface(Node node) {
//    return new MIDIEvent(node);
//  }
    
    /**
     * toSCCXML内で呼び出されます
     * @param h
     * @author totani
     * @since 2007.08.08
     */
  public void processMIDIEvent(MIDIHandler h){
    checkFinalized();
//    h.processHeaders(format, nTracks, ticksPerBeat, timestampType, this);
////    h.processFormat(format, this);
////    h.processTrackCount(nTracks, this);
////    h.processTicksPerBeat(ticksPerBeat, this);
////    h.processTimeStampType(timestampType, this);
    Track[] tracks = getTrackList();
    for (Track track : tracks) {
//    for(int i=1; i<=nTracks; i++){
//      Track track = getTrackNodeInterface(i);
      h.beginTrack(track, this);
//      NodeList midiEventList = track.getMIDIEventList();
      MIDIEvent[] events = track.getMIDIEventList();
      for (MIDIEvent event : events) {
//      for(int j=0; j<midiEventList.getLength(); j++){
//        MIDIEvent event = new MIDIEvent(midiEventList.item(j));
        h.processMIDIEvent(event, this);
      }
      h.endTrack(track, this);
    }
  }

  public void processNotes(CommonNoteHandler h) throws TransformerException, 
                 IOException, ParserConfigurationException, SAXException {
    toSCCXML().processNotes(h);
  }

  public List<SimpleNoteList> getPartwiseNoteList(int tickePerBeat) 
  throws IOException, TransformerException, ParserConfigurationException,
  SAXException   {
    return toSCCXML().getPartwiseNoteList(ticksPerBeat);
  }

  public InputStream getMIDIInputStream() throws IOException {
    PipedInputStream pipein = new PipedInputStream();
    PipedOutputStream pipeout = new PipedOutputStream();
    final DataOutputStream dataout = new DataOutputStream(pipeout);
    pipein.connect(pipeout);
    (new Thread(new Runnable() {
      public void run() {
        try {
          writeAsSMF(dataout);
        } catch (IOException e) {
          new RuntimeException(e.toString());
        }
      }
    })).start();
    return pipein;
  }

  public SCCXMLWrapper toSCCXML() throws TransformerException,IOException,
                                ParserConfigurationException, SAXException {
    SCCXMLWrapper dest = 
      (SCCXMLWrapper)CMXFileWrapper.createDocument(SCCXMLWrapper.TOP_TAG);
    toSCCXML(dest);
    return dest;
  }
  
  public void toSCCXML(final SCCXMLWrapper dest) throws TransformerException, IOException, ParserConfigurationException, SAXException{
    toSCCXML(dest, null);
  }

  public void toSCCXML(final SCCXMLWrapper dest, SCCXMLWrapper.EasyChord[] chords) 
    throws TransformerException, IOException, 
    ParserConfigurationException, SAXException {
    toSCCXML(dest, chords, null);
  }
    
    public void toSCCXML(final SCCXMLWrapper dest, SCCXMLWrapper.EasyChord[] chords, String key) 
    throws TransformerException, IOException, 
    ParserConfigurationException, SAXException {
    dest.setDivision(ticksPerBeat);
    final Map<Byte, ArrayList<MutableMusicEvent>> channelToNotes = 
        new HashMap<Byte, ArrayList<MutableMusicEvent>>();
    final ArrayList<Integer> headerList = new ArrayList<Integer>();
    processMIDIEvent(new MIDIHandler(){
        private int totalTime;
        private MutableNote[] onNotes;
        public void beginTrack(Track track, MIDIXMLWrapper w) {
          totalTime = 0;
          onNotes = new MutableNote[128];
        }
        public void endTrack(Track track, MIDIXMLWrapper w) {
        }
        public void processMIDIEvent(MIDIEvent midiEvent, MIDIXMLWrapper w) {
          totalTime += midiEvent.deltaTime();
          String messageType = midiEvent.messageType();
          if(midiEvent.messageType().equals("SetTempo")){
            headerList.add(totalTime);
            headerList.add(60*1000*1000/midiEvent.value(0));
            return;
          }
          if(isSupportedMetaEvent(midiEvent.messageType())) return;
          short statusNo = msgNameToStatusNo(messageType);
          if((statusNo == NOTE_ON) && (midiEvent.value(1) > 0)){
            MutableNote note = 
              new MutableNote(totalTime, totalTime, midiEvent.value(0), 
                              midiEvent.value(1), ticksPerBeat());
            addNote(note, midiEvent, channelToNotes, onNotes);
          }else if((statusNo == NOTE_OFF) || 
                   (statusNo == NOTE_ON) && (midiEvent.value(1) == 0)) {
            addNoteOff(totalTime, midiEvent, channelToNotes);
          } else if (statusNo == CONTROL_CHANGE) {
            MutableControlChange c = 
              new MutableControlChange(totalTime, midiEvent.value(0), 
                                  midiEvent.value(1), ticksPerBeat());
            addControlChange(c, midiEvent, channelToNotes);
          }
        }
        private void addControlChange(MutableControlChange c, 
                                      MIDIEvent e, 
                                      Map<Byte,ArrayList<MutableMusicEvent>> 
                                      chnlwiseNotes) {
          if (!chnlwiseNotes.containsKey(e.channel()))
            chnlwiseNotes.put(e.channel(), new ArrayList<MutableMusicEvent>());
          chnlwiseNotes.get(e.channel()).add(c);
        }
        private void addNote(MutableNote note, MIDIEvent event, 
                     Map<Byte,ArrayList<MutableMusicEvent>> chnlwiseNotes,
                     MutableNote[] onNotes) {
          int notenum = note.notenum();
          if (onNotes[notenum] != null)
            onNotes[notenum].setOffset(note.onset() - 1);
          if (!chnlwiseNotes.containsKey(event.channel()))
            chnlwiseNotes.put(event.channel(), 
                               new ArrayList<MutableMusicEvent>());
          chnlwiseNotes.get(event.channel()).add(note);
          onNotes[note.notenum()] = note;
        }
        private void addNoteOff(int offset, MIDIEvent event, 
                     Map<Byte,ArrayList<MutableMusicEvent>> chnlwiseNotes) {
          int notenum = event.value(0);
          if (onNotes[notenum] != null) {
             onNotes[notenum].setOffset(offset);
             onNotes[notenum] = null;
          }
//          if (chnlwiseNotes.containsKey(event.channel())) {
//            ArrayList<MutableMusicEvent> notes = 
//              chnlwiseNotes.get(event.channel());
//            for (int i = notes.size() - 1; i >= 0; i--) {
//              MutableMusicEvent note = notes.get(i);
//              if (note instanceof MutableNote 
//                  && ((MutableNote)note).notenum() == event.value(0)) {
//                ((MutableNote)note).setOffset(offset);
//                break;
//              }
//            }
//          } else {
//            throw new InvalidElementException();
//          }
        }
      });
    dest.beginHeader();
    for(int i=0;i<headerList.size();i+=2){
      dest.addHeaderElement(headerList.get(i), "TEMPO", headerList.get(i+1));
    }
    if(key != null) dest.addHeaderElement(0, "KEY", key);
    dest.endHeader();
    Map.Entry<Byte, ArrayList<MutableMusicEvent>> mapent;
    Iterator<Map.Entry<Byte, ArrayList<MutableMusicEvent>>> iterator 
      = channelToNotes.entrySet().iterator();
    while(iterator.hasNext()){
      mapent = iterator.next();
      byte channel = mapent.getKey();
      ArrayList<MutableMusicEvent> notes = mapent.getValue();
      dest.newPart(channel, channel, 0, 100);
      for(MutableMusicEvent e : notes){
        if (e instanceof MutableNote) {
          MutableNote note = (MutableNote)e;
          dest.addNoteElement(note.onset(), note.offset(), note.notenum(), 
                              note.velocity(), note.offVelocity());
        } else if (e instanceof MutableControlChange) {
          MutableControlChange cc = (MutableControlChange)e;
          dest.addControlChange(cc.onset(), cc.offset(), cc.ctrlnum(), 
                                cc.value());
        }
      }
      dest.endPart();
    }
    if(chords != null){
      dest.beginAnnotations();
      for(SCCXMLWrapper.EasyChord c : chords)
    	  //String[] chords の要素がnullの時は追加しない処理を追加
    	  if(c != null){
    		  dest.addChord(c.onset, c.offset, c.chord);
    	  }
   
      dest.endAnnotations();
    }
    dest.finalizeDocument();
  }

/*
  private class MyControlChange extends MutableNote {
    private MyControlChange(int time, int ctrlnum, int value) {
      super(time, time, ctrlnum, value);
    }
  }
*/

  public int format() {
    return format;
  }

  public int trackCount() {
    return nTracks;
  }
    
  public int ticksPerBeat(){
    return ticksPerBeat;
  }

  protected void analyze() {
    if (!firstElementsAdded) {
      format = 
        (byte)NodeInterface.getTextInt(selectSingleNode("/MIDIFile/Format"));
      nTracks = 
        NodeInterface.getTextInt(selectSingleNode("/MIDIFile/TrackCount"));
      ticksPerBeat = 
        NodeInterface.getTextInt(selectSingleNode("/MIDIFile/TicksPerBeat"));
    }
  }

  public Track[] getTrackList() {
    if (tracks == null) {
      NodeList nl = selectNodeList("/MIDIFile/Track");
      int size = nl.getLength();
      tracks = new Track[size];
      for (int i = 0; i < size; i++) 
        tracks[i] = new Track(nl.item(i));
    }
    return tracks;
  }

    public class Track extends NodeInterface {
      private MIDIEvent[] events;

      private Track(Node node) {
        super(node);
      }

      @Override
      protected String getSupportedNodeName() {
        return "Track";
      }

//      public NodeList getMIDIEventList() {
//        return selectNodeList(node(), "Event");
//      }

      public MIDIEvent[] getMIDIEventList() {
        if (events == null) {
          NodeList nl = getChildNodes();
          int size = nl.getLength();
          events = new MIDIEvent[size];
          for (int i = 0; i < size; i++) 
            events[i] = new MIDIEvent(nl.item(i));
        }
        return events;
      }

	byte[] toSMFFormat() {
	    ArrayList<byte[]> byteArray = new ArrayList<byte[]>();
//	    NodeList nl = getMIDIEventList();
//	    for (int i = 0; i < nl.getLength(); i++) {
//		MIDIEvent evt = getEventNodeInterface(nl.item(i));
            MIDIEvent[] events = getMIDIEventList();
            for (MIDIEvent evt : events) {
		byteArray.add(evt.getDeltaTimeForSMF());
		byteArray.add(evt.toMIDIMessageBinary());
	    }
	    //	    byteArray.add(MIDI_EVENT_END_OF_TRACK);
	    int datasize = 0;
	    for (byte[] b : byteArray) 
		datasize += b.length;
	    byte[] bytes = new byte[datasize];
	    int i = 0;
	    for (byte[] b : byteArray) {
		for (int j = 0; j < b.length; j++) {
		    bytes[i] = b[j];
		    i++;
		}
	    }
	    return bytes;
	}
    }

    public class MIDIEvent extends NodeInterface {

	private Node child1, child2;
	private int deltaTime;
	private String msgType;
	private boolean isChannelMessage;
	private boolean isMetaEvent;
	private int[] values;
	//	private int msgValue1 = -1, msgValue2 = -1;
	private byte channel = -1;
	

	private MIDIEvent(Node node) {
	    super(node);
	    NodeList nl = node().getChildNodes();
	    child1 = nl.item(0);
	    if (child1.getNodeName().equals("Delta"))
		deltaTime = getTextInt(child1);
	    else
		throw new InvalidElementException("No Delta element.");
	    child2 = nl.item(1);
	    msgType = child2.getNodeName();
	    isChannelMessage = isSupportedMessage(msgType);
	    isMetaEvent = isSupportedMetaEvent(msgType);
	    if (isChannelMessage)
		channel = Byte.parseByte(getAttribute(child2, "Channel"));
	    String[] attrkeys = getAttributeList(msgType);
	    values = new int[attrkeys.length];
	    for (int i = 0; i < attrkeys.length; i++)
		if (isOnOffMessage(msgType)) 
		    values[i] = 
		      onOffMsgStringToByte(getAttribute(child2, attrkeys[i]));
		else
		    values[i] = getAttributeInt(child2, attrkeys[i]);
	}

	@Override
	protected final String getSupportedNodeName() {
	    return "Event";
	}

	public final int deltaTime() {
	    return deltaTime;
	}

	public final String messageType() {
	    return msgType;
	}

	public final int value(int i) {
	    return values[i];
	}

	public final int[] values() {
	    return values;
	}
	
	/* テンポデータを抽出 '07.8.26 by hashida
	 * （meta event の取り出し方がわからなかった・・・
	 * 　誰か追加・修正・教授してください） 
	 */
	public final byte[] tempoValues() {
		if(!isMetaEvent)return null;
		int tempoInMPQ = values[0];
		byte[] data = new byte[3];
		data[0] = (byte)((tempoInMPQ >> 16) & 0xFF);
		data[1] = (byte)((tempoInMPQ >> 8) & 0xFF);
		data[2] = (byte)(tempoInMPQ & 0xFF);
		
		return data;
	}

	public final byte channel() {
	    return channel;
	}

	public final boolean isChannelMessage() {
	    return isChannelMessage;
	}

	public final boolean isMetaEvent() {
	    return isMetaEvent;
	}

	byte[] getDeltaTimeForSMF() {
	    int size = 
		(Integer.SIZE-Integer.numberOfLeadingZeros(deltaTime)+1)/7+1;
	    int d = deltaTime;
	    byte[] bytes = new byte[size];
	    for (int i = size - 1; i >= 0; i--) {
		bytes[i] = (byte)(d % 0x80);
		if (i < size - 1) bytes[i] |= 0x80;
		d = d / 0x80;
		//	if (d == 0) break;
	    }
	    return bytes;
	}

	byte[] toMIDIMessageBinary() {
	    int counter = 0;
	    byte[] data = new byte[16];
	    if (isChannelMessage) {
		short statusBase = msgNameToStatusNo(msgType);
		if (statusBase >= 0x80) {
		    data[counter] = (byte)(statusBase + channel - 1);
		    counter++;
		} else {
		    data[counter] = (byte)(0xB0 + channel - 1);
		    counter++;
		    data[counter] = (byte)(statusBase);
		}
		for (int i = 0; i < values.length; i++) {
		    byte len = getByteLength(msgType);
		    if (len == (byte)1) {
			data[counter] = (byte)values[i];
			counter++;
		    } else if (len == (byte)2) {
			data[counter] = (byte)(values[i] % 0x80);
			counter++;
			data[counter] = (byte)(values[i] / 0x80);
			counter++;
		    } else {
			throw new IllegalArgumentException();
		    }
		}
		if (values.length == 0) {
		    data[counter] = DUMMY_VALUE;
		    counter++;
		}
	    } else if (isMetaEvent) {
		data[counter] = (byte)(0xFF);
		counter++;
		data[counter] = (byte)(metaEventNameToType(msgType));
		counter++;
		int clen = counter;
		counter++;
		for (int i = 0; i < values.length; i++) {
		    byte len = getByteLength(msgType);
		    if (len == (byte)1) {
			data[counter] = (byte)values[i];
			counter++;
		    } else if (len == (byte)2) {
			data[counter] = (byte)(values[i] / 0x100);
			counter++;
			data[counter] = (byte)(values[i] % 0x100);
			counter++;
		    } else if (len == (byte)3) {
			data[counter] = (byte)(values[i] / 0x10000);
			counter++;
			data[counter] = (byte)(values[i] / 0x100 % 0x100);
			counter++;
			data[counter] = (byte)(values[i] % 0x100);
			counter++;
		    } else {
			throw new IllegalArgumentException();
		    }
		}
		data[clen] = (byte)(counter - clen - 1);
	    }
	    byte[] data2 = new byte[counter];
	    System.arraycopy(data, 0, data2, 0, counter);
	    return data2;
	}
    }
}
