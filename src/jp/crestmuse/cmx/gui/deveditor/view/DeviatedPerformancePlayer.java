package jp.crestmuse.cmx.gui.deveditor.view;

import java.io.IOException;
import java.io.OutputStream;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.swing.JOptionPane;

import org.xml.sax.SAXException;

import jp.crestmuse.cmx.gui.deveditor.model.DeviatedPerformance;
import jp.crestmuse.cmx.sound.MusicPlayer;

/**
 * このクラスは一つのSequencerと複数のCompiledDeviationを所持し、指定された曲を演奏するクラスです．
 * @author ntotani
 */
public class DeviatedPerformancePlayer implements MusicPlayer {

//  private ArrayList<DeviatedPerformance> compiledDeviations;
//  private int playingIndex = -1;
  private DeviatedPerformance currentPerformance = null;
  private Sequencer sequencer = null;

  public DeviatedPerformancePlayer(){
//    compiledDeviations = new ArrayList<DeviatedPerformance>();
    try {
      sequencer = MidiSystem.getSequencer();
      sequencer.open();
    } catch (MidiUnavailableException e) {
      JOptionPane.showMessageDialog(null, "can't find default sequencer");
    }
  }
  
  protected void finalize() throws Throwable {
    super.finalize();
    if(sequencer != null)
      sequencer.close();
  }

  public long getMicrosecondPosition() {
    if(sequencer == null) return 0;
    return sequencer.getMicrosecondPosition();
  }

  public int getTicksPerBeat() {
    if(sequencer == null) return 0;
    return sequencer.getSequence().getResolution();
  }

  public long getTickPosition() {
    if(sequencer == null) return 0;
    return sequencer.getTickPosition();
  }

  public boolean isNowPlaying() {
    if(sequencer == null) return false;
    return sequencer.isRunning();
  }

  public void play() {
    if(sequencer != null)
      sequencer.start();
  }

  public void stop() {
    if(sequencer != null)
      sequencer.stop();
  }

  public void run() {
  }
  
  public void reset(){
    if(sequencer != null)
      sequencer.setTickPosition(0);
  }
  
  public void setTickPosition(long tick){
    if(sequencer != null)
      sequencer.setTickPosition(tick);
  }
  
  public void setMicrosecondPosition(long microseconds){
    if(sequencer != null)
      sequencer.setMicrosecondPosition(microseconds);
  }
  
//  public DeviatedPerformance open(CMXFileWrapper wrapper) throws IOException, InvalidMidiDataException{
//    DeviationInstanceWrapper dev;
//    try{
//      dev = DeviationInstanceWrapper.createDeviationInstanceFor((MusicXMLWrapper)wrapper);
//      dev.finalizeDocument();
//    }catch(ClassCastException e){
//      try{
//        dev = (DeviationInstanceWrapper)wrapper;
//      }catch(ClassCastException e1){
//        throw new IllegalArgumentException("argument must be MusicXMLWrapper or DeviationInstanceWrapper");
//      }
//    }
//    DeviatedPerformance cd = new DeviatedPerformance(dev);
//    compiledDeviations.add(cd);
//    return cd;
//  }

  public void changeDeviation(DeviatedPerformance newPerformance) throws InvalidMidiDataException{
    if(newPerformance == currentPerformance) return;
    try {
      long tick = sequencer.getTickPosition();
      sequencer.setSequence(newPerformance.getSequence());
      sequencer.setTickPosition(tick);
    } catch (NullPointerException e) {}
    currentPerformance = newPerformance;
  }
  
  public Sequence getCurrentSequence(){
    return currentPerformance.getSequence();
  }
  
  public void writeFile(OutputStream out) throws IOException, SAXException {
    currentPerformance.calcDeviation().write(out);
  }

}
