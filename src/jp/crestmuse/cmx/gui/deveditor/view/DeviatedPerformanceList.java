package jp.crestmuse.cmx.gui.deveditor.view;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;

import jp.crestmuse.cmx.filewrappers.CMXFileWrapper;
import jp.crestmuse.cmx.filewrappers.DeviationInstanceWrapper;
import jp.crestmuse.cmx.filewrappers.MusicXMLWrapper;
import jp.crestmuse.cmx.gui.deveditor.controller.CommandInvoker;
import jp.crestmuse.cmx.gui.deveditor.controller.DeviatedNoteControler;
import jp.crestmuse.cmx.gui.deveditor.model.DeviatedPerformance;

public class DeviatedPerformanceList extends JList {

  private DefaultListModel model;

  public DeviatedPerformanceList() {
    model = new DefaultListModel();
    setModel(model);
  }

  public void addPerformance(String fileName, JScrollPane notelistParent,
      ListElementLoadListener listener) {
    model.addElement(new ListElement(fileName, notelistParent, listener));
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
    private boolean loading;

    private ListElement(final String filename,
        final JScrollPane notelistParent, final ListElementLoadListener listener) {
      Thread t = new Thread() {
        public void run() {
          try {
            CMXFileWrapper wrapper = CMXFileWrapper.readfile(filename);
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
            fileName = wrapper.getFileName();
            deviatedPerformance = new DeviatedPerformance(dev);
            commandInvoker = new CommandInvoker();
            DeviatedNoteControler dnc = new DeviatedNoteControler(
                commandInvoker);
            pianoRollPanel = new PianoRollPanel(deviatedPerformance, dnc);
            curvesPanel = new CurvesPanel(deviatedPerformance, pianoRollPanel);
            velocityPanel = new VelocityPanel(deviatedPerformance,
                pianoRollPanel, dnc);
            noteList = new NoteList(deviatedPerformance, dnc, notelistParent);
            noteEditPanel = new NoteEditPanel(dnc);

            deviatedPerformance.addListener(pianoRollPanel);
            deviatedPerformance.addListener(velocityPanel);
            deviatedPerformance.addListener(noteList);
            deviatedPerformance.addListener(noteEditPanel);
            loading = false;
            listener.listElementLoaded();
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      };
      t.start();
      fileName = "loading...";
      loading = true;
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

    public boolean isLoading() {
      return loading;
    }
  }

  public interface ListElementLoadListener {
    public void listElementLoaded();
  }

}
