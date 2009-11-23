package jp.crestmuse.cmx.gui.deveditor.view;

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
import java.util.HashMap;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import jp.crestmuse.cmx.filewrappers.MusicXMLWrapper;
import jp.crestmuse.cmx.gui.deveditor.controller.ChangeAttackInMSec;
import jp.crestmuse.cmx.gui.deveditor.controller.ChangeDeviation;
import jp.crestmuse.cmx.gui.deveditor.controller.ChangeReleaseInMSec;
import jp.crestmuse.cmx.gui.deveditor.controller.DeviatedNoteControler;
import jp.crestmuse.cmx.gui.deveditor.controller.DeviatedNoteSelectListener;
import jp.crestmuse.cmx.gui.deveditor.controller.DeviatedNoteUpdateListener;
import jp.crestmuse.cmx.gui.deveditor.model.DeviatedPerformance;
import jp.crestmuse.cmx.gui.deveditor.model.DeviatedPerformance.DeviatedNote;

/**
 * このクラスはDeviationエディターで表示されるピアノロールを表します．
 * 
 * @author ntotani
 */
public class PianoRollPanel extends JPanel implements
    DeviatedNoteSelectListener, DeviatedNoteUpdateListener {

  public static int WIDTH_PER_BEAT = 32;
  public static int HEIGHT_PER_NOTE = 16;
  public static int COLUMN_HEADER_HEIGHT = 16;
  private DeviatedPerformance deviatedPerformance;
  private ArrayList<PrintableOriginalNote> originalNotes;
  private ArrayList<PrintableDeviatedNote> deviatedNotes;
  private ArrayList<PrintableNote> allNotes;
  private PrintableDeviatedNote hoverNote;
  // private PrintableDeviatedNote.NoteMoveHandle holdNote;
  private PrintableDeviatedNote selectedNote;
  private ColumnHeaderPanel columnHeader;
  private int playingLine;
  private HashMap<DeviatedNote, PrintableDeviatedNote> dn2pdn;
  // private DeviatedNoteControler deviatedNoteControler;
  private boolean hideDeviatedNote = false;
  private static short showVoice = 63;

  public PianoRollPanel(DeviatedPerformance deviatedPerformance) {
    this.deviatedPerformance = deviatedPerformance;
    // this.deviatedNoteControler = deviatedNoteControler;
    playingLine = 0;
    originalNotes = new ArrayList<PrintableOriginalNote>();
    deviatedNotes = new ArrayList<PrintableDeviatedNote>();
    allNotes = new ArrayList<PrintableNote>();
    dn2pdn = new HashMap<DeviatedNote, PrintableDeviatedNote>();
    for (DeviatedPerformance.DeviatedNote dn : deviatedPerformance.getDeviatedNotes()) {
      PrintableDeviatedNote pdn = new PrintableDeviatedNote(dn);
      deviatedNotes.add(pdn);
      allNotes.add(pdn);
      dn2pdn.put(dn, pdn);
      if (!dn.isExtraNote()) {
        PrintableOriginalNote on = new PrintableOriginalNote(dn);
        originalNotes.add(on);
        allNotes.add(on);
        pdn.pair = on;
        on.pair = pdn;
      }
    }
    // holdNote = null;
    int tickLength = (int) deviatedPerformance.getSequence().getTickLength();
    int width = WIDTH_PER_BEAT * tickLength
        / DeviatedPerformance.TICKS_PER_BEAT;
    setPreferredSize(new Dimension(width, HEIGHT_PER_NOTE * 128));
    // addMouseListener(this);
    // addMouseMotionListener(this);

    int measures = tickLength / (DeviatedPerformance.TICKS_PER_BEAT * 4);
    int seconds = (int) (deviatedPerformance.getSequence().getMicrosecondLength() / 1000000);
    columnHeader = new ColumnHeaderPanel(measures, WIDTH_PER_BEAT * 4, seconds,
        width / seconds, width, COLUMN_HEADER_HEIGHT);
  }

  public void noteSelected(DeviatedNote selectedNote) {
    this.selectedNote = dn2pdn.get(selectedNote);
    hoverNote = null;
    repaint();
  }

  public void noteUpdated(DeviatedNote updatedNote) {
    dn2pdn.get(updatedNote).updateScale();
    repaint();
  }

  public void paint(Graphics g) {
    super.paint(g);
    g.setColor(Color.BLACK);
    if (!MainFrame.getInstance().getShowAsTickTime()) {
      int span = 2
          * this.getPreferredSize().width
          / (int) (deviatedPerformance.getSequence().getMicrosecondLength() / 1000000);
      for (int i = 0; i < getPreferredSize().width; i += span)
        g.drawLine(i, 0, i, getHeight());
    } else {
      MusicXMLWrapper mus = deviatedPerformance.getMusicXML();
      for (int i = 1; i < mus.getPartList()[0].getMeasureList().length; i++) {
        int x = mus.getCumulativeTicks(i, DeviatedPerformance.TICKS_PER_BEAT);
        x = x * WIDTH_PER_BEAT / DeviatedPerformance.TICKS_PER_BEAT;
        g.drawLine(x, 0, x, getHeight());
      }
    }
    for (PrintableNote pn : allNotes)
      pn.paint(g);
    if (hoverNote != null)
      hoverNote.paintAsHover(g);
    if (selectedNote != null) {
      if (hideDeviatedNote) {
        if (selectedNote.pair != null)
          selectedNote.pair.paintAsSelected(g);
      } else
        selectedNote.paintAsSelected(g);
    }
    g.setColor(Color.BLUE);
    g.fillRect(playingLine - 1, 0, 3, getHeight());
  }

  public DeviatedPerformance getDeviatedPerformance() {
    return deviatedPerformance;
  }

  public ColumnHeaderPanel getColumnHeader() {
    return columnHeader;
  }

  // public JFrame getTempoFrame() { return tempoFrame; }

  /**
   * 現在の再生位置のX座標を返します．
   * 
   * @param currentTime
   * @param currentTick
   * @return
   */
  public int getPlayPointX(double currentTime, long currentTick) {
    if (MainFrame.getInstance().getShowAsTickTime())
      playingLine = (int) (getPreferredSize().width * currentTick / deviatedPerformance.getSequence().getTickLength());
    else
      playingLine = (int) (getPreferredSize().width * currentTime * 1000000 / deviatedPerformance.getSequence().getMicrosecondLength());
    return playingLine;
  }

  /**
   * パネルの幅を更新する．
   */
  public void updateScale() {
    // TODO real time
    int tickLength = (int) deviatedPerformance.getSequence().getTickLength();
    int width = WIDTH_PER_BEAT * tickLength
        / DeviatedPerformance.TICKS_PER_BEAT;
    setPreferredSize(new Dimension(width, HEIGHT_PER_NOTE * 128));
    // updateNotes();
    for (PrintableNote pn : allNotes)
      pn.updateScale();
    columnHeader.setPreferredSize(new Dimension(width, COLUMN_HEADER_HEIGHT));
    columnHeader.widthPerMeasure = WIDTH_PER_BEAT * 4;
    int seconds = (int) (deviatedPerformance.getSequence().getMicrosecondLength() / 1000000);
    columnHeader.widthPerSecond = width / seconds;
  }

  public boolean isShowing(DeviatedNote dn) {
    return (dn2pdn.get(dn).voice & showVoice) > 0;
  }

  public boolean existHoverNote() {
    return hoverNote != null;
  }

  public PrintableDeviatedNote getHoverNote() {
    return hoverNote;
  }

  public void setHoverNote(PrintableDeviatedNote pd) {
    hoverNote = pd;
  }

  public void releaseHoverNote() {
    hoverNote = null;
  }

  public PrintableDeviatedNote getSelectedNote() {
    return selectedNote;
  }

  public Color getNoteColor(DeviatedNote dn) {
    return dn2pdn.get(dn).roundColor;
  }

  public ArrayList<PrintableOriginalNote> getOriginalNotes() {
    return originalNotes;
  }

  public ArrayList<PrintableDeviatedNote> getDeviatedNotes() {
    return deviatedNotes;
  }

  public void setHideDeviateNote(boolean value) {
    hideDeviatedNote = value;
  }

  public boolean getHideDeviatedNote() {
    return hideDeviatedNote;
  }
  
  public PrintableDeviatedNote getPrintableDeviatedNote(DeviatedNote deviatedNote) {
    return dn2pdn.get(deviatedNote);
  }

  public static void toggleExtra(boolean b) {
    if (b)
      showVoice |= 1;
    else
      showVoice &= 62;
  }

  public static void toggleVoice1(boolean b) {
    if (b)
      showVoice |= 2;
    else
      showVoice &= 61;
  }

  public static void toggleVoice2(boolean b) {
    if (b)
      showVoice |= 4;
    else
      showVoice &= 59;
  }

  public static void toggleVoice3(boolean b) {
    if (b)
      showVoice |= 8;
    else
      showVoice &= 55;
  }

  public static void toggleVoice4(boolean b) {
    if (b)
      showVoice |= 16;
    else
      showVoice &= 47;
  }

  public static void toggleVoiceOther(boolean b) {
    if (b)
      showVoice |= 32;
    else
      showVoice &= 31;
  }

  private abstract class PrintableNote {
    int x, y, width, height;

    public boolean isMouseOver(int mouseX, int mouseY) {
      return mouseX > x && mouseX < x + width && mouseY > y
          && mouseY < y + height;
    }

    abstract void updateScale();

    abstract void paint(Graphics g);
  }

  public class PrintableOriginalNote extends PrintableNote {

    private int onset, offset, onsetInMSec, offsetInMSec;
    private PrintableDeviatedNote pair;

    public PrintableOriginalNote(DeviatedNote dn) {
      onset = dn.onsetOriginal();
      offset = dn.offsetOriginal();
      onsetInMSec = dn.onsetOriginalInMSec();
      offsetInMSec = dn.offsetOriginalInMSec();
      y = HEIGHT_PER_NOTE * (127 - dn.notenum());
      height = HEIGHT_PER_NOTE;
      updateScale();
    }

    public PrintableDeviatedNote getPair() {
      return pair;
    }

    void paint(Graphics g) {
      g.setColor(Color.BLACK);
      g.drawRect(x, y, width - 1, height - 1);
    }

    void paintAsSelected(Graphics g) {
      g.setColor(Color.BLACK);
      g.fillRect(x, y, width - 1, height - 1);
    }

    void updateScale() {
      if (MainFrame.getInstance().getShowAsTickTime()) {
        int ticksPerBeat = DeviatedPerformance.TICKS_PER_BEAT;
        x = onset * WIDTH_PER_BEAT / ticksPerBeat;
        width = (offset - onset) * WIDTH_PER_BEAT / ticksPerBeat;
      } else {
        int panelWidth = getPreferredSize().width;
        int milSecLength = (int) (getDeviatedPerformance().getSequence().getMicrosecondLength() / 1000);
        x = (int) (panelWidth * onsetInMSec / milSecLength);
        width = (int) (panelWidth * (offsetInMSec - onsetInMSec) / milSecLength);
      }
    }

  }

  public class PrintableDeviatedNote extends PrintableNote {

    private PrintableOriginalNote pair;
    private DeviatedNote deviatedNote;
    private Color fillColor;
    private Color roundColor;
    private short voice;

    private PrintableDeviatedNote(DeviatedNote deviatedNote) {
      this.deviatedNote = deviatedNote;
      updateScale();
      y = HEIGHT_PER_NOTE * (127 - deviatedNote.notenum());
      height = HEIGHT_PER_NOTE;
    }

    void paint(Graphics g) {
      if ((showVoice & voice) > 0 && !hideDeviatedNote) {
        g.setColor(fillColor);
        g.fillRect(x, y, width, height);
        g.setColor(roundColor);
        g.drawRect(x + 1, y + 1, width - 3, height - 3);
      }
    }

    private void paintAsHover(Graphics g) {
      g.setColor(roundColor);
      g.fillRect(x - 5, y - 5, width + 10, height + 10);
    }

    private void paintAsSelected(Graphics g) {
      g.setColor(Color.BLACK);
      for (int i = 0; i < 3; i++)
        g.drawRect(x - i, y - i, width + i * 2, height + i * 2);
    }

    void updateScale() {
      if (MainFrame.getInstance().getShowAsTickTime()) {
        int ticksPerBeat = DeviatedPerformance.TICKS_PER_BEAT;
        x = deviatedNote.onset() * WIDTH_PER_BEAT / ticksPerBeat;
        width = (deviatedNote.offset() - deviatedNote.onset()) * WIDTH_PER_BEAT
            / ticksPerBeat;
      } else {
        int panelWidth = getPreferredSize().width;
        int milSecLength = (int) (getDeviatedPerformance().getSequence().getMicrosecondLength() / 1000);
        x = (int) (panelWidth * deviatedNote.onsetInMSec() / milSecLength);
        width = (int) (panelWidth
            * (deviatedNote.offsetInMSec() - deviatedNote.onsetInMSec()) / milSecLength);
      }
      if (deviatedNote.isExtraNote()) {
        voice = 1;
        fillColor = new Color(255, 255, 0, deviatedNote.velocity() * 2);
        roundColor = Color.YELLOW;
      } else if (deviatedNote.getNote().voice() == 1) {
        voice = 2;
        fillColor = new Color(255, 0, 0, deviatedNote.velocity() * 2);
        roundColor = Color.RED;
      } else if (deviatedNote.getNote().voice() == 2) {
        voice = 4;
        fillColor = new Color(255, 127, 0, deviatedNote.velocity() * 2);
        roundColor = Color.ORANGE;
      } else if (deviatedNote.getNote().voice() == 3) {
        voice = 8;
        fillColor = new Color(255, 175, 175, deviatedNote.velocity() * 2);
        roundColor = Color.PINK;
      } else if (deviatedNote.getNote().voice() == 4) {
        voice = 16;
        fillColor = new Color(255, 0, 255, deviatedNote.velocity() * 2);
        roundColor = Color.MAGENTA;
      } else {
        voice = 32;
        fillColor = new Color(255, 0, 127, deviatedNote.velocity() * 2);
        roundColor = new Color(255, 0, 127);
      }
    }

    public boolean isMouseOnRight(int mouseX, int mouseY) {
      return isMouseOver(mouseX, mouseY) && mouseX > x + width / 2;
    }

    public int getX() {
      return x;
    }

    public void setX(int x) {
      this.x = x;
    }

    public int getY() {
      return y;
    }

    public int getWidth() {
      return width;
    }

    public void setWidth(int width) {
      this.width = width;
    }

    public int getHeight() {
      return height;
    }

    public DeviatedNote getDeviatedNote() {
      return deviatedNote;
    }

    public boolean show() {
      return (voice & showVoice) > 0;
    }

  }

  private class ColumnHeaderPanel extends JPanel {

    private int measureNum;
    private int widthPerMeasure;
    private int seconds;
    private int widthPerSecond;

    public ColumnHeaderPanel(int measureNum, int widthPerMeasure, int seconds,
        int widthPerSecond, int width, int height) {
      this.measureNum = measureNum;
      this.widthPerMeasure = widthPerMeasure;
      this.seconds = seconds;
      this.widthPerSecond = widthPerSecond;
      setPreferredSize(new Dimension(width, height));
    }

    public void paint(Graphics g) {
      super.paint(g);
      g.setColor(Color.BLACK);
      if (MainFrame.getInstance().getShowAsTickTime()) {
        for (int i = 0; i < measureNum; i++)
          g.drawString((i + 1) + "", i * widthPerMeasure, COLUMN_HEADER_HEIGHT);
      } else {
        for (int i = 0; i < seconds; i += 2) {
          g.drawString(i / 60 + ":" + i % 60, i * widthPerSecond,
              COLUMN_HEADER_HEIGHT);
        }
      }
    }
  }

}
