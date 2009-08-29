package jp.crestmuse.cmx.gui.deveditor.view;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import jp.crestmuse.cmx.gui.deveditor.controller.DeviatedNoteControler;
import jp.crestmuse.cmx.gui.deveditor.controller.DeviatedNoteListener;
import jp.crestmuse.cmx.gui.deveditor.model.DeviatedPerformance;
import jp.crestmuse.cmx.gui.deveditor.model.DeviatedPerformance.DeviatedNote;

public class NoteList extends JList implements MouseListener, DeviatedNoteListener {

  private HashMap<DeviatedNote, Integer> dn2index;
  private JScrollPane parent;
  private DeviatedNoteControler deviatedNoteControler;
  private DefaultListModel model;
  private boolean selectByList = false;

  public NoteList(DeviatedPerformance deviatedPerformance,
      DeviatedNoteControler deviatedNoteControler, JScrollPane parent) {
    deviatedNoteControler.addDeviatedNoteListener(this);
    addMouseListener(this);
    this.deviatedNoteControler = deviatedNoteControler;
    this.parent = parent;
    model = new DefaultListModel();
    setModel(model);
    dn2index = new HashMap<DeviatedNote, Integer>();
    for (DeviatedNote dn : deviatedPerformance.getDeviatedNotes()) {
      dn2index.put(dn, model.getSize());
      model.addElement(new ListElement(dn));
    }
  }

  public void mouseClicked(MouseEvent e) {
  }

  public void mouseEntered(MouseEvent e) {
  }

  public void mouseExited(MouseEvent e) {
  }

  public void mousePressed(MouseEvent e) {
    selectByList = true;
    int index = locationToIndex(e.getPoint());
    ListElement le = (ListElement)model.get(index);
    deviatedNoteControler.select(le.dn);
    selectByList = false;
  }

  public void mouseReleased(MouseEvent e) {
  }

  public void noteSelected(DeviatedNote selectedNote) {
    if(selectByList) return;
    setSelectedIndex(dn2index.get(selectedNote));
    parent.getViewport().setViewPosition(indexToLocation(getSelectedIndex()));
  }

  public void noteUpdated(DeviatedNote updatedNote) {
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
