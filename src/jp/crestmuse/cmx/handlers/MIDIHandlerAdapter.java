package jp.crestmuse.cmx.handlers;
import jp.crestmuse.cmx.filewrappers.MIDIXMLWrapper;

public class MIDIHandlerAdapter implements MIDIHandler {
  public void beginTrack(MIDIXMLWrapper.Track track, MIDIXMLWrapper w) {
    // do nothing
  }
  public void endTrack(MIDIXMLWrapper.Track track, MIDIXMLWrapper w) {
    // do nothing
  }
  public void processMIDIEvent(MIDIXMLWrapper.MIDIEvent midiEvent, 
                               MIDIXMLWrapper w) {
    // do nothing
  }
  public void beginXFKM(MIDIXMLWrapper.XFKM xfkm, MIDIXMLWrapper w) {
    // do nothing
  }
  public void endXFKM(MIDIXMLWrapper.XFKM xfkm, MIDIXMLWrapper w) {
    // do nothing
  }
}
