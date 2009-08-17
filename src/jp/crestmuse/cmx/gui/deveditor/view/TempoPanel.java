package jp.crestmuse.cmx.gui.deveditor.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JPanel;

import jp.crestmuse.cmx.gui.deveditor.model.DeviatedPerformance;
import static jp.crestmuse.cmx.gui.deveditor.view.PianoRollPanel.*;

public class TempoPanel extends JPanel{

  public static int PANEL_HEIGHT = 200;
  private int[] xPoints;
  private int[] yPoints;
  private int nPoints;
  private double scale;
  private RowHeader rowHeader;
  private DeviatedPerformance deviatedPerformance;
  private PianoRollPanel pianoRollPanel;

  public TempoPanel(DeviatedPerformance deviatedPerformance, PianoRollPanel pianoRollPanel){
    this.deviatedPerformance = deviatedPerformance;
    this.pianoRollPanel = pianoRollPanel;
//    setPreferredSize(new Dimension((int)(ticksLength*scale), WINDOW_HEIGHT));
    updateScale();
    rowHeader = new RowHeader();
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
    nPoints = ticks2tempo.size() * 2 - 1;
    xPoints = new int[nPoints];
    yPoints = new int[nPoints];
    Iterator<Map.Entry<Integer, Integer>> it = ticks2tempo.entrySet().iterator();
    Map.Entry<Integer, Integer> head = it.next();
    xPoints[0] = (int)(head.getKey() * scale);
    yPoints[0] = PANEL_HEIGHT - head.getValue();
    int i = 2, x;
    while(it.hasNext()){
      Map.Entry<Integer, Integer> e = it.next();
      x = (int)(e.getKey() * scale);
      xPoints[i-1] = x;
      yPoints[i-1] = yPoints[i-2];
      xPoints[i] = x;
      yPoints[i] = PANEL_HEIGHT - e.getValue();
      i += 2;
    }
  }

  public void paint(Graphics g) {
    super.paint(g);
    g.setColor(Color.BLACK);
    g.drawPolyline(xPoints, yPoints, nPoints);
    g.drawLine(xPoints[nPoints-1], yPoints[nPoints-1], getWidth(), yPoints[nPoints-1]);
  }
  
  private class RowHeader extends JPanel{

    private RowHeader(){
      setPreferredSize(new Dimension(32, PANEL_HEIGHT));
    }

    public void paint(Graphics g) {
      super.paint(g);
      g.setColor(Color.BLACK);
      for(int i=0; i<PANEL_HEIGHT; i+=30){
        g.drawString((PANEL_HEIGHT - i) + "", 0, i);
      }
    }

  }
}
