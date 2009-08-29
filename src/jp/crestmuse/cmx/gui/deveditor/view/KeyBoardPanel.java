package jp.crestmuse.cmx.gui.deveditor.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

import static jp.crestmuse.cmx.gui.deveditor.view.PianoRollPanel.*;

public class KeyBoardPanel extends JPanel {

  public KeyBoardPanel() {
    setPreferredSize(new Dimension(CurvesPanel.ROW_HEADER_WIDTH, HEIGHT_PER_NOTE*128));
  }

  public void paint(Graphics g) {
    super.paint(g);
    g.setColor(Color.BLACK);
    g.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());
    for (int i = 0; i < 128; i++) {
      if (i % 12 == 1 || i % 12 == 3 || i % 12 == 6 || i % 12 == 8
          || i % 12 == 10) {
        g.drawLine(0, (127 - i) * HEIGHT_PER_NOTE + HEIGHT_PER_NOTE / 2,
            getWidth(), (127 - i) * HEIGHT_PER_NOTE + HEIGHT_PER_NOTE / 2);
        g.fillRect(0, (127 - i) * HEIGHT_PER_NOTE + HEIGHT_PER_NOTE / 4,
            getWidth() / 2, HEIGHT_PER_NOTE / 2);
      } else if (i % 12 == 0 || i % 12 == 5)
        g.drawLine(0, (127 - i + 1) * HEIGHT_PER_NOTE, getWidth(),
            (127 - i + 1) * HEIGHT_PER_NOTE);
    }
  }

}
