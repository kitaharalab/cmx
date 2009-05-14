package jp.crestmuse.cmx.sound;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Track;

public class SequencerManager implements TickTimer, Runnable {

  public static int TICKS_PER_BEAT = 480;
  private Sequencer sequencer;
  private int currentMeasure = 0;
  private Track recordTrack;
  private List<SgWithTrack> generatables;
  private String outFileName;
  private Receiver receiver;
  private boolean finish = false;

  public SequencerManager() throws MidiUnavailableException,
      InvalidMidiDataException {
    this(MidiSystem.getReceiver());
  }

  public SequencerManager(Receiver res) throws MidiUnavailableException,
      InvalidMidiDataException {
    sequencer = MidiSystem.getSequencer(false);
    sequencer.getTransmitter().setReceiver(res);
    receiver = res;
    sequencer.open();
    Sequence sequence = new Sequence(Sequence.PPQ, TICKS_PER_BEAT);
    sequencer.setSequence(sequence);
    generatables = new LinkedList<SgWithTrack>();
  }

  public long getTickPosition() {
    return sequencer.getTickPosition();
  }

  public void run() {
    for (SgWithTrack c : generatables) {
      c.sg.sendInitializingMessages(receiver);
      c.sg.changeMeasure(c.track, 0);
      currentMeasure = 1;
      c.terminal.setTick(TICKS_PER_BEAT * 4);
    }
    sequencer.start();
    boolean alive = true;
    while (alive && !finish) {
      if (sequencer.getTickPosition() >= currentMeasure * TICKS_PER_BEAT * 4
          - TICKS_PER_BEAT) {
        currentMeasure++;
        alive = false;
        for (SgWithTrack c : generatables) {
          alive |= c.sg.changeMeasure(c.track, (currentMeasure - 1)
              * TICKS_PER_BEAT * 4);
          if(c.terminal.getTick() < TICKS_PER_BEAT * 4 * currentMeasure)
            c.terminal.setTick(TICKS_PER_BEAT * 4 * currentMeasure);
        }
      }
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        break;
      }
    }
    sequencer.stop();
    if (outFileName != null) {
      try {
        MidiSystem.write(sequencer.getSequence(), 1, new File(outFileName));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    sequencer.close();
  }

  public Sequencer getSequencer() {
    return sequencer;
  }

  public void addGeneratable(SequenceGeneratable sg) {
    generatables
        .add(new SgWithTrack(sg, sequencer.getSequence().createTrack()));
    try {
      sequencer.setSequence(sequencer.getSequence());
    } catch (InvalidMidiDataException e) {
      e.printStackTrace();
    }
  }

  public void setRecording(String outFileName) {
    this.outFileName = outFileName;
  }

  public Track getRecordTrack() {
    if (recordTrack == null) {
      recordTrack = sequencer.getSequence().createTrack();
      try {
        sequencer.setSequence(sequencer.getSequence());
      } catch (InvalidMidiDataException e) {
        e.printStackTrace();
      }
    }
    return recordTrack;
  }

  public void start() {
    Thread t = new Thread(this);
    t.start();
  }

  public void stop() {
    finish = true;
  }

  private class SgWithTrack {
    SequenceGeneratable sg;
    Track track;
    MidiEvent terminal;

    SgWithTrack(SequenceGeneratable sg, Track track) {
      this.sg = sg;
      this.track = track;
      this.terminal = track.get(0);
    }
  }

}
