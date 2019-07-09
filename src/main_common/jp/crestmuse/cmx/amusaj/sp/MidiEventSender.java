package jp.crestmuse.cmx.amusaj.sp;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

import jp.crestmuse.cmx.sound.TickTimer;

import static jp.crestmuse.cmx.amusaj.sp.MidiEventWithTicktime.createControlChangeEvent;
import static jp.crestmuse.cmx.amusaj.sp.MidiEventWithTicktime.createNoteOffEvent;
import static jp.crestmuse.cmx.amusaj.sp.MidiEventWithTicktime.createNoteOnEvent;
import static jp.crestmuse.cmx.amusaj.sp.MidiEventWithTicktime.createProgramChangeEvent;

public class MidiEventSender extends SPModule {
  private TickTimer tt = null;
  private BlockingQueue src_queue = new SynchronousQueue();

  public MidiEventSender() {

  }

  public void setTickTimer(TickTimer tt) {
    this.tt = tt;
  }

  public void sendNoteOn(long position, int ch, int nn, int vel) {
    try {
      src_queue.put(createNoteOnEvent(position, ch, nn, vel));
    } catch (InterruptedException e) {
    }
  }

  public void sendNoteOnDelayed(final long position, final int ch, 
                                final int nn, final int vel, final long delay) {
    Thread th = new Thread() {
        public void run() {
          try {
            sleep(delay);
            sendNoteOn(position, ch, nn, vel);
          } catch (InterruptedException e) {}
        }
      };
    th.start();
  }

  public void sendNoteOff(long position, int ch, int nn, int vel) {
    try {
      src_queue.put(createNoteOffEvent(position, ch, nn, vel));
    } catch (InterruptedException e) {
    }
  }

  public void sendNoteOffDelayed(final long position, final int ch, 
                                 final int nn, final int vel, 
                                 final long delay) {
    Thread th = new Thread() {
        public void run() {
          try { 
            sleep(delay);
            sendNoteOff(position, ch, nn, vel);
          } catch (InterruptedException e) {}
        }
      };
    th.start();    
  }

  public void sendControlChange(long position, int ch, int type, int value) {
    try {
      src_queue.put(createControlChangeEvent(position, ch, type, value));
    } catch (InterruptedException e) {
    }
  }

  public void sendControlChangeDelayed(final long position, final int ch, 
                                       final int type, final int value, 
                                       final long delay) {
    Thread th = new Thread() {
        public void run() {
          try {
            sleep(delay);
            sendControlChange(position, ch, type, value);
          } catch (InterruptedException e) {}
        }
      };
    th.start();
  }

  public void sendProgramChange(long position, int ch, int value) {
    try {
      src_queue.put(createProgramChangeEvent(position, ch, value));
    } catch (InterruptedException e) {
    }
  }

  public void sendProgramChangeDelayed(final long position, final int ch, 
                                       final int value, final long delay) {
    Thread th = new Thread() {
        public void run() {
          try { 
            sleep(delay);
            sendProgramChange(position, ch, value);
          } catch (InterruptedException e) {}
        }
      };
    th.start();
  }
    

  public void execute(Object[] src, TimeSeriesCompatible[] dest)
    throws InterruptedException {
    while (true) {
      dest[0].add(src_queue.take());
    } 
  }

  public Class[] getInputClasses() {
    return new Class[0];
  }

  public Class[] getOutputClasses() {
    return new Class[]{MidiEventWithTicktime.class};
  }
}