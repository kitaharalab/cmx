package jp.crestmuse.cmx.sound;

import java.util.LinkedList;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.SysexMessage;
import javax.sound.midi.Track;

public class SequencerManager implements TickTimer, Runnable {

  public static int TICKS_PER_BEAT = 480;
  private Sequencer sequencer;
  private int currentMeasure = 0;
  private Track dummyTrack;
  private List<SgWithTrack> generatables;

  public SequencerManager() throws MidiUnavailableException, InvalidMidiDataException{
    sequencer = MidiSystem.getSequencer(false);
    sequencer.getTransmitter().setReceiver(MidiSystem.getReceiver());
    sequencer.open();
    Sequence sequence = new Sequence(Sequence.PPQ, TICKS_PER_BEAT);
    dummyTrack = sequence.createTrack();
    dummyTrack.add(new MidiEvent(new SysexMessage(), TICKS_PER_BEAT*4));
    sequencer.setSequence(sequence);
    generatables = new LinkedList<SgWithTrack>();
  }

  public long getTickPosition() {
    return currentMeasure;
  }

  public void run() {
    sequencer.start();
    boolean alive = true;
    while(alive){
      if(sequencer.getTickPosition() >= currentMeasure*TICKS_PER_BEAT*4 - TICKS_PER_BEAT){
        currentMeasure++;
        alive = false;
        for(SgWithTrack c : generatables)
          alive |= c.sg.changeMeasure(c.track, (currentMeasure-1)*TICKS_PER_BEAT*4);
        dummyTrack.add(new MidiEvent(new SysexMessage(), (currentMeasure + 1)*TICKS_PER_BEAT*4));
      }
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        break;
      }
    }
    sequencer.close();
  }

  public void addGeneratable(SequenceGeneratable sg){
    generatables.add(new SgWithTrack(sg, sequencer.getSequence().createTrack()));
    try {
      sequencer.setSequence(sequencer.getSequence());
    } catch (InvalidMidiDataException e) {
      e.printStackTrace();
    }
  }

  public void start(){
    Thread t = new Thread(this);
    t.start();
  }

  private class SgWithTrack{
    SequenceGeneratable sg;
    Track track;
    SgWithTrack(SequenceGeneratable sg, Track track){
      this.sg = sg;
      this.track = track;
    }
  }

}
