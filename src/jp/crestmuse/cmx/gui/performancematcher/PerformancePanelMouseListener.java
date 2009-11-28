package jp.crestmuse.cmx.gui.performancematcher;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import jp.crestmuse.cmx.gui.deveditor.view.PianoRollPanel;
import jp.crestmuse.cmx.gui.deveditor.view.PianoRollPanel.PrintableDeviatedNote;

public class PerformancePanelMouseListener implements MouseListener {

  private PianoRollPanel performancePanel;
  private PerformanceMatcherController controller;

  public PerformancePanelMouseListener(PianoRollPanel performancePanel,
      PerformanceMatcherController controller) {
    this.performancePanel = performancePanel;
    this.controller = controller;
  }

  public void mouseClicked(MouseEvent e) {
  }

  public void mouseEntered(MouseEvent e) {
  }

  public void mouseExited(MouseEvent e) {
  }

  public void mousePressed(MouseEvent e) {
    for (PrintableDeviatedNote d : performancePanel.getDeviatedNotes()) {
      if (d.show() && !d.getDeviatedNote().getIsMissNote()
          && d.isMouseOver(e.getX(), e.getY())) {
        controller.selectPerformance(d);
        break;
      }
    }
  }

  public void mouseReleased(MouseEvent e) {
  }

}
