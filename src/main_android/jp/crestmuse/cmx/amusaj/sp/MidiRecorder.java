package jp.crestmuse.cmx.amusaj.sp;

import java.io.File;
import java.io.IOException;

import jp.kshoji.javax.sound.midi.InvalidMidiDataException;
import jp.kshoji.javax.sound.midi.MidiEvent;
import jp.kshoji.javax.sound.midi.MidiSystem;
import jp.kshoji.javax.sound.midi.Sequence;
import jp.kshoji.javax.sound.midi.Track;

public class MidiRecorder extends SPModule {

  private int ticksPerBeat;
  private String filename;
  private Sequence seq;
  private Track track;

  public MidiRecorder(String filename, int ticksPerBeat) {
    try {
      seq = new Sequence(Sequence.PPQ, ticksPerBeat);
      track = seq.createTrack();
    } catch (InvalidMidiDataException e) {
      e.printStackTrace();
      throw new IllegalArgumentException();
    }
    this.filename = filename;
    this.ticksPerBeat = ticksPerBeat;
  }

  public void execute(Object[] src, TimeSeriesCompatible[] dest)
    throws InterruptedException {
    MidiEventWithTicktime e = (MidiEventWithTicktime)src[0];
    System.err.println(e.getMessage());
    System.err.println(e.music_position);
    track.add(new MidiEvent(e.getMessage(), e.music_position));
  }

  public void stop() {
    try {
      MidiSystem.write(seq, 1, new File(filename));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public Class[] getInputClasses() {
    return new Class[] { MidiEventWithTicktime.class };
  }

  public Class[] getOutputClasses() {
    return new Class[0];
  }
}
              