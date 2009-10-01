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

  private void removePerformance(ListElement le) {
    model.removeElement(le);
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

    private ListElement(String filename, JScrollPane notelistParent,
        ListElementLoadListener listener) {
      Thread t = new LoadThread(filename, notelistParent, listener);
      t.start();
      fileName = "loading...";
      loading = true;
    }

    private class LoadThread extends Thread {

      String filename;
      JScrollPane notelistParent;
      ListElementLoadListener listener;

      LoadThread(String filename, JScrollPane notelistParent,
          ListElementLoadListener listener) {
        this.filename = filename;
        this.notelistParent = notelistParent;
        this.listener = listener;
      }

      public void run() {
        Thread prog = new Thread() {
          int c = 0, v = 1;
          public void run() {
            while (loading) {
              c += v;
              if (c > 10)
                v = -1;
              else if (c < 1)
                v = 1;
              fileName = "loading";
              for (int i = 0; i < c; i++)
                fileName += " ";
              fileName += "...";
              repaint();
              try {
                Thread.sleep(100);
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
            }
          };
        };
        prog.start();
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
          deviatedPerformance = new DeviatedPerformance(dev);
          commandInvoker = new CommandInvoker();
          DeviatedNoteControler dnc = new DeviatedNoteControler(commandInvoker);
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
          deviatedPerformance.addListener(curvesPanel);
          loading = false;
          fileName = wrapper.getFileName();
          listener.listElementLoaded();
        } catch (Exception e) {
          e.printStackTrace();
          removePerformance(ListElement.this);
          listener.listElementLoadFailed();
        }
      }
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
    public void listElementLoadFailed();
  }

}
