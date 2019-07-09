package jp.crestmuse.cmx.amusaj.sp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;

//import jp.crestmuse.cmx.amusaj.filewrappers.TimeSeriesCompatible;

public class MidiSequenceSender extends SPModule {

  private final Sequencer sequencer;
  private final BlockingQueue<MidiEvent> queue = new LinkedBlockingQueue<MidiEvent>();

  public MidiSequenceSender(String smfName) throws MidiUnavailableException,
      InvalidMidiDataException, IOException {
    this(MidiSystem.getSequence(new File(smfName)));
  }

  public MidiSequenceSender(InputStream stream)
      throws MidiUnavailableException, InvalidMidiDataException, IOException {
    this(MidiSystem.getSequence(stream));
  }

  private MidiSequenceSender(Sequence sequence)
      throws MidiUnavailableException, InvalidMidiDataException {
    sequencer = MidiSystem.getSequencer();
    getSequencer().getTransmitter().setReceiver(new Receiver() {

      public void send(MidiMessage message, long timeStamp) {
        queue.add(new MidiEventWithTicktime(message, timeStamp, timeStamp));
      }

      public void close() {
      }

    });
    getSequencer().setSequence(sequence);
    getSequencer().open();
  }

  public void execute(Object[] src, TimeSeriesCompatible[] dest)
      throws InterruptedException {
    dest[0].add(queue.take());
  }

  public Class[] getInputClasses() {
    return new Class[0];
  }

  public Class[] getOutputClasses() {
    return new Class[] { MidiEventWithTicktime.class };
  }

  public void start() {
    getSequencer().start();
  }

  public Sequencer getSequencer() {
    return sequencer;
  }

/*
  public static void main(String[] args) throws MidiUnavailableException,
      InvalidMidiDataException, IOException {
    SPExecutor sp = new SPExecutor();
    MidiSequenceSender mss = new MidiSequenceSender(args[0]);
    SPModule printModule = new SPModule() {

      public Class[] getOutputClasses() {
        return new Class[0];
      }

      public Class[] getInputClasses() {
        return new Class[] { MidiEvent.class };
      }

      public void execute(Object[] src, TimeSeriesCompatible[] dest)
          throws InterruptedException {
        MidiEvent e = (MidiEvent) src[0];
        System.out.println(Arrays.toString(e.getMessage().getMessage()));
      }
    };
    sp.addSPModule(mss);
    sp.addSPModule(printModule);
    sp.connect(mss, 0, printModule, 0);
    sp.start();
    mss.start();
  }
*/

}
