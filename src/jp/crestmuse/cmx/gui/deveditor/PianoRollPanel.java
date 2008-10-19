package jp.crestmuse.cmx.gui.deveditor;

import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;

import javax.swing.JPanel;

public class PianoRollPanel extends JPanel{

  private ArrayList<PrintableDeviatedNote> deviatedNotes;
  public static int WIDTH_PER_BEAT = 32;
  public static int HEIGHT_PER_NOTE = 16;
  private long tickLength;
  
  public PianoRollPanel(CompiledDeviation compiledDeviation) {
    deviatedNotes = new ArrayList<PrintableDeviatedNote>();
    for(CompiledDeviation.DeviatedNote dn : compiledDeviation.getDeviatedNotes()){
      deviatedNotes.add(new PrintableDeviatedNote(dn));
    }
    tickLength = compiledDeviation.getSequence().getTickLength();
    long width = WIDTH_PER_BEAT * tickLength / CompiledDeviation.TICKS_PER_BEAT;
    setPreferredSize(new Dimension((int)width, HEIGHT_PER_NOTE*128));
  }
  
  public void paint(Graphics g) {
    super.paint(g);
    for(PrintableDeviatedNote p : deviatedNotes) p.print(g);
  }
  
  public int getX(double currentTime, long currentTick){
    return (int)(getWidth() * currentTick / tickLength);
  }
  
  /**
   * 実時刻表示と楽譜時刻表示を切り替える
   */
  public void changeTimeLine(){
    // TODO 実装
  }

}
