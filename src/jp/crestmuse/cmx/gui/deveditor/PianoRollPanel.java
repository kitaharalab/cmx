package jp.crestmuse.cmx.gui.deveditor;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import jp.crestmuse.cmx.gui.deveditor.PrintableDeviatedNote.NoteMoveHandle;

public class PianoRollPanel extends JPanel implements MouseListener, MouseMotionListener {

  public static int WIDTH_PER_BEAT = 32;
  public static int HEIGHT_PER_NOTE = 16;
  public static int COLUMN_HEADER_HEIGHT = 16;
  private CompiledDeviation compiledDeviation;
  private ArrayList<PrintableDeviatedNote> deviatedNotes;
  private PrintableDeviatedNote hoverNote;
  private NoteMoveHandle holdNote;
  private boolean showAsTickTime;
  private ColumnHeaderPanel columnHeader;
  private RowHeaderPanel rowHeader;
  private JScrollPane parent;
  private int playingLine;
  
  public PianoRollPanel(CompiledDeviation compiledDeviation, JScrollPane parent) {
    this.compiledDeviation = compiledDeviation;
    this.parent = parent;
    playingLine = 0;
    deviatedNotes = new ArrayList<PrintableDeviatedNote>();
    for(CompiledDeviation.DeviatedNote dn : compiledDeviation.getDeviatedNotes())
      deviatedNotes.add(new PrintableDeviatedNote(dn));
    holdNote = null;
    showAsTickTime = true;
    int tickLength = (int)compiledDeviation.getSequence().getTickLength();
    int width = WIDTH_PER_BEAT * tickLength / CompiledDeviation.TICKS_PER_BEAT;
    setPreferredSize(new Dimension(width, HEIGHT_PER_NOTE*128));
    addMouseListener(this);
    addMouseMotionListener(this);
    
    int measures = tickLength/(CompiledDeviation.TICKS_PER_BEAT*4);
    int seconds = (int)(compiledDeviation.getSequence().getMicrosecondLength()/1000000);
    columnHeader = new ColumnHeaderPanel(measures, WIDTH_PER_BEAT*4, seconds, width/seconds, width, COLUMN_HEADER_HEIGHT);
    rowHeader = new RowHeaderPanel();
    rowHeader.setPreferredSize(new Dimension(16, HEIGHT_PER_NOTE*128));
  }

  public void mouseClicked(MouseEvent e) {
  }

  public void mouseEntered(MouseEvent e) {
  }

  public void mouseExited(MouseEvent e) {
  }

  public void mousePressed(MouseEvent e) {
    for(PrintableDeviatedNote d : deviatedNotes){
      holdNote = d.getHandle(e.getX(), e.getY());
      if(holdNote != null){
        hoverNote = null;
        try {
          Point p = MouseInfo.getPointerInfo().getLocation();
          Robot r = new Robot();
          r.mouseMove(p.x + holdNote.press(e.getX()), p.y);
        } catch (AWTException e1) {
          e1.printStackTrace();
        }
        break;
      }
    }
  }

  public void mouseReleased(MouseEvent e) {
    if(holdNote != null){
      holdNote.release();
      holdNote = null;
    }
  }

  public void mouseDragged(MouseEvent e) {
    if(holdNote != null){
      holdNote.drag(e.getX());
      repaint();
    }
  }

  public void mouseMoved(MouseEvent e) {
    if(holdNote == null){
      hoverNote = null;
      for(PrintableDeviatedNote p : deviatedNotes)
        if(p.isMouseOver(e.getX(), e.getY())){
          hoverNote = p;
          if(p.isMouseOnRight(e.getX(), e.getY()))
            setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
          else
            setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
          break;
        }
      if(hoverNote == null)
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      repaint();
    }
  }
  
  public void paint(Graphics g) {
    super.paint(g);
    for(PrintableDeviatedNote p : deviatedNotes) p.print(g);
    if(hoverNote != null) hoverNote.printAsHover(g);
    g.setColor(Color.BLUE);
    g.fillRect(playingLine - 1, 0, 3, getHeight());
  }
  
  public void setScrollPane(){
    parent.setViewportView(this);
    parent.setColumnHeaderView(columnHeader);
    parent.setRowHeaderView(rowHeader);
  }
  
  public int getPlayPointX(double currentTime, long currentTick){
    if(showAsTickTime)
      playingLine = (int)(getWidth()*currentTick/compiledDeviation.getSequence().getTickLength());
    else
      playingLine = (int)(getWidth()*currentTime*1000000/compiledDeviation.getSequence().getMicrosecondLength());
    return playingLine;
  }

  /**
   * 実時刻表示と楽譜時刻表示を切り替える
   */
  public void changeTimeLine(){
    if(showAsTickTime){
      int milSecLength = (int)(compiledDeviation.getSequence().getMicrosecondLength()/1000);
      for(PrintableDeviatedNote n : deviatedNotes) n.asRealTime(getWidth(), milSecLength);
    }else
      for(PrintableDeviatedNote n : deviatedNotes) n.asTickTime();
    showAsTickTime = !showAsTickTime;
  }
  
  private class ColumnHeaderPanel extends JPanel{
    private int measureNum;
    private int widthPerMeasure;
    private int seconds;
    private int widthPerSecond;
    public ColumnHeaderPanel(int measureNum, int widthPerMeasure, int seconds, int widthPerSecond, int width, int height) {
      this.measureNum = measureNum;
      this.widthPerMeasure = widthPerMeasure;
      this.seconds = seconds;
      this.widthPerSecond = widthPerSecond;
      setPreferredSize(new Dimension(width, height));
    }
    public void paint(Graphics g) {
      super.paint(g);
      g.setColor(Color.BLACK);
      if(showAsTickTime){
        for(int i=0; i<measureNum; i++)
          g.drawString((i + 1) + "", i*widthPerMeasure, COLUMN_HEADER_HEIGHT);
      }else{
        for(int i=0; i<seconds; i++){
          g.drawString(i/60 + ":" + i%60, i*widthPerSecond, COLUMN_HEADER_HEIGHT);
        }
      }
    }
  }
  
  private class RowHeaderPanel extends JPanel {
    public void paint(Graphics g) {
      super.paint(g);
      g.setColor(Color.BLACK);
      g.drawLine(getWidth() - 1, 0, getWidth() -1, getHeight());
      for(int i=0; i<128; i++){
        if(i%12==1 || i%12==3 || i%12==6 || i%12==8 || i%12==10){
          g.drawLine(0, i*HEIGHT_PER_NOTE + HEIGHT_PER_NOTE/2, getWidth(), i*HEIGHT_PER_NOTE + HEIGHT_PER_NOTE/2);
          g.fillRect(0, i*HEIGHT_PER_NOTE + HEIGHT_PER_NOTE/4, getWidth()*2/3, HEIGHT_PER_NOTE/2);
        }else if(i%12== 0 || i%12 == 5)
          g.drawLine(0, i*HEIGHT_PER_NOTE, getWidth(), i*HEIGHT_PER_NOTE);
      }
    }
  }

}
