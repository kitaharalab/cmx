package jp.crestmuse.cmx.gui.deveditor.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import jp.crestmuse.cmx.gui.deveditor.model.DeviatedPerformance;
import jp.crestmuse.cmx.gui.deveditor.model.DeviatedPerformance.DeviatedNote;

public class CurvesPanel extends JPanel implements ChangeListener {

  public static int ROW_HEADER_WIDTH = 64;
  public static int PANEL_HEIGHT = 200;
  private PolyLine tempo;
  private PolyLine dynamics;
  private double scale;
  private RowHeader rowHeader;
  private DeviatedPerformance deviatedPerformance;
  private PianoRollPanel pianoRollPanel;

  public CurvesPanel(DeviatedPerformance deviatedPerformance, PianoRollPanel pianoRollPanel){
    this.deviatedPerformance = deviatedPerformance;
    this.pianoRollPanel = pianoRollPanel;
    updateScale();
    rowHeader = new RowHeader();
  }

  public void stateChanged(ChangeEvent e) {
    updateScale();
    repaint();
  }

  public JPanel getRowHeader(){
    return rowHeader;
  }

  public void updateScale(){
    int tickLength = (int)deviatedPerformance.getSequence().getTickLength();
    int width = pianoRollPanel.getPreferredSize().width;
    setPreferredSize(new Dimension(width, PANEL_HEIGHT));
    scale = width / (double)tickLength;
    Map<Integer, Integer> ticks2tempo = deviatedPerformance.getTicks2Tempo();
    tempo = new PolyLine(ticks2tempo.size() * 2 - 1);
    Iterator<Map.Entry<Integer, Integer>> it = ticks2tempo.entrySet().iterator();
    Map.Entry<Integer, Integer> head = it.next();
    tempo.xPoints[0] = (int)(head.getKey() * scale);
    tempo.yPoints[0] = PANEL_HEIGHT - head.getValue();
    int i = 2, x;
    while(it.hasNext()){
      Map.Entry<Integer, Integer> e = it.next();
      x = (int)(e.getKey() * scale);
      tempo.xPoints[i-1] = x;
      tempo.yPoints[i-1] = tempo.yPoints[i-2];
      tempo.xPoints[i] = x;
      tempo.yPoints[i] = PANEL_HEIGHT - e.getValue();
      i += 2;
    }

    TreeMap<Integer, Double> ticks2dyn = new TreeMap<Integer, Double>();
    for(DeviatedNote dn : deviatedPerformance.getDeviatedNotes()) {
      Double dyn = ticks2dyn.get(dn.onset());
      if(dyn == null)
        dyn = 0.0;
      ticks2dyn.put(dn.onset(), dyn + Math.exp(dn.getDynamics()*100));
      dyn = ticks2dyn.get(dn.offset());
      if(dyn == null)
        dyn = 0.0;
      ticks2dyn.put(dn.offset(), dyn - Math.exp(dn.getDynamics()*100));
    }
    dynamics = new PolyLine(ticks2dyn.size() * 2 + 1);
    dynamics.xPoints[0] = 0;
    dynamics.yPoints[0] = PANEL_HEIGHT;
    i = 1;
    double sum = 0.0;
    for(Map.Entry<Integer, Double> e : ticks2dyn.entrySet()) {
      sum += e.getValue();
      dynamics.xPoints[i] = dynamics.xPoints[i + 1] = (int)(e.getKey() * scale);
      dynamics.yPoints[i] = dynamics.yPoints[i - 1];
      if(sum < 0)
        dynamics.yPoints[i + 1] = PANEL_HEIGHT;
      else
        dynamics.yPoints[i + 1] = dynamics2height(Math.log(sum));
      i += 2;
    }
  }

  private int dynamics2height(double d) {
    return (int)(PANEL_HEIGHT - d);
  }

  public void paint(Graphics g) {
    super.paint(g);
    g.setColor(Color.BLACK);
    tempo.draw(g);
    dynamics.draw(g);
  }

  public static class RowHeader extends JPanel{

    public RowHeader(){
      setPreferredSize(new Dimension(ROW_HEADER_WIDTH, PANEL_HEIGHT));
//      add(new JLabel("tempo"));
//      add(new JLabel("dynamics"));
    }

    public void paint(Graphics g) {
      super.paint(g);
      g.setColor(Color.BLACK);
      for(int i=0; i<getHeight(); i+=30)
        g.drawString(i + "", 0, getHeight() - i - 0);
    }

  }

  private class PolyLine {
    int[] xPoints, yPoints;
    int nPoints;
    PolyLine(int n) {
      xPoints = new int[n];
      yPoints = new int[n];
      nPoints = n;
    }
    void draw(Graphics g) {
      g.drawPolyline(xPoints, yPoints, nPoints);
      g.drawLine(xPoints[nPoints-1], yPoints[nPoints-1], getWidth(), yPoints[nPoints-1]);
    }
  }
}
