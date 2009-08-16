package jp.crestmuse.cmx.gui.deveditor.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JPanel;

public class TempoPanel extends JPanel{

  private int[] xPoints;
  private int[] yPoints;
  private int nPoints;
  private double scale = 0.05;
  private int WINDOW_HEIGHT = 200;
  private RowHeader rowHeader;

  public TempoPanel(int ticksLength, Map<Integer, Integer> ticks2tempo){
    setPreferredSize(new Dimension((int)(ticksLength*scale), WINDOW_HEIGHT));
    setPoints(ticks2tempo);
    rowHeader = new RowHeader();
  }
  
  public JPanel getRowHeader(){
    return rowHeader;
  }

  public void setPoints(Map<Integer, Integer> ticks2tempo){
    nPoints = ticks2tempo.size() * 2 - 1;
    xPoints = new int[nPoints];
    yPoints = new int[nPoints];
    Iterator<Map.Entry<Integer, Integer>> it = ticks2tempo.entrySet().iterator();
    Map.Entry<Integer, Integer> head = it.next();
    xPoints[0] = (int)(head.getKey() * scale);
    yPoints[0] = WINDOW_HEIGHT - head.getValue();
    int i = 2, x;
    while(it.hasNext()){
      Map.Entry<Integer, Integer> e = it.next();
      x = (int)(e.getKey() * scale);
      xPoints[i-1] = x;
      yPoints[i-1] = yPoints[i-2];
      xPoints[i] = x;
      yPoints[i] = WINDOW_HEIGHT - e.getValue();
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
      setPreferredSize(new Dimension(32, WINDOW_HEIGHT));
    }

    public void paint(Graphics g) {
      super.paint(g);
      g.setColor(Color.BLACK);
      for(int i=0; i<WINDOW_HEIGHT; i+=30){
        g.drawString((WINDOW_HEIGHT - i) + "", 0, i);
      }
    }

  }
}
