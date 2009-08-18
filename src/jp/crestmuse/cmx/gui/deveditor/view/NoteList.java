package jp.crestmuse.cmx.gui.deveditor.view;

import java.util.HashMap;

import javax.swing.DefaultListModel;
import javax.swing.JList;

import jp.crestmuse.cmx.gui.deveditor.model.DeviatedPerformance;
import jp.crestmuse.cmx.gui.deveditor.model.DeviatedPerformance.DeviatedNote;

public class NoteList extends JList {

  private HashMap<DeviatedNote, Integer> dn2index;

  public NoteList(DeviatedPerformance deviatedPerformance) {
    DefaultListModel model = new DefaultListModel();
    setModel(model);
    dn2index = new HashMap<DeviatedNote, Integer>();
    for(DeviatedNote dn : deviatedPerformance.getDeviatedNotes()) {
      dn2index.put(dn, model.getSize());
      model.addElement(new ListElement(dn));
    }
  }

  private class ListElement {
    DeviatedNote dn;
    ListElement(DeviatedNote dn) {
      this.dn = dn;
    }
    public String toString() {
      return dn.onset() + ", " + dn.offset() + ", " + dn.notenum();
    }
  }

}
