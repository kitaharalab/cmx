package jp.crestmuse.cmx.amusaj.sp;
import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.sound.*;
import static jp.crestmuse.cmx.misc.MIDIConst.*;
import java.io.*;

public class MidiRecorder2 extends SPModule {

  private double tempo = 120.0;
  private TickTimer tt;
  private long prevTick = 0;
  private MIDIXMLWrapper midixml =  null;
  
  public MidiRecorder2(TickTimer tt) {
    this.tt = tt;
  }

  public void setTempo(double tempo) {
    this.tempo = tempo;
  }
  
  public void start() {
    prevTick = tt.getTickPosition();
    try {
      midixml =
        (MIDIXMLWrapper)CMXFileWrapper.createDocument(MIDIXMLWrapper.TOP_TAG);
    } catch (InvalidFileTypeException e) {
      e.printStackTrace();
      throw new XMLException(e);
    }
    midixml.addElementsFirstForFormat1(1, tt.getTicksPerBeat());
    midixml.newTrack(0);
    midixml.addMetaEvent("SetTempo", 0,
                         (int)(1000 * 1000 * 60 / tempo));
  }
    
  public void stop() {
    midixml.endTrack();
    try {
      midixml.finalizeDocument();
    } catch (IOException e) {
      e.printStackTrace();
      throw new XMLException(e);
    }
  }
  
  public void execute(Object[] src, TimeSeriesCompatible[] dst)
    throws InterruptedException {
    if (midixml != null) {
      MidiEventWithTicktime e = (MidiEventWithTicktime)src[0];
      byte[] msg = e.getMessage().getMessage();
      byte ch = (byte)(msg[0] & 0x0F);
      short status = (short)(msg[0] & 0xF0);
      System.err.println(status);
      String msgtype = statusNoToMsgName(status);
      if (msgtype != null) {
        System.err.println(msgtype);
        String[] attlist = getAttributeList(msgtype);
        System.err.println(attlist);
        int[] values = new int[attlist.length];
        int len = getByteLength(msgtype);
        if (len == 1) {
          for (int i = 0; i < attlist.length; i++) {
            values[i] = msg[i+1];
          }
        } else {
          System.err.println("Unsupported MIDI channel message");
        }
        midixml.addMIDIChannelMessage(msgtype, (int)(e.music_position - prevTick),
                                      ++ch, values);
        prevTick = e.music_position;
      }
    }
  }

  public MIDIXMLWrapper getMIDIXML() {
    return midixml;
  }

  public Class[] getInputClasses() {
    return new Class[]{MidiEventWithTicktime.class};
  }

  public Class[] getOutputClasses() {
    return new Class[0];
  }
}
