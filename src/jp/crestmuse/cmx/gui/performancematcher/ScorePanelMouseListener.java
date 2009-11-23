package jp.crestmuse.cmx.gui.performancematcher;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import jp.crestmuse.cmx.gui.deveditor.view.PianoRollPanel;
import jp.crestmuse.cmx.gui.deveditor.view.PianoRollPanel.PrintableOriginalNote;

public class ScorePanelMouseListener implements MouseListener {

  private PianoRollPanel scorePanel;
  private PerformanceMatcherController controller;

  public ScorePanelMouseListener(PianoRollPanel scorePanel,
      PerformanceMatcherController controller) {
    this.scorePanel = scorePanel;
    this.controller = controller;
  }

  public void mouseClicked(MouseEvent e) {
  }

  public void mouseEntered(MouseEvent e) {
  }

  public void mouseExited(MouseEvent e) {
  }

  public void mousePressed(MouseEvent e) {
    for (PrintableOriginalNote pon : scorePanel.getOriginalNotes()) {
      if (pon.isMouseOver(e.getX(), e.getY())) {
        controller.selectScore(pon);
        break;
      }
    }
  }

  public void mouseReleased(MouseEvent e) {
  }

}
