package jp.crestmuse.cmx.gui.deveditor.view;

import java.awt.Dimension;

import javax.swing.JPanel;

import jp.crestmuse.cmx.gui.deveditor.model.DeviatedPerformance;

public class VelocityPanel extends JPanel {

  public static int PANEL_HEIGTH = 100;
  private DeviatedPerformance deviatedPerformance;
  private PianoRollPanel pianoRollPanel;

  public VelocityPanel(DeviatedPerformance deviatedPerformance, PianoRollPanel pianoRollpanel) {
    this.deviatedPerformance = deviatedPerformance;
    this.pianoRollPanel = pianoRollpanel;
    updateScale();
  }

  public void updateScale() {
    int width = pianoRollPanel.getPreferredSize().width;
    setPreferredSize(new Dimension(width, PANEL_HEIGTH));
  }

}
