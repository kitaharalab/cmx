package jp.crestmuse.cmx.sound;

import javax.sound.midi.*;
import java.io.*;

public class SMFPlayer implements MusicPlayer {

  Sequence sequence = null;
  Sequencer sequencer = null;

/**
 * @author Mitsuyo Hashida @ CrestMuse Project, JST
 * @version ver. 1.0 (Nov. 26, 2007)
 */
  public SMFPlayer() throws MidiUnavailableException {
    sequencer = MidiSystem.getSequencer();
    sequencer.open();
    if (!(sequencer instanceof Synthesizer)) {
      Synthesizer synthesizer = MidiSystem.getSynthesizer();
      synthesizer.open();
      Receiver receiverSyntheFromSeq = synthesizer.getReceiver();
      Transmitter transmitterSeqToSynthe = sequencer.getTransmitter();
      transmitterSeqToSynthe.setReceiver(receiverSyntheFromSeq);
    }
  }

  public void readSMF(String filename) 
    throws InvalidMidiDataException, IOException {
    readSMF(new File(filename));
  }
  
  /** read Standard Midi File */
  public void readSMF(File file) throws InvalidMidiDataException, IOException {
    stop();
    sequence = MidiSystem.getSequence(file);
    sequencer.setSequence(sequence);
  }
  
  public void readSMF(InputStream instream) throws InvalidMidiDataException, 
    IOException {
    stop();
    sequence = MidiSystem.getSequence(instream);
    sequencer.setSequence(sequence);
  }

  /** play SMF file */
  public void play() {
    if (sequencer.getSequence() != null) {
      sequencer.start();
      System.err.println("(SMFPlayer) start playing....");
    }
  }

  public void back() {
    sequencer.setTickPosition(0);
  }

  public boolean isNowPlaying() {
    return sequencer.isRunning();
  }

  public void stop() {
    if (sequencer.isRunning())
      sequencer.stop();
  }
  
  public long getMicrosecondLength(){
    return sequencer.getMicrosecondLength();
  }

  public long getMicrosecondPosition() {
    return sequencer.getMicrosecondPosition();
  }
  
  public void setMicrosecondPosition(long microseconds){
    sequencer.setMicrosecondPosition(microseconds);
  }

  public void run() {
    // do nothing
  }

  public void close() {
    this.stop();
    if (sequencer.isOpen())
      sequencer.close();
  }

  public int getResolution() {
    return sequence.getResolution();
  }

  public long getTickPosition() {
    return sequencer.getTickPosition();
  }

}
