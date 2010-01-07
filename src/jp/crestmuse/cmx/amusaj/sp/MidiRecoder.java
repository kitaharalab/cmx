package jp.crestmuse.cmx.amusaj.sp;
import java.io.*;
import javax.sound.midi.*;
import jp.crestmuse.cmx.amusaj.filewrappers.*;

public class MidiRecoder extends SPModule {

  private int ticksPerBeat;
  private String filename;
  private Sequence seq;
  private Track track;

  public MidiRecoder(String filename, int ticksPerBeat) {
    try {
      seq = new Sequence(Sequence.PPQ, ticksPerBeat);
      track = seq.createTrack();
    } catch (InvalidMidiDataException e) {
      e.printStackTrace();
    }
    this.filename = filename;
    this.ticksPerBeat = ticksPerBeat;
  }

  public void execute(Object[] src, TimeSeriesCompatible[] dest)
    throws InterruptedException {
    MidiEventWithTicktime e = (MidiEventWithTicktime)src[0];
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
              