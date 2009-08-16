package jp.crestmuse.cmx.gui.deveditor.view;

import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;

import jp.crestmuse.cmx.filewrappers.CMXFileWrapper;
import jp.crestmuse.cmx.filewrappers.DeviationInstanceWrapper;
import jp.crestmuse.cmx.filewrappers.MusicXMLWrapper;
import jp.crestmuse.cmx.gui.deveditor.controller.CommandInvoker;
import jp.crestmuse.cmx.gui.deveditor.model.DeviatedPerformance;

public class DeviatedPerformanceView {

  public static int ROW_HEADER_WIDTH = 64;
  private DeviatedPerformance deviatedPerformance;
  private PianoRollPanel pianoRollPanel;
  private CommandInvoker commandInvoker;

  public DeviatedPerformanceView(String fileName) throws IOException, InvalidMidiDataException {
    CMXFileWrapper wrapper = CMXFileWrapper.readfile(fileName);
    DeviationInstanceWrapper dev;
    try {
      dev = DeviationInstanceWrapper.createDeviationInstanceFor((MusicXMLWrapper)wrapper);
      dev.finalizeDocument();
    } catch(ClassCastException e) {
      try{
        dev = (DeviationInstanceWrapper)wrapper;
      }catch(ClassCastException e1){
        throw new IllegalArgumentException("argument must be MusicXMLWrapper or DeviationInstanceWrapper");
      }
    }
    deviatedPerformance = new DeviatedPerformance(dev);
    pianoRollPanel = new PianoRollPanel(deviatedPerformance);
    commandInvoker = new CommandInvoker();
  }

  public DeviatedPerformance getDeviatedPerformance() {
    return deviatedPerformance;
  }

  public PianoRollPanel getPianoRollPanel() {
    return pianoRollPanel;
  }

  public void updateScale() {
    pianoRollPanel.updateScale();
  }

  public void updateNotes() {
    pianoRollPanel.updateNotes();
  }

}
