package jp.crestmuse.cmx.gui.deveditor;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequencer;

import org.xml.sax.SAXException;

import jp.crestmuse.cmx.filewrappers.CMXFileWrapper;
import jp.crestmuse.cmx.filewrappers.DeviationInstanceWrapper;
import jp.crestmuse.cmx.filewrappers.MusicXMLWrapper;
import jp.crestmuse.cmx.filewrappers.MusicXMLWrapper.Note;
import jp.crestmuse.cmx.sound.MusicPlayer;

public class CorePlayer implements MusicPlayer {

  private ArrayList<CompiledDeviation> compiledDeviations;
  private int playingIndex = -1;
  private Sequencer sequencer;

  public CorePlayer() throws MidiUnavailableException {
    // TODO デフォルトシーケンサ取れなかった場合
    compiledDeviations = new ArrayList<CompiledDeviation>();
    sequencer = MidiSystem.getSequencer();
    sequencer.open();
  }
  
  protected void finalize() throws Throwable {
    super.finalize();
    sequencer.close();
  }

  public long getMicrosecondPosition() {
    return sequencer.getMicrosecondPosition();
  }

  public long getTickPosition() {
    return sequencer.getTickPosition();
  }

  public boolean isNowPlaying() {
    return sequencer.isRunning();
  }

  public void play() {
    sequencer.start();
  }

  public void stop() {
    sequencer.stop();
  }

  public void run() {
  }
  
  public void reset(){
    sequencer.setTickPosition(0);
  }
  
  public CompiledDeviation open(CMXFileWrapper wrapper) throws IOException, InvalidMidiDataException{
    DeviationInstanceWrapper dev;
    try{
      dev = DeviationInstanceWrapper.createDeviationInstanceFor((MusicXMLWrapper)wrapper);
      dev.finalizeDocument();
    }catch(ClassCastException e){
      try{
        dev = (DeviationInstanceWrapper)wrapper;
      }catch(ClassCastException e1){
        throw new IllegalArgumentException("argument must be MusicXMLWrapper or DeviationInstanceWrapper");
      }
    }
    CompiledDeviation cd = new CompiledDeviation(dev);
    compiledDeviations.add(cd);
    return cd;
  }

  public void changeDeviation(int index) throws InvalidMidiDataException{
    if(index == playingIndex) return;
    long tick = sequencer.getTickPosition();
    sequencer.setSequence(compiledDeviations.get(index).getSequence());
    sequencer.setTickPosition(tick);
    playingIndex = index;
  }

  public void changeNoteDeviation(Note note, double attack, double release, double dynamics, double endDynamics) throws InvalidMidiDataException {
    //compiledDeviations.get(playingIndex).changeNoteDeviation(note, attack, release, dynamics, endDynamics);
  }
  
  public void writeFile(OutputStream out) throws IOException, SAXException {
    //compiledDeviations.get(playingIndex).calcDeviation().write(out);
  }

}
