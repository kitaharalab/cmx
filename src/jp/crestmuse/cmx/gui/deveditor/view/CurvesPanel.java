package jp.crestmuse.cmx.gui.deveditor.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JPanel;

import jp.crestmuse.cmx.gui.deveditor.controller.DeviatedNoteUpdateListener;
import jp.crestmuse.cmx.gui.deveditor.model.DeviatedPerformance;
import jp.crestmuse.cmx.gui.deveditor.model.DeviatedPerformance.DeviatedNote;

public class CurvesPanel extends JPanel implements DeviatedNoteUpdateListener {

  public static int ROW_HEADER_WIDTH = 64;
  public static int PANEL_HEIGHT = 200;
  private Curve tempo;
  private Curve dynamics;
  private Curve beatDev;
  private double scale;
  private RowHeader rowHeader;
  private DeviatedPerformance deviatedPerformance;
  private PianoRollPanel pianoRollPanel;

  public CurvesPanel(DeviatedPerformance deviatedPerformance,
      PianoRollPanel pianoRollPanel) {
    this.deviatedPerformance = deviatedPerformance;
    this.pianoRollPanel = pianoRollPanel;
    updateScale();
    rowHeader = new RowHeader();
  }

  public void noteUpdated(DeviatedNote updatedNote) {
    updateScale();
    repaint();
  }

  public JPanel getRowHeader() {
    return rowHeader;
  }

  public void updateScale() {
    int tickLength = (int) deviatedPerformance.getSequence().getTickLength();
    int width = pianoRollPanel.getPreferredSize().width;
    setPreferredSize(new Dimension(width, PANEL_HEIGHT));
    scale = width / (double) tickLength;
    Map<Integer, Integer> ticks2tempo = deviatedPerformance.getTicks2Tempo();
    tempo = new Curve(ticks2tempo.size() * 2 - 1, Color.GREEN);
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

    TreeMap<Integer, Double> ticks2dyn = new TreeMap<Integer, Double>();
    for (DeviatedNote dn : deviatedPerformance.getDeviatedNotes()) {
      Double dyn = ticks2dyn.get(dn.onset());
      if (dyn == null)
        dyn = 0.0;
      ticks2dyn.put(dn.onset(), dyn + Math.exp(dn.getDynamics() * 100));
      dyn = ticks2dyn.get(dn.offset());
      if (dyn == null)
        dyn = 0.0;
      ticks2dyn.put(dn.offset(), dyn - Math.exp(dn.getDynamics() * 100));
    }
    dynamics = new Curve(ticks2dyn.size() * 2 + 1, Color.BLUE);
    dynamics.xPoints[0] = 0;
    dynamics.yPoints[0] = 0;
    i = 1;
    double sum = 0.0;
    for (Map.Entry<Integer, Double> e : ticks2dyn.entrySet()) {
      sum += e.getValue();
      dynamics.xPoints[i] = dynamics.xPoints[i + 1] = (int) (e.getKey() * scale);
      dynamics.yPoints[i] = dynamics.yPoints[i - 1];
      if (sum < 0)
        dynamics.yPoints[i + 1] = 0;
      else
        dynamics.yPoints[i + 1] = (int) Math.round(Math.log(sum));
      i += 2;
    }

    // TODO imp
    beatDev = new Curve(2, Color.BLACK);
    beatDev.xPoints[0] = 0;
    beatDev.yPoints[0] = 10;
    beatDev.xPoints[1] = width;
    beatDev.yPoints[1] = 10;
  }

  public void paint(Graphics g) {
    super.paint(g);
    tempo.draw(g);
    dynamics.draw(g);
    beatDev.draw(g);
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

  public static class RowHeader extends JPanel {

    public RowHeader() {
      setPreferredSize(new Dimension(ROW_HEADER_WIDTH, PANEL_HEIGHT));
      // add(new JLabel("tempo"));
      // add(new JLabel("dynamics"));
    }

    public void paint(Graphics g) {
      super.paint(g);
      g.setColor(Color.BLACK);
      for (int i = 0; i < getHeight(); i += 30)
        g.drawString(i + "", ROW_HEADER_WIDTH - 30, getHeight() - i - 0);
      g.setColor(Color.GREEN);
      g.drawString("tempo", 0, 20);
      g.setColor(Color.BLUE);
      g.drawString("dynamics", 0, 40);
    }

  }

}
