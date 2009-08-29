package jp.crestmuse.cmx.gui.deveditor.view;

import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;
import javax.swing.DefaultListModel;
import javax.swing.JList;

import jp.crestmuse.cmx.filewrappers.CMXFileWrapper;
import jp.crestmuse.cmx.filewrappers.DeviationInstanceWrapper;
import jp.crestmuse.cmx.filewrappers.MusicXMLWrapper;
import jp.crestmuse.cmx.gui.deveditor.controller.CommandInvoker;
import jp.crestmuse.cmx.gui.deveditor.model.DeviatedPerformance;

public class DeviatedPerformanceList extends JList {

  private DefaultListModel model;

  public DeviatedPerformanceList() {
    model = new DefaultListModel();
    setModel(model);
  }

  public void addPerformance(String fileName) throws IOException,
      InvalidMidiDataException {
    model.addElement(new ListElement(fileName));
  }

  public ListElement getSelectedValue() {
    return (ListElement) super.getSelectedValue();
  }

  public class ListElement {

    private String fileName;
    private DeviatedPerformance deviatedPerformance;
    private CommandInvoker commandInvoker;
    private PianoRollPanel pianoRollPanel;
    private CurvesPanel curvesPanel;
    private VelocityPanel velocityPanel;
    private NoteList noteList;
    private NoteEditPanel noteEditPanel;

    private ListElement(String fileName) throws IOException,
        InvalidMidiDataException {
      CMXFileWrapper wrapper = CMXFileWrapper.readfile(fileName);
      DeviationInstanceWrapper dev;
      try {
        dev = DeviationInstanceWrapper.createDeviationInstanceFor((MusicXMLWrapper) wrapper);
        dev.finalizeDocument();
      } catch (ClassCastException e) {
        try {
          dev = (DeviationInstanceWrapper) wrapper;
        } catch (ClassCastException e1) {
          throw new IllegalArgumentException(
              "argument must be MusicXMLWrapper or DeviationInstanceWrapper");
        }
      }
      this.fileName = wrapper.getFileName();
      deviatedPerformance = new DeviatedPerformance(dev);
      commandInvoker = new CommandInvoker();
      pianoRollPanel = new PianoRollPanel(deviatedPerformance);
      curvesPanel = new CurvesPanel(deviatedPerformance, pianoRollPanel);
      velocityPanel = new VelocityPanel(deviatedPerformance, pianoRollPanel);
      noteList = new NoteList(deviatedPerformance);
      noteEditPanel = new NoteEditPanel();
    }

    public String toString() {
      return fileName;
    }

    public String getName() {
      return fileName;
    }

    public DeviatedPerformance getDeviatedPerformance() {
      return deviatedPerformance;
    }

    public CommandInvoker getCommandInvoker() {
      return commandInvoker;
    }

    public PianoRollPanel getPianoRollPanel() {
      return pianoRollPanel;
    }

    public CurvesPanel getCurvesPanel() {
      return curvesPanel;
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

  }

}
