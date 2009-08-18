package jp.crestmuse.cmx.gui.deveditor.view;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;
import javax.swing.JPanel;

import jp.crestmuse.cmx.filewrappers.CMXFileWrapper;
import jp.crestmuse.cmx.filewrappers.DeviationInstanceWrapper;
import jp.crestmuse.cmx.filewrappers.MusicXMLWrapper;
import jp.crestmuse.cmx.gui.deveditor.controller.CommandInvoker;
import jp.crestmuse.cmx.gui.deveditor.model.DeviatedPerformance;

public class DeviatedPerformanceView {

  public static int ROW_HEADER_WIDTH = 64;
  private DeviatedPerformance deviatedPerformance;
  private PianoRollPanel pianoRollPanel;
  private CurvesPanel tempoPanel;
  private VelocityPanel velocityPanel;
  private NoteList noteList;
  private NoteEditPanel noteEditPanel;
  private CommandInvoker commandInvoker;
  private String fileName;
  private static int ID_COUNTER = 0;
  private int id;

  public DeviatedPerformanceView(String fileName) throws IOException, InvalidMidiDataException {
    CMXFileWrapper wrapper = CMXFileWrapper.readfile(fileName);
    this.fileName = wrapper.getFileName();
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
    tempoPanel = new CurvesPanel(deviatedPerformance, pianoRollPanel);
    velocityPanel = new VelocityPanel(deviatedPerformance, pianoRollPanel);
    noteList = new NoteList(deviatedPerformance);
    noteEditPanel = new NoteEditPanel();
    commandInvoker = new CommandInvoker();
    id = ID_COUNTER;
    ID_COUNTER++;
  }

  public DeviatedPerformance getDeviatedPerformance() {
    return deviatedPerformance;
  }

  public PianoRollPanel getPianoRollPanel() {
    return pianoRollPanel;
  }

  public CurvesPanel getTempoPanel() {
    return tempoPanel;
  }

  public VelocityPanel getVelocityPanel() {
    return velocityPanel;
  }

  public NoteList getNoteList() {
    return noteList;
  }

  public NoteEditPanel getNoteEditPanel() {
    return noteEditPanel;
  }

  public void updateScale() {
    pianoRollPanel.updateScale();
    tempoPanel.updateScale();
    velocityPanel.updateScale();
  }

  public void updateNotes() {
    pianoRollPanel.updateNotes();
  }

  public String getID() {
    return "dp" + id;
  }

  public String toString() {
    return fileName;
  }

}
