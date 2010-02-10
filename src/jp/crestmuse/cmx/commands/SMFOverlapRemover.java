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

/**
SMFOverlapRemover shortens the durations of notes removes a pair of two notes having 

*/
public class SMFOverlapRemover extends 
                               CMXCommand<MIDIXMLWrapper,MIDIXMLWrapper> {

  private static final double ON_ON = 0.1;
  private static final double ON_OFF = 0.5;
  private static final int OFF_ON = -1;
  String outSMFFileName;
  String outSCCFileName;

  MIDIEventWithTime[][] last1 = new MIDIEventWithTime[128][16];
  MIDIEventWithTime[][] last2 = new MIDIEventWithTime[128][16];

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

  //  MIDIEvent[] ary = new MIDIEvent[128];
//  NoteCompatible[] noteary = new NoteCompatible[128];
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
//      for (MIDIEventWithTime[] etary : last1)
//        for (MIDIEventWithTime et : etary)
//          if (et != null) l.add(et);
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
      last2[nn][ch] = last1[nn][ch];
      last1[nn][ch] = e;
    } else if (isNoteOff(e)) {
      int nn = e.evt.value(0);
      int ch = e.evt.channel();
      if(last2[nn][ch] != null && last1[nn][ch] != null 
	 && isNoteOn(last2[nn][ch]) && isNoteOn(last1[nn][ch])) {
        if(last1[nn][ch].time - last2[nn][ch].time > div * ON_ON 
	   && e.time - last1[nn][ch].time < div * ON_OFF) {
          e.time = last1[nn][ch].time + OFF_ON;
          last2[nn][ch] = e;
        } else {
          last2[nn][ch] = last1[nn][ch];
          last1[nn][ch] = e;
        }
      } else {
        last2[nn][ch] = last1[nn][ch];
        last1[nn][ch] = e;
      }
    }
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
    SMFOverlapRemover sor = new SMFOverlapRemover();
    try {
      sor.start(args);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }
}
