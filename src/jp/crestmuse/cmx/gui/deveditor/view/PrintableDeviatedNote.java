package jp.crestmuse.cmx.gui.deveditor.view;

import java.awt.Color;
import java.awt.Graphics;

import javax.sound.midi.InvalidMidiDataException;

import jp.crestmuse.cmx.gui.deveditor.model.DeviatedPerformance;

/**
 * PianoRollPanel上でDeviatedNoteを表示するするためのクラスです．
 * @author ntotani
 */
public class PrintableDeviatedNote {
  private DeviatedPerformance.DeviatedNote deviatedNote;
  private PianoRollPanel parent;
  private int x, y, width, height;
  private Color fillColor;
  private Color roundColor;

  public PrintableDeviatedNote(DeviatedPerformance.DeviatedNote deviatedNote, PianoRollPanel parent) {
    this.deviatedNote = deviatedNote;
    this.parent = parent;
    asTickTime();
    y = PianoRollPanel.HEIGHT_PER_NOTE*(127 - deviatedNote.notenum());
    height = PianoRollPanel.HEIGHT_PER_NOTE;
    if(deviatedNote.isExtraNote()){
      fillColor = new Color(255, 255, 0, deviatedNote.velocity()*2);
      roundColor = Color.YELLOW;
    }else if(deviatedNote.getNote().voice() == 1){
      fillColor = new Color(255, 0, 0, deviatedNote.velocity()*2);
      roundColor = Color.RED;
    }else{
      fillColor = new Color(255, 127, 0, deviatedNote.velocity()*2);
      roundColor = Color.ORANGE;
    }
  }

  public void print(Graphics g){
    g.setColor(fillColor);
    g.fillRect(x, y, width, height);
    g.setColor(roundColor);
    g.drawRect(x + 1, y + 1, width - 3, height - 3);
  }
  
  public void printAsHover(Graphics g){
    g.setColor(roundColor);
    g.fillRect(x - 5, y - 5, width + 10, height + 10);
  }
  
  public DeviatedPerformance.DeviatedNote getDeviatedNote(){
    return deviatedNote;
  }
  
  /**
   * 楽譜時刻として位置と幅を更新する．
   */
  public void asTickTime(){
    int ticksPerBeat = DeviatedPerformance.TICKS_PER_BEAT;
    x = (int)(deviatedNote.onset() / (double)ticksPerBeat * PianoRollPanel.WIDTH_PER_BEAT);
    width = (int)((deviatedNote.offset() - deviatedNote.onset()) / (double)ticksPerBeat * PianoRollPanel.WIDTH_PER_BEAT);
  }
  
  /**
   * 実時刻として位置を更新する．
   */
  public void asRealTime(){
    int panelWidth = parent.getPreferredSize().width;
    int milSecLength = (int)(parent.getDeviatedPerformance().getSequence().getMicrosecondLength()/1000);
    x = (int)(panelWidth*deviatedNote.onsetInMSec()/milSecLength);
    width = (int)(panelWidth*(deviatedNote.offsetInMSec() - deviatedNote.onsetInMSec())/milSecLength);
  }

  public boolean isMouseOver(int mouseX, int mouseY){
    return mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height;
  }
  
  public boolean isMouseOnRight(int mouseX, int mouseY){
    return isMouseOver(mouseX, mouseY) && mouseX > x + width/2;
  }
  
  /**
   * DeviatedNoteのchangeDeviationを呼び出して位置と色を更新する．
   */
  public boolean changeDeviation(double attack, double release, double dynamics, double endDynamics){
    try {
      if(deviatedNote.changeDeviation(attack, release, dynamics, endDynamics)){
        if(MainFrame.getInstance().getShowAsTickTime())
          asTickTime();
        else
          asRealTime();
        fillColor = new Color(fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), deviatedNote.velocity()*2);
        return true;
      }
    } catch (InvalidMidiDataException e) {
      e.printStackTrace();
    }
    return false;
  }

  public void setMissNote(boolean missnote) {
    try {
      deviatedNote.setMissNote(missnote);
      fillColor = new Color(fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), deviatedNote.velocity()*2);
    } catch (InvalidMidiDataException e) {
      e.printStackTrace();
    }
  }
  
  public NoteMoveHandle getHandle(int mouseX, int mouseY){
    if(isMouseOver(mouseX, mouseY)){
      if(mouseX > x + width/2)
        return new NoteMoveHandle(false, x, x, width);
      else
        return new NoteMoveHandle(true, x + width, x, width);
    }
    return null;
  }

  class NoteMoveHandle{
    // TODO 実時刻表時のとき
    private boolean noteon;
    private int limit, prevX, prevWidth;
    private NoteMoveHandle(boolean noteon, int limit, int prevX, int prevWidth){
      this.noteon = noteon;
      this.limit = limit;
      this.prevX = prevX;
      this.prevWidth = prevWidth;
    }
    public int press(int posX){
      if(noteon) return prevX - posX;
      return prevX + prevWidth - posX;
    }
    public void drag(int posX){
      if(noteon){
        x = Math.min(posX, limit - 1);
        width = limit - x;
      }else{
        width = Math.max(posX - x, 1);
      }
    }
    public void release(){
      try {
        if(MainFrame.getInstance().getShowAsTickTime()){
          if(noteon)
            deviatedNote.changeDeviation((x - prevX)/(double)PianoRollPanel.WIDTH_PER_BEAT, 0);
          else
            deviatedNote.changeDeviation(0, (width - prevWidth)/(double)PianoRollPanel.WIDTH_PER_BEAT);
          asTickTime();
        }else{
          int msecLength = (int)(parent.getDeviatedPerformance().getSequence().getMicrosecondLength()/1000);
          int panelWidth = parent.getPreferredSize().width;
          if(noteon)
            deviatedNote.changeAttackInMsec(x*msecLength/panelWidth);
          else
            deviatedNote.changeReleaseInMsec((x + width)*msecLength/panelWidth);
          asRealTime();
        }
      } catch (InvalidMidiDataException e) {
        e.printStackTrace();
      }
    }
  }

}
