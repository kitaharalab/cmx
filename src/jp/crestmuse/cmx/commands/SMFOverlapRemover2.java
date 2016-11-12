package jp.crestmuse.cmx.commands;
import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.filewrappers.MIDIXMLWrapper.*;
//import jp.crestmuse.cmx.filewrappers.SCCXMLWrapper.*;
import static jp.crestmuse.cmx.misc.MIDIConst.*;
import jp.crestmuse.cmx.misc.*;
import javax.xml.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import org.xml.sax.*;
import java.io.*;
import java.util.*;

class SMFOverlapRemover2 extends 
                               CMXCommand<MIDIXMLWrapper,MIDIXMLWrapper> {


  String outSMFFileName;
  String outSCCFileName;

  MIDIEventWithTime[][] last1 = new MIDIEventWithTime[128][16];
  boolean[][] overlapped = new boolean[128][16];

  class MIDIEventWithTime implements Comparable {
    MIDIEvent evt;
    int time;
    MIDIEventWithTime(MIDIEvent e, int t) {
      evt = e;
      time = t;
    }
    public int compareTo(Object o) {
      return time - ((MIDIEventWithTime)o).time;
    }
      public boolean equals(Object o) {
	  if (o instanceof MIDIEventWithTime) {
	      MIDIEventWithTime a = (MIDIEventWithTime)o;
	      return time == a.time 
		  && evt.messageType().equals(a.evt.messageType())
		  && evt.channel() ==  a.evt.channel()
		  && evt.value(0) == a.evt.value(0)
		  && evt.value(1) == a.evt.value(1);
	  } else {
	      return false;
	  }
      }
      public String toString() {
	  return time + " " + evt.messageType() + " " 
	      + evt.channel() + " " + evt.value(0) + " " 
	      + evt.value(1);
      }
  }

  protected boolean setOptionsLocal(String option, String value) {
    if(option.equals("-smf")) {
      outSMFFileName = value;
      return true;
    } else if(option.equals("-scc")) {
      outSCCFileName = value;
      return true;
    }
    return false;
  }

  int div;
  int nTracks = 1;

  protected MIDIXMLWrapper readInputData(String filename) 
    throws IOException, ParserConfigurationException, SAXException,
    TransformerException {
    return MIDIXMLWrapper.readSMF(filename);
  }

  protected MIDIXMLWrapper run(MIDIXMLWrapper indata)
    throws ParserConfigurationException, SAXException, 
    TransformerException, IOException {
    MIDIXMLWrapper newmidi = 
      (MIDIXMLWrapper)CMXFileWrapper.createDocument(MIDIXMLWrapper.TOP_TAG);
    if (indata.format() == 0)
      newmidi.addElementsFirstForFormat0(div = indata.ticksPerBeat());
    else if (indata.format() == 1)
      newmidi.addElementsFirstForFormat1(nTracks = indata.trackCount(),
                                         div = indata.ticksPerBeat());
    else 
      throw new IllegalStateException("unsupported SMF");
    Track[] tt = indata.getTrackList();
    for (int i = 0; i < tt.length; i++) {
      int currentTime = 0;
      MIDIEvent[] evts = tt[i].getMIDIEventList();
      List<MIDIEventWithTime> l = new ArrayList<MIDIEventWithTime>();
      for (MIDIEvent e : evts) {
        currentTime += e.deltaTime();
        MIDIEventWithTime et = new MIDIEventWithTime(e, currentTime);
        addMIDIEventWithTime(et, l);
      }
      System.err.println();
      Collections.sort(l);
      newmidi.newTrack(i);
      addMIDIEventsToMIDIXML(l, newmidi);
      newmidi.endTrack(false);
      System.err.println();
    }
    if(outSMFFileName != null) {
      newmidi.finalizeDocument();
      newmidi.writefileAsSMF(outSMFFileName);
    } else if(outSCCFileName != null) {
      newmidi.finalizeDocument();
      newmidi.toSCCXML().writefile(outSCCFileName);
    }
    return newmidi;
  }

  private void addMIDIEventWithTime(MIDIEventWithTime e, 
                                    List<MIDIEventWithTime> l) {
    if (isNoteOn(e)) {
      int nn = e.evt.value(0);
      int ch = e.evt.channel();
      if (last1[nn][ch] == null) {
	  last1[nn][ch] = e;
      } else {
	  System.err.println(e);
	  System.err.println(last1[nn][ch]);
	  System.err.println(last1[nn][ch].equals(e));
	  if (last1[nn][ch].equals(e)) {
	      overlapped[nn][ch] = true;
	      System.err.println("Overlapped: " + e.time + "\t" + nn + "\t" + e.evt.value(1));
	      e = null;
	  } else {
	      last1[nn][ch] = e;
	  }
      }
    } else if (isNoteOff(e)) {
      int nn = e.evt.value(0);
      int ch = e.evt.channel();
	if (overlapped[nn][ch]) {
	    e = null;
	    overlapped[nn][ch] = false;
	}
	last1[nn][ch] = null;
    }
    if (e != null)
	l.add(e);
  }
  
  private boolean isNoteOn(MIDIEventWithTime e) {
    return e.evt.messageType().equals("NoteOn")
      && e.evt.value(1) > 0;
  }
  
  private boolean isNoteOff(MIDIEventWithTime e) {
    return e.evt.messageType().equals("NoteOff")
      || (e.evt.messageType().equals("NoteOn")
          && e.evt.value(1) == 0);
  }

  private void addMIDIEventsToMIDIXML(List<MIDIEventWithTime> l, 
                                      MIDIXMLWrapper newmidi) {
    MIDIEventWithTime last = null;
    for (MIDIEventWithTime e : l) {
      int deltaTime;
      if (last == null)
        deltaTime = e.time;
      else
        deltaTime = e.time - last.time;
      if (isSupportedMessage(e.evt.messageType()))
        newmidi.addMIDIChannelMessage(e.evt.messageType(), deltaTime, 
                                      e.evt.channel(), e.evt.values());
      else
        newmidi.addMetaEvent(e.evt.messageType(), deltaTime, e.evt.values());
      last = e;
    }
  }


  public static void main(String[] args) {
    SMFOverlapRemover2 sor = new SMFOverlapRemover2();
    try {
      sor.start(args);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }
}
