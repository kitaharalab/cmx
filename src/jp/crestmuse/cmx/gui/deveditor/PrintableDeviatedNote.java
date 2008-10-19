package jp.crestmuse.cmx.gui.deveditor;

import java.awt.Color;
import java.awt.Graphics;

public class PrintableDeviatedNote {

  private CompiledDeviation.DeviatedNote deviatedNote;
  int x, y, width, height;
  Color color;
  
  public PrintableDeviatedNote(CompiledDeviation.DeviatedNote deviatedNote) {
    this.deviatedNote = deviatedNote;
    int ticksPerBeat = deviatedNote.ticksPerBeat();
    x = (int)(deviatedNote.onset() / (double)ticksPerBeat * PianoRollPanel.WIDTH_PER_BEAT);
    y = PianoRollPanel.HEIGHT_PER_NOTE*deviatedNote.notenum();
    width = (int)((deviatedNote.offset() - deviatedNote.onset()) / (double)ticksPerBeat * PianoRollPanel.WIDTH_PER_BEAT);
    height = PianoRollPanel.HEIGHT_PER_NOTE;
    color = Color.RED;
  }

  public void print(Graphics g){
    g.setColor(color);
    g.fillRect(x, y, width, height);
  }

}
