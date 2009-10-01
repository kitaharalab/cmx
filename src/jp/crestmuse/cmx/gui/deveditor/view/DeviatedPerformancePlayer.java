package jp.crestmuse.cmx.gui.deveditor.view;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Transmitter;

import org.xml.sax.SAXException;

import jp.crestmuse.cmx.gui.deveditor.model.DeviatedPerformance;
import jp.crestmuse.cmx.sound.MusicPlayer;

/**
 * このクラスは一つのSequencerと複数のCompiledDeviationを所持し、指定された曲を演奏するクラスです．
 * 
 * @author ntotani
 */
public class DeviatedPerformancePlayer implements MusicPlayer {

  private DeviatedPerformance currentPerformance = null;
  private Sequencer sequencer = null;
  private LabeledMidiDevice[] receivers;
  private int currentReceiverIndex = 0;
  private Transmitter transmitter;

  public DeviatedPerformancePlayer() throws MidiUnavailableException {
    sequencer = MidiSystem.getSequencer();
    sequencer.open();
    transmitter = sequencer.getTransmitters().get(0);
    ArrayList<MidiDevice> devices = new ArrayList<MidiDevice>();
    for (MidiDevice.Info info : MidiSystem.getMidiDeviceInfo()) {
      try {
        MidiDevice d = MidiSystem.getMidiDevice(info);
        if(d.getMaxReceivers() != 0)
          devices.add(d);
      } catch (MidiUnavailableException e) {
        e.printStackTrace();
      }
    }
    receivers = new LabeledMidiDevice[devices.size()];
    for(int i=0; i<receivers.length; i++)
      receivers[i] = new LabeledMidiDevice(devices.get(i));
  }

  public void setReciever(int index) throws MidiUnavailableException {
    MidiDevice md = receivers[index].md;
    md.open();
    Receiver receiver = md.getReceiver();
    transmitter.setReceiver(receiver);
    currentReceiverIndex = index;
  }

  protected void finalize() throws Throwable {
    super.finalize();
    if (sequencer != null)
      sequencer.close();
  }

  public long getMicrosecondPosition() {
    if (sequencer == null)
      return 0;
    return sequencer.getMicrosecondPosition();
  }

  public int getTicksPerBeat() {
    if (sequencer == null)
      return 0;
    return sequencer.getSequence().getResolution();
  }

  public long getTickPosition() {
    if (sequencer == null)
      return 0;
    return sequencer.getTickPosition();
  }

  public boolean isNowPlaying() {
    if (sequencer == null)
      return false;
    return sequencer.isRunning();
  }

  public void play() {
    if (sequencer != null)
      sequencer.start();
  }

  public void stop() {
    if (sequencer != null)
      sequencer.stop();
  }

  public void run() {
  }

  public void reset() {
    if (sequencer != null)
      sequencer.setTickPosition(0);
  }

  public void setTickPosition(long tick) {
    if (sequencer != null)
      sequencer.setTickPosition(tick);
  }

  public void setMicrosecondPosition(long microseconds) {
    if (sequencer != null)
      sequencer.setMicrosecondPosition(microseconds);
  }

  public void changeDeviation(DeviatedPerformance newPerformance)
      throws InvalidMidiDataException {
    if (newPerformance == currentPerformance)
      return;
    try {
      long tick = sequencer.getTickPosition();
      sequencer.setSequence(newPerformance.getSequence());
      sequencer.setTickPosition(tick);
    } catch (NullPointerException e) {
    }
    currentPerformance = newPerformance;
  }

  public Sequence getCurrentSequence() {
    return currentPerformance.getSequence();
  }

  public LabeledMidiDevice[] getReceivers() {
    return receivers;
  }

  public int getCurrentReceiverIndex() {
    return currentReceiverIndex;
  }

  public void writeFile(OutputStream out) throws IOException, SAXException {
    currentPerformance.calcDeviation().write(out);
  }

  public class LabeledMidiDevice {
    private MidiDevice md;
    private LabeledMidiDevice(MidiDevice md) {
      this.md = md;
    }
    public String toString() {
      return md.getDeviceInfo().getName();
    }
  }

}
