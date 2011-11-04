package jp.crestmuse.cmx.amusaj.sp;

import jp.crestmuse.cmx.amusaj.filewrappers.*;
import jp.crestmuse.cmx.sound.*;
import javax.sound.midi.*;
import static jp.crestmuse.cmx.amusaj.sp.MidiEventWithTicktime.*;
import java.util.*;
import java.util.concurrent.*;

public class MidiEventSender extends SPModule {
  private TickTimer tt = null;
  private BlockingQueue src_queue = new SynchronousQueue();

  public MidiEventSender() {

  }

  public void setTickTimer(TickTimer tt) {
    this.tt = tt;
  }

  public void sendNoteOn(long position, int ch, int nn, int vel) 
    throws InvalidMidiDataException {
    src_queue.add(createNoteOnEvent(position, ch, nn, vel));
  }

  public void sendNoteOff(long position, int ch, int nn, int vel) 
    throws InvalidMidiDataException {
    src_queue.add(createNoteOffEvent(position, ch, nn, vel));
  }

  public void sendControlChange(long position, int ch, int type, int value)
    throws InvalidMidiDataException {
    src_queue.add(createControlChangeEvent(position, ch, type, value));
  }

  public void sendProgramChange(long position, int ch, int value) 
    throws InvalidMidiDataException {
    src_queue.add(createProgramChangeEvent(position, ch, value));
  }

  public void execute(Object[] src, TimeSeriesCompatible[] dest)
    throws InterruptedException {
    dest[0].add(src_queue.take());
  }

  public Class[] getInputClasses() {
    return new Class[0];
  }

  public Class[] getOutputClasses() {
    return new Class[]{MidiEventWithTicktime.class};
  }
}