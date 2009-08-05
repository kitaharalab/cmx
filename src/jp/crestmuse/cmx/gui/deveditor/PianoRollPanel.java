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

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import jp.crestmuse.cmx.gui.deveditor.PrintableDeviatedNote.NoteMoveHandle;

/**
 * このクラスはDeviationエディターで表示されるピアノロールを表します．
 * @author ntotani
 */
public class PianoRollPanel extends JPanel implements MouseListener, MouseMotionListener {

  public static int WIDTH_PER_BEAT = 32;
  public static int HEIGHT_PER_NOTE = 16;
  public static int COLUMN_HEADER_HEIGHT = 16;
  private CompiledDeviation compiledDeviation;
  private ArrayList<PrintableDeviatedNote> deviatedNotes;
  private ArrayList<PrintableNote> originalNotes;
  private PrintableDeviatedNote hoverNote;
  private NoteMoveHandle holdNote;
  private ColumnHeaderPanel columnHeader;
  private RowHeaderPanel rowHeader;
  private int playingLine;
  private JFrame tempoFrame;
  private NoteEditFrame noteEditFrame;

  public PianoRollPanel(CompiledDeviation compiledDeviation) {
    noteEditFrame = new NoteEditFrame(this);
    this.compiledDeviation = compiledDeviation;
    setTempoFrame();
    playingLine = 0;
    deviatedNotes = new ArrayList<PrintableDeviatedNote>();
    originalNotes = new ArrayList<PrintableNote>();
    for(CompiledDeviation.DeviatedNote dn : compiledDeviation.getDeviatedNotes()){
      deviatedNotes.add(new PrintableDeviatedNote(dn, this));
      if(!dn.isExtraNote()) originalNotes.add(new PrintableNote(dn, this));
    }
    holdNote = null;
    int tickLength = (int)compiledDeviation.getSequence().getTickLength();
    int width = WIDTH_PER_BEAT * tickLength / CompiledDeviation.TICKS_PER_BEAT;
    setPreferredSize(new Dimension(width, HEIGHT_PER_NOTE*128));
    addMouseListener(this);
    addMouseMotionListener(this);
    
    int measures = tickLength/(CompiledDeviation.TICKS_PER_BEAT*4);
    int seconds = (int)(compiledDeviation.getSequence().getMicrosecondLength()/1000000);
    columnHeader = new ColumnHeaderPanel(measures, WIDTH_PER_BEAT*4, seconds, width/seconds, width, COLUMN_HEADER_HEIGHT);
    rowHeader = new RowHeaderPanel();
    rowHeader.setPreferredSize(new Dimension(64, HEIGHT_PER_NOTE*128));
  }
  
  private void setTempoFrame(){
    tempoFrame = new JFrame();
    tempoFrame.setSize(320, 256);
    tempoFrame.setResizable(false);
    JScrollPane jsp = new JScrollPane();
    TempoPanel tp = new TempoPanel((int)compiledDeviation.getSequence().getTickLength(), compiledDeviation.getTicks2Tempo());
    jsp.setViewportView(tp);
    jsp.setRowHeaderView(tp.getRowHeader());
    tempoFrame.getContentPane().add(jsp);
  }
  
  public void mouseClicked(MouseEvent e) {
    if(e.getClickCount() == 2 && hoverNote == null && holdNote == null){
      if(GUI.Instance().getShowAsTickTime())
        GUI.Instance().setPlayPosition(compiledDeviation.getSequence().getTickLength()*e.getX()/getPreferredSize().width);
      else
        GUI.Instance().setPlayPosition(compiledDeviation.getSequence().getMicrosecondLength()*e.getX()/getPreferredSize().width);
    }else if(e.getButton() == MouseEvent.BUTTON3 && hoverNote != null){
      noteEditFrame.setNote(hoverNote);
      noteEditFrame.setLocation(MouseInfo.getPointerInfo().getLocation());
      noteEditFrame.setVisible(true);
      hoverNote = null;
      repaint();
    }
  }

  public void mouseEntered(MouseEvent e) {
  }

  public void mouseExited(MouseEvent e) {
  }

  public void mousePressed(MouseEvent e) {
    if(e.getButton() != MouseEvent.BUTTON1) return;
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
        // TODO PerformanceMatcherデバッグ緊急用
        System.out.println(d.getDeviatedNote().getIndex());
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

    g.setColor(Color.BLACK);
    int span = WIDTH_PER_BEAT*4;
    if(!GUI.Instance().getShowAsTickTime()) span = 2*this.getPreferredSize().width/(int)(compiledDeviation.getSequence().getMicrosecondLength()/1000000);
    for(int i=0; i<getPreferredSize().width; i+=span)
      g.drawLine(i, 0, i, getHeight());

    for(PrintableNote p : originalNotes) p.paint(g);
    for(PrintableDeviatedNote p : deviatedNotes) p.print(g);
    if(hoverNote != null) hoverNote.printAsHover(g);
    g.setColor(Color.BLUE);
    g.fillRect(playingLine - 1, 0, 3, getHeight());
  }
  
  public CompiledDeviation getCompiledDeviation() {
    return compiledDeviation;
  }
  
  public JFrame getTempoFrame() { return tempoFrame; }
  
  /**
   * 現在の再生位置のX座標を返します．
   * @param currentTime
   * @param currentTick
   * @return
   */
  public int getPlayPointX(double currentTime, long currentTick){
    if(GUI.Instance().getShowAsTickTime())
      playingLine = (int)(getPreferredSize().width*currentTick/compiledDeviation.getSequence().getTickLength());
    else
      playingLine = (int)(getPreferredSize().width*currentTime*1000000/compiledDeviation.getSequence().getMicrosecondLength());
    return playingLine;
  }
  
  public void setScrollPane(JScrollPane jsp){
    jsp.setViewportView(this);
    jsp.setColumnHeaderView(columnHeader);
    jsp.setRowHeaderView(rowHeader);
  }

  /**
   * パネルの幅を更新する．
   */
  public void updateScale(){
    int tickLength = (int)compiledDeviation.getSequence().getTickLength();
    int width = WIDTH_PER_BEAT * tickLength / CompiledDeviation.TICKS_PER_BEAT;
    setPreferredSize(new Dimension(width, HEIGHT_PER_NOTE*128));
    updateNotes();
    columnHeader.setPreferredSize(new Dimension(width, COLUMN_HEADER_HEIGHT));
    columnHeader.widthPerMeasure = WIDTH_PER_BEAT*4;
    int seconds = (int)(compiledDeviation.getSequence().getMicrosecondLength()/1000000);
    columnHeader.widthPerSecond = width/seconds;
  }

  /**
   * ノートの幅を更新する．
   */
  public void updateNotes(){
    if(GUI.Instance().getShowAsTickTime()){
      for(PrintableDeviatedNote n : deviatedNotes) n.asTickTime();
      for(PrintableNote n : originalNotes) n.asTickTime();
    }else{
      for(PrintableDeviatedNote n : deviatedNotes) n.asRealTime();
      for(PrintableNote n : originalNotes) n.asRealTime();
    }
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
      if(GUI.Instance().getShowAsTickTime()){
        for(int i=0; i<measureNum; i++)
          g.drawString((i + 1) + "", i*widthPerMeasure, COLUMN_HEADER_HEIGHT);
      }else{
        for(int i=0; i<seconds; i+=2){
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
          g.drawLine(0, (127 - i)*HEIGHT_PER_NOTE + HEIGHT_PER_NOTE/2, getWidth(), (127 - i)*HEIGHT_PER_NOTE + HEIGHT_PER_NOTE/2);
          g.fillRect(0, (127 - i)*HEIGHT_PER_NOTE + HEIGHT_PER_NOTE/4, getWidth()/2, HEIGHT_PER_NOTE/2);
        }else if(i%12== 0 || i%12 == 5)
          g.drawLine(0, (127 - i + 1)*HEIGHT_PER_NOTE, getWidth(), (127 - i + 1)*HEIGHT_PER_NOTE);
      }
    }
  }

}
