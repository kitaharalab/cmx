package jp.crestmuse.cmx.gui.dppatheditor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JPanel;

import jp.crestmuse.cmx.gui.deveditor.model.DeviatedPerformance;

public class TempoCurve extends JPanel {
  
  public static int PANEL_HEIGHT = 200;
  private Curve tempo;
  
  public TempoCurve(DeviatedPerformance deviatedPerformance, int width) {
    int tickLength = (int) deviatedPerformance.getSequence().getTickLength();
    setPreferredSize(new Dimension(width, PANEL_HEIGHT));
    double scale = width / (double) tickLength;
    Map<Integer, Integer> ticks2tempo = deviatedPerformance.getTicks2Tempo();
    tempo = new Curve(ticks2tempo.size() * 2 - 1, Color.RED);
    Iterator<Map.Entry<Integer, Integer>> it = ticks2tempo.entrySet().iterator();
    Map.Entry<Integer, Integer> head = it.next();
    tempo.xPoints[0] = (int) (head.getKey() * scale);
    tempo.yPoints[0] = head.getValue();
    int i = 2, x;
    while (it.hasNext()) {
      Map.Entry<Integer, Integer> e = it.next();
      x = (int) (e.getKey() * scale);
      tempo.xPoints[i - 1] = x;
      tempo.yPoints[i - 1] = tempo.yPoints[i - 2];
      tempo.xPoints[i] = x;
      tempo.yPoints[i] = e.getValue();
      i += 2;
    }
  }
  
  public void paint(Graphics g) {
    super.paint(g);
    tempo.draw(g);
  }
  
  private class Curve {

    int[] xPoints, yPoints;
    int nPoints;
    Color color;

    Curve(int n, Color color) {
      xPoints = new int[n];
      yPoints = new int[n];
      nPoints = n;
      this.color = color;
    }

    void draw(Graphics g) {
      g.setColor(color);
      // don't use drawPolyLine for y axis
      for (int i = 0; i < nPoints - 1; i++) {
        g.drawLine(xPoints[i], getHeight() - yPoints[i], xPoints[i + 1],
            getHeight() - yPoints[i + 1]);
      }
      g.drawLine(xPoints[nPoints - 1], getHeight() - yPoints[nPoints - 1],
          getWidth(), getHeight() - yPoints[nPoints - 1]);
    }

  }

}
