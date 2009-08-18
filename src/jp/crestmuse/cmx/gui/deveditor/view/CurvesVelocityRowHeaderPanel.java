package jp.crestmuse.cmx.gui.deveditor.view;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class CurvesVelocityRowHeaderPanel extends JPanel {

  public CurvesVelocityRowHeaderPanel() {
    JPanel curvesPanel = new JPanel();
    curvesPanel.setPreferredSize(new Dimension(1, 1));
    curvesPanel.add(new JLabel("tempo"));
    curvesPanel.add(new JLabel("dynamics"));
    JPanel velocityPanel = new JPanel();
    velocityPanel
        .setPreferredSize(new Dimension(1, VelocityPanel.PANEL_HEIGTH));
    velocityPanel.add(new JLabel("velocity"));
    setLayout(new BorderLayout());
    setPreferredSize(new Dimension(DeviatedPerformanceView.ROW_HEADER_WIDTH,
        CurvesPanel.PANEL_HEIGHT + VelocityPanel.PANEL_HEIGTH));
    add(curvesPanel, BorderLayout.CENTER);
    add(velocityPanel, BorderLayout.SOUTH);
  }

}
