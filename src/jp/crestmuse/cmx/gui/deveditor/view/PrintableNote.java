package jp.crestmuse.cmx.gui.deveditor.view;

import java.awt.Color;
import java.awt.Graphics;

import jp.crestmuse.cmx.gui.deveditor.model.DeviatedPerformance;

public class PrintableNote {

  private PianoRollPanel parent;
  private int x, y, width, height;
  private int onset, offset, onsetInMSec, offsetInMSec;
  
  public PrintableNote(DeviatedPerformance.DeviatedNote dn, PianoRollPanel parent){
    this.parent = parent;
    onset = dn.onsetOriginal();
    offset = dn.offsetOriginal();
    onsetInMSec = dn.onsetOriginalInMSec();
    offsetInMSec = dn.offsetOriginalInMSec();
    y = PianoRollPanel.HEIGHT_PER_NOTE*(127 - dn.notenum());
    height = PianoRollPanel.HEIGHT_PER_NOTE;
    asTickTime();
  }
  
  public void paint(Graphics g){
    g.setColor(Color.BLACK);
    g.drawRect(x, y, width - 1, height - 1);
  }
  
  public void asTickTime(){
    int ticksPerBeat = DeviatedPerformance.TICKS_PER_BEAT;
    x = (int)(onset / (double)ticksPerBeat * PianoRollPanel.WIDTH_PER_BEAT);
    width = (int)((offset - onset) / (double)ticksPerBeat * PianoRollPanel.WIDTH_PER_BEAT);
  }
  
  public void asRealTime(){
    int panelWidth = parent.getPreferredSize().width;
    int milSecLength = (int)(parent.getDeviatedPerformance().getSequence().getMicrosecondLength()/1000);
    x = (int)(panelWidth*onsetInMSec/milSecLength);
    width = (int)(panelWidth*(offsetInMSec - onsetInMSec)/milSecLength);
  }

}