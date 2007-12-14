package jp.crestmuse.cmx.commands;

import javax.sound.midi.*;
import java.io.*;
import java.util.*;

public class SMFComparator {
  private static final int LENGTH_IN_SEC = 10 * 60;
  private static final int TICKS_PER_BEAT = 480;
  private static final int TEMPO = 120;
  private static final int TIME_THRESHOLD = 5;
  private static final int VEL_THRESHOLD = 1;

  private static Track record(String filename) throws Exception {
    Sequencer sequencer1 = MidiSystem.getSequencer(true);
    Transmitter trans = sequencer1.getTransmitter();
    Sequence sequence1 = MidiSystem.getSequence(new File(filename));
    sequencer1.open();
    sequencer1.setSequence(sequence1);
    Sequencer sequencer2 = MidiSystem.getSequencer(false);
    Receiver recv = sequencer2.getReceiver();
    trans.setReceiver(recv);
    Sequence sequence2 = new Sequence(Sequence.PPQ, TICKS_PER_BEAT);
    sequencer2.open();
    Track tr = sequence2.createTrack();
    if (tr.size() >= 1)
      tr.get(0).setTick(LENGTH_IN_SEC * TICKS_PER_BEAT);
    sequencer2.setSequence(sequence2);
    sequencer2.setTempoInBPM(TEMPO);
    sequencer2.recordEnable(tr, -1);
    sequencer2.startRecording();
    sequencer1.start();
    while (sequencer1.isRunning()) {
      Thread.currentThread().sleep(100);
    }
    sequencer2.stop();
    sequencer1.close();
    sequencer2.close();
    return tr;
  }

  private static boolean[] isOn;

  private static ArrayList<NoteEvent> getNoteEventList(Track tr) {
    ArrayList<NoteEvent> list = new ArrayList<NoteEvent>();
    isOn = new boolean[128];
    int size = tr.size();
    for (int i = 0; i < size; i++) {
      try {
        NoteEvent ne = new NoteEvent(tr.get(i), 
                                     i == 0 ? tr.get(0).getTick() : 
                                     tr.get(i-1).getTick());
        list.add(ne);
      } catch (IllegalStateException e) { }
    }
    return list;
  }

  private static void compare(Track tr1, Track tr2) {
    ArrayList<NoteEvent> list1 = getNoteEventList(tr1);
    ArrayList<NoteEvent> list2 = getNoteEventList(tr2);
    int size1 = list1.size();
    int size2 = list2.size();
    int i = 0, j = 0;
    while (i < size1 && j < size2) {
      if (list1.get(i).approxEquals(list2.get(j))) {
        System.out.println("[" + list1.get(i) + "] || [" + list2.get(j) + 
                           "]  ==> ok");
        i++;
        j++;
      } else if (j < size2-1 && list1.get(i).approxEquals(
                   new NoteEvent(list2.get(j).delta, list2.get(j+1)))) {
        System.out.println("[null]                           || [" 
                           + list2.get(j) + "] ==> diff");
        j++;
      } else if (i < size1-1 && list2.get(j).approxEquals(
                   new NoteEvent(list1.get(i).delta, list1.get(i+1)))) {
        System.out.println("[" + list1.get(i) + 
                           "] || [null]                           ==> diff");
        i++;
      } else {
        System.out.println("[" + list1.get(i) + "] || [" + list2.get(j) + 
                           "]  ==> diff");
        i++;
        j++;
      }
    }
    for ( ; i < size1; i++)
      System.out.println("[" + list1.get(i) + "] || [null] ==> diff");
    for ( ; j < size2; j++)
        System.out.println("[null] || [" + list2.get(j) + "] ==> diff");
  }

  private static class NoteEvent {
    private long tick = 0;
    private long delta = 0;
    private String status = "";
    private byte notenum = 0;
    private byte vel = 0;
    private String text;
    private NoteEvent(MidiEvent evt, long prevTick) {
      tick = evt.getTick();
      delta = tick - prevTick;
      MidiMessage msg = evt.getMessage();
      if ((msg.getStatus() & 0xF0) == 0x90 && (msg.getMessage()[2]==0)
          || (msg.getStatus() & 0xF0) == 0x80) {
        status = "NOTE_OFF";
        notenum = msg.getMessage()[1];
        vel = 0;
      } else if ((msg.getStatus() & 0xF0) == 0x90) {
        status = "NOTE_ON ";
        notenum = msg.getMessage()[1];
        vel = msg.getMessage()[2];
      } else {
        throw new IllegalStateException();
      }
      text = String.format("%4d", delta) 
        + " (" + String.format("%6d", tick)  + ") " 
        + status + " " + String.format("%3d", notenum) + " " 
        + String.format("%3d", vel);
    }
    private NoteEvent(long baseDelta, NoteEvent ne) {
      tick = ne.tick;
      delta = baseDelta + ne.delta;
      status = ne.status;
      notenum = ne.notenum;
      vel = ne.vel;
      text = ne.text;
    }
    private boolean approxEquals(NoteEvent e) {
      return (e != null) 
        && (Math.abs(delta - e.delta) <= TIME_THRESHOLD) 
        && (status.equals(e.status))
        && (notenum == e.notenum) 
        && (Math.abs(vel - e.vel) <= VEL_THRESHOLD);
    }
    public String toString() {
      return text;
    }
  }


  public static void main(String[] args) {
    try {
      Track tr1 = record(args[0]);
      Track tr2 = record(args[1]);
      compare(tr1, tr2);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }
}
    
