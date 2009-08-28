package jp.crestmuse.cmx.gui.deveditor.view;

import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import jp.crestmuse.cmx.gui.deveditor.model.DeviatedPerformance;
import jp.crestmuse.cmx.gui.deveditor.model.DeviatedPerformance.DeviatedNote;
import static jp.crestmuse.cmx.gui.deveditor.model.DeviatedPerformance.*;
import static jp.crestmuse.cmx.gui.deveditor.view.PianoRollPanel.*;

public class VelocityPanel extends JPanel implements ChangeListener {

  public static int PANEL_HEIGTH = 100;
  private DeviatedPerformance deviatedPerformance;
  private PianoRollPanel pianoRollPanel;
  private ArrayList<NoteVelocity> velocities;

  public VelocityPanel(DeviatedPerformance deviatedPerformance,
      PianoRollPanel pianoRollpanel) {
    this.deviatedPerformance = deviatedPerformance;
    this.pianoRollPanel = pianoRollpanel;
    velocities = new ArrayList<NoteVelocity>();
    for (DeviatedNote dn : deviatedPerformance.getDeviatedNotes())
      velocities.add(new NoteVelocity(dn));
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

  public void paint(Graphics g) {
    super.paint(g);
    for (NoteVelocity nv : velocities)
      nv.paint(g);
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
      g.drawLine(x1, getHeight(), x1, getHeight() - y);
      g.drawLine(x1, getHeight() - y, x2, getHeight() - y);
      g.drawLine(x2, getHeight() - y, x2, getHeight());
//      g.drawString(notenum, x1, y);
    }
  }

  public static class RowHeader extends JPanel {
    public RowHeader() {
      setPreferredSize(new Dimension(DeviatedPerformanceView.ROW_HEADER_WIDTH,
          1));
      add(new JLabel("velocity"));
    }
  }

}
