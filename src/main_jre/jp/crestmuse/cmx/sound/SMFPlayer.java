package jp.crestmuse.cmx.sound;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Transmitter;

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

  public SMFPlayer(MidiDevice mididev) throws MidiUnavailableException {
    sequencer = MidiSystem.getSequencer(false);
//    if (!(sequencer instanceof Synthesizer)) {
//      Synthesizer synthesizer = MidiSystem.getSynthesizer();
//      synthesizer.open();
      Receiver receiverSyntheFromSeq = mididev.getReceiver();
      Transmitter transmitterSeqToSynthe = sequencer.getTransmitter();
      transmitterSeqToSynthe.setReceiver(receiverSyntheFromSeq);
//    }
    sequencer.open();
  }

  public Sequencer getSequencer() {
    return sequencer;
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
    sequence = MidiSystem.getSequence(new BufferedInputStream(instream));
    //sequence = MidiSystem.getSequence(instream);
    sequencer.setSequence(sequence);
  }

  public void readSMF(Sequence s) throws InvalidMidiDataException {
    stop();
    sequence = s;
    sequencer.setSequence(s);
  }

  /** play SMF file */
  public void play() {
    if (sequencer.getSequence() != null) {
      sequencer.start();
      System.err.println("(SMFPlayer) start playing...." + sequencer.getSequence().getMicrosecondLength());
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
  
  public void setLoopEnabled(boolean b) {
    throw new UnsupportedOperationException("not implemented yet");
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

  public int getTicksPerBeat() {
    return sequence.getResolution();
  }

  public long getTickPosition() {
    return sequencer.getTickPosition();
  }
    
    public void setTickPosition(long tick) {
	sequencer.setTickPosition(tick);
    }

    public float getTempoInBPM() {
	return sequencer.getTempoInBPM();
    }

    public void setTempoInBPM(float t) {
	sequencer.setTempoInBPM(t);
    }

    public void setTempoInBPM(double t) {
	setTempoInBPM((float)t);
    }

}
