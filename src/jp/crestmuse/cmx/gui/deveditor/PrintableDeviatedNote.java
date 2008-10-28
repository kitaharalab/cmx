package jp.crestmuse.cmx.gui.deveditor;

import java.awt.Color;
import java.awt.Graphics;

import javax.sound.midi.InvalidMidiDataException;

public class PrintableDeviatedNote {
// TODO widthが負の数(onset > offset)のやつがいる
  private CompiledDeviation.DeviatedNote deviatedNote;
  private int x, y, width, height;
  private Color color;

  public PrintableDeviatedNote(CompiledDeviation.DeviatedNote deviatedNote) {
    this.deviatedNote = deviatedNote;
    y = PianoRollPanel.HEIGHT_PER_NOTE*deviatedNote.notenum();
    height = PianoRollPanel.HEIGHT_PER_NOTE;
    asTickTime();
    color = Color.RED;
  }

  public void print(Graphics g){
    g.setColor(color);
    g.fillRect(x, y, width, height);
    g.setColor(color.brighter());
    g.drawLine(x, y, x + width - 1, y);
    g.drawLine(x, y, x, y + height - 1);
    g.setColor(color.darker());
    g.drawLine(x, y + height, x + width, y + height);
    g.drawLine(x + width, y, x + width, y + height);
  }
  
  public void printAsHover(Graphics g){
    x -= 5; y -= 5; width += 10; height += 10;
    print(g);
    x += 5; y += 5; width -= 10; height -= 10;
  }
  
  public void asTickTime(){
    int ticksPerBeat = deviatedNote.ticksPerBeat();
    x = (int)(deviatedNote.onset() / (double)ticksPerBeat * PianoRollPanel.WIDTH_PER_BEAT);
    width = (int)((deviatedNote.offset() - deviatedNote.onset()) / (double)ticksPerBeat * PianoRollPanel.WIDTH_PER_BEAT);
  }
  
  public void asRealTime(int panelWidth, int milSecLength){
    x = (int)(panelWidth*deviatedNote.onsetInMSec()/milSecLength);
    width = (int)(panelWidth*(deviatedNote.offsetInMSec() - deviatedNote.onsetInMSec())/milSecLength);
  }

  public boolean isMouseOver(int mouseX, int mouseY){
    return mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height;
  }
  
  public boolean isMouseOnRight(int mouseX, int mouseY){
    return isMouseOver(mouseX, mouseY) && mouseX > x + width/2;
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
        if(noteon)
          deviatedNote.changeDeviation((x - prevX)/(double)PianoRollPanel.WIDTH_PER_BEAT, 0);
        else
          deviatedNote.changeDeviation(0, (width - prevWidth)/(double)PianoRollPanel.WIDTH_PER_BEAT);
        asTickTime();
      } catch (InvalidMidiDataException e) {
        e.printStackTrace();
      }
    }
  }

}
