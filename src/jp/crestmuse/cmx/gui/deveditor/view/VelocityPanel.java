package jp.crestmuse.cmx.gui.deveditor.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import jp.crestmuse.cmx.gui.deveditor.controller.DeviatedNoteControler;
import jp.crestmuse.cmx.gui.deveditor.controller.DeviatedNoteListener;
import jp.crestmuse.cmx.gui.deveditor.model.DeviatedPerformance;
import jp.crestmuse.cmx.gui.deveditor.model.DeviatedPerformance.DeviatedNote;
import static jp.crestmuse.cmx.gui.deveditor.model.DeviatedPerformance.*;
import static jp.crestmuse.cmx.gui.deveditor.view.PianoRollPanel.*;

public class VelocityPanel extends JPanel implements ChangeListener, DeviatedNoteListener {

  public static int PANEL_HEIGTH = 100;
  private PianoRollPanel pianoRollPanel;
  private ArrayList<NoteVelocity> velocities;
  private HashMap<DeviatedNote, NoteVelocity> dn2nv;
  private NoteVelocity selectedNoteVelocity;
  private DeviatedNoteControler deviatedNoteControler;

  public VelocityPanel(DeviatedPerformance deviatedPerformance,
      PianoRollPanel pianoRollpanel, DeviatedNoteControler deviatedNoteControler) {
    this.pianoRollPanel = pianoRollpanel;
    this.deviatedNoteControler = deviatedNoteControler;
    this.deviatedNoteControler.addDeviatedNoteListener(this);
    velocities = new ArrayList<NoteVelocity>();
    dn2nv = new HashMap<DeviatedNote, NoteVelocity>();
    for (DeviatedNote dn : deviatedPerformance.getDeviatedNotes()) {
      NoteVelocity nv = new NoteVelocity(dn);
      velocities.add(nv);
      dn2nv.put(dn, nv);
    }
    updateScale();
  }

  public void updateScale() {
    int width = pianoRollPanel.getPreferredSize().width;
    setPreferredSize(new Dimension(width, PANEL_HEIGTH));
    for (NoteVelocity nv : velocities)
      nv.updateScale();
  }

  public void stateChanged(ChangeEvent e) {
    updateScale();
    repaint();
  }

  public void noteSelected(DeviatedNote selectedNote) {
    selectedNoteVelocity = dn2nv.get(selectedNote);
    repaint();
  }

  public void noteUpdated(DeviatedNote updatedNote) {
  }

  public void paint(Graphics g) {
    super.paint(g);
    for (NoteVelocity nv : velocities)
      nv.paint(g);
    if(selectedNoteVelocity != null)
      selectedNoteVelocity.paintAsSelected(g);
  }

  private class NoteVelocity {
    DeviatedNote deviatedNote;
    int x1, x2, y;
    String velocity;

    NoteVelocity(DeviatedNote dn) {
      deviatedNote = dn;
      updateScale();
    }

    void updateScale() {
      x1 = deviatedNote.onset() * WIDTH_PER_BEAT / TICKS_PER_BEAT;
      x2 = deviatedNote.offset() * WIDTH_PER_BEAT / TICKS_PER_BEAT;
      y = deviatedNote.velocity();
      velocity = deviatedNote.velocity() + "";
    }

    void paint(Graphics g) {
      g.setColor(Color.BLACK);
      g.drawLine(x1, getHeight(), x1, getHeight() - y);
      g.drawLine(x1, getHeight() - y, x2, getHeight() - y);
      g.drawLine(x2, getHeight() - y, x2, getHeight());
    }

    void paintAsSelected(Graphics g) {
      g.setColor(Color.RED);
      g.drawLine(x1, getHeight(), x1, getHeight() - y);
      g.drawLine(x1, getHeight() - y, x2, getHeight() - y);
      g.drawLine(x2, getHeight() - y, x2, getHeight());
      g.drawString(velocity, x1, getHeight() - y);      
    }
  }

  public static class RowHeader extends JPanel {
    public RowHeader() {
      setPreferredSize(new Dimension(CurvesPanel.ROW_HEADER_WIDTH,
          1));
      add(new JLabel("velocity"));
    }
  }

}
