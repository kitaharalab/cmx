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

import javax.sound.midi.InvalidMidiDataException;
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
public class PianoRollPanel extends JPanel implements MouseListener,
    MouseMotionListener, ChangeListener, DeviatedNoteSelectListener,
    DeviatedNoteUpdateListener {

  public static int WIDTH_PER_BEAT = 32;
  public static int HEIGHT_PER_NOTE = 16;
  public static int COLUMN_HEADER_HEIGHT = 16;
  private DeviatedPerformance deviatedPerformance;
  private ArrayList<PrintableDeviatedNote> deviatedNotes;
  private ArrayList<PrintableNote> allNotes;
  private PrintableDeviatedNote hoverNote;
  private PrintableDeviatedNote.NoteMoveHandle holdNote;
  private PrintableDeviatedNote selectedNote;
  private ColumnHeaderPanel columnHeader;
  private int playingLine;
  private HashMap<DeviatedNote, PrintableDeviatedNote> dn2pdn;
  private DeviatedNoteControler deviatedNoteControler;
  private static short showVoice = 63;

  public PianoRollPanel(DeviatedPerformance deviatedPerformance,
      DeviatedNoteControler deviatedNoteControler) {
    this.deviatedPerformance = deviatedPerformance;
    this.deviatedNoteControler = deviatedNoteControler;
    deviatedNoteControler.addDeviatedNoteSelectListener(this);
    playingLine = 0;
    deviatedNotes = new ArrayList<PrintableDeviatedNote>();
    allNotes = new ArrayList<PrintableNote>();
    dn2pdn = new HashMap<DeviatedNote, PrintableDeviatedNote>();
    for (DeviatedPerformance.DeviatedNote dn : deviatedPerformance.getDeviatedNotes()) {
      PrintableDeviatedNote pdn = new PrintableDeviatedNote(dn);
      deviatedNotes.add(pdn);
      allNotes.add(pdn);
      if (!dn.isExtraNote()) {
        allNotes.add(new PrintableOriginalNote(dn));
      }
      dn2pdn.put(dn, pdn);
    }
    holdNote = null;
    int tickLength = (int) deviatedPerformance.getSequence().getTickLength();
    int width = WIDTH_PER_BEAT * tickLength
        / DeviatedPerformance.TICKS_PER_BEAT;
    setPreferredSize(new Dimension(width, HEIGHT_PER_NOTE * 128));
    addMouseListener(this);
    addMouseMotionListener(this);

    int measures = tickLength / (DeviatedPerformance.TICKS_PER_BEAT * 4);
    int seconds = (int) (deviatedPerformance.getSequence().getMicrosecondLength() / 1000000);
    columnHeader = new ColumnHeaderPanel(measures, WIDTH_PER_BEAT * 4, seconds,
        width / seconds, width, COLUMN_HEADER_HEIGHT);
  }

  public void mouseClicked(MouseEvent e) {
    if (e.getClickCount() == 2 && hoverNote == null && holdNote == null) {
      if (MainFrame.getInstance().getShowAsTickTime())
        MainFrame.getInstance().setPlayPosition(
            deviatedPerformance.getSequence().getTickLength() * e.getX()
                / getPreferredSize().width);
      else
        MainFrame.getInstance().setPlayPosition(
            deviatedPerformance.getSequence().getMicrosecondLength() * e.getX()
                / getPreferredSize().width);
    } else if (e.getButton() == MouseEvent.BUTTON3 && hoverNote != null) {
      hoverNote = null;
      repaint();
    }
  }

  public void mouseEntered(MouseEvent e) {
  }

  public void mouseExited(MouseEvent e) {
  }

  public void mousePressed(MouseEvent e) {
    if (e.getButton() != MouseEvent.BUTTON1)
      return;
    if (selectedNote != null) {
      holdNote = selectedNote.getHandle(e.getX(), e.getY());
      if (holdNote != null) {
        try {
          Point p = MouseInfo.getPointerInfo().getLocation();
          Robot r = new Robot();
          r.mouseMove(p.x + holdNote.press(e.getX()), p.y);
        } catch (AWTException e1) {
          e1.printStackTrace();
        }
        return;
      }
    }
    for (PrintableDeviatedNote d : deviatedNotes) {
      // holdNote = d.getHandle(e.getX(), e.getY());
      // if (holdNote != null) {
      // hoverNote = null;
      // try {
      // Point p = MouseInfo.getPointerInfo().getLocation();
      // Robot r = new Robot();
      // r.mouseMove(p.x + holdNote.press(e.getX()), p.y);
      // } catch (AWTException e1) {
      // e1.printStackTrace();
      // }
      // break;
      // }
      if (!(e.getX() < d.x || e.getX() > d.x + d.width || e.getY() < d.y || e.getY() > d.y
          + d.height)) {
        deviatedNoteControler.select(d.deviatedNote);
        break;
      }
    }
  }

  public void mouseReleased(MouseEvent e) {
    if (holdNote != null) {
      holdNote.release();
      holdNote = null;
    }
  }

  public void mouseDragged(MouseEvent e) {
    if (holdNote != null) {
      holdNote.drag(e.getX());
      repaint();
    }
  }

  public void mouseMoved(MouseEvent e) {
    if (holdNote == null) {
      hoverNote = null;
      for (PrintableDeviatedNote p : deviatedNotes)
        if (p.isMouseOver(e.getX(), e.getY())) {
          hoverNote = p;
          if (p.isMouseOnRight(e.getX(), e.getY()))
            setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
          else
            setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
          break;
        }
      if (hoverNote == null)
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      repaint();
    }
  }

  public void stateChanged(ChangeEvent e) {
    updateScale();
    repaint();
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
    if (selectedNote != null)
      selectedNote.paintAsSelected(g);
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

  public static void toggleExtra(boolean b) {
    if(b)
      showVoice |= 1;
    else
      showVoice &= 62;
  }

  public static void toggleVoice1(boolean b) {
    if(b)
      showVoice |= 2;
    else
      showVoice &= 61;
  }

  public static void toggleVoice2(boolean b) {
    if(b)
      showVoice |= 4;
    else
      showVoice &= 59;
  }

  public static void toggleVoice3(boolean b) {
    if(b)
      showVoice |= 8;
    else
      showVoice &= 55;
  }

  public static void toggleVoice4(boolean b) {
    if(b)
      showVoice |= 16;
    else
      showVoice &= 47;
  }

  public static void toggleVoiceOther(boolean b) {
    if(b)
      showVoice |= 32;
    else
      showVoice &= 31;
  }

  private abstract class PrintableNote {
    int x, y, width, height;

    abstract void updateScale();

    abstract void paint(Graphics g);
  }

  private class PrintableOriginalNote extends PrintableNote {

    private int onset, offset, onsetInMSec, offsetInMSec;

    public PrintableOriginalNote(DeviatedNote dn) {
      onset = dn.onsetOriginal();
      offset = dn.offsetOriginal();
      onsetInMSec = dn.onsetOriginalInMSec();
      offsetInMSec = dn.offsetOriginalInMSec();
      y = HEIGHT_PER_NOTE * (127 - dn.notenum());
      height = HEIGHT_PER_NOTE;
      updateScale();
    }

    public void paint(Graphics g) {
      g.setColor(Color.BLACK);
      g.drawRect(x, y, width - 1, height - 1);
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

  private class PrintableDeviatedNote extends PrintableNote {

    private DeviatedNote deviatedNote;
    private Color fillColor;
    private Color roundColor;
    private short voice;

    public PrintableDeviatedNote(DeviatedNote deviatedNote) {
      this.deviatedNote = deviatedNote;
      updateScale();
      y = HEIGHT_PER_NOTE * (127 - deviatedNote.notenum());
      height = HEIGHT_PER_NOTE;
    }

    public void paint(Graphics g) {
      if ((showVoice & voice) > 0) {
        g.setColor(fillColor);
        g.fillRect(x, y, width, height);
        g.setColor(roundColor);
        g.drawRect(x + 1, y + 1, width - 3, height - 3);
      }
    }

    public void paintAsHover(Graphics g) {
      g.setColor(roundColor);
      g.fillRect(x - 5, y - 5, width + 10, height + 10);
    }

    public void paintAsSelected(Graphics g) {
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
      } else if(deviatedNote.getNote().voice() == 2) {
        voice = 4;
        fillColor = new Color(255, 127, 0, deviatedNote.velocity() * 2);
        roundColor = Color.ORANGE;
      } else if(deviatedNote.getNote().voice() == 3) {
        voice = 8;
        fillColor = new Color(255, 175, 175, deviatedNote.velocity() * 2);
        roundColor = Color.PINK;
      } else if(deviatedNote.getNote().voice() == 4) {
        voice = 16;
        fillColor = new Color(255, 0, 255, deviatedNote.velocity() * 2);
        roundColor = Color.MAGENTA;
      } else {
        voice = 32;
        fillColor = new Color(255, 0, 127, deviatedNote.velocity() * 2);
        roundColor = new Color(255, 0, 127);
      }
    }

    public boolean isMouseOver(int mouseX, int mouseY) {
      return mouseX > x && mouseX < x + width && mouseY > y
          && mouseY < y + height;
    }

    public boolean isMouseOnRight(int mouseX, int mouseY) {
      return isMouseOver(mouseX, mouseY) && mouseX > x + width / 2;
    }

    /**
     * DeviatedNoteのchangeDeviationを呼び出して位置と色を更新する．
     */
    public boolean changeDeviation(double attack, double release,
        double dynamics, double endDynamics) {
      try {
        if (deviatedNote.changeDeviation(attack, release, dynamics, endDynamics)) {
          // if(MainFrame.getInstance().getShowAsTickTime())
          // asTickTime();
          // else
          // asRealTime();
          updateScale();
          fillColor = new Color(fillColor.getRed(), fillColor.getGreen(),
              fillColor.getBlue(), deviatedNote.velocity() * 2);
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
        fillColor = new Color(fillColor.getRed(), fillColor.getGreen(),
            fillColor.getBlue(), deviatedNote.velocity() * 2);
      } catch (InvalidMidiDataException e) {
        e.printStackTrace();
      }
    }

    public NoteMoveHandle getHandle(int mouseX, int mouseY) {
      if (isMouseOver(mouseX, mouseY)) {
        if (mouseX > x + width / 2)
          return new NoteMoveHandle(false, x, x, width);
        else
          return new NoteMoveHandle(true, x + width, x, width);
      }
      return null;
    }

    class NoteMoveHandle {
      // TODO 実時刻表時のとき
      private boolean noteon;
      private int limit, prevX, prevWidth;

      private NoteMoveHandle(boolean noteon, int limit, int prevX, int prevWidth) {
        this.noteon = noteon;
        this.limit = limit;
        this.prevX = prevX;
        this.prevWidth = prevWidth;
      }

      public int press(int posX) {
        if (noteon)
          return prevX - posX;
        return prevX + prevWidth - posX;
      }

      public void drag(int posX) {
        if (noteon) {
          x = Math.min(posX, limit - 1);
          width = limit - x;
        } else {
          width = Math.max(posX - x, 1);
        }
      }

      public void release() {
        if (MainFrame.getInstance().getShowAsTickTime()) {
          if (noteon) {
            // deviatedNote.changeDeviation((x - prevX)
            // / (double) PianoRollPanel.WIDTH_PER_BEAT, 0);
            ChangeDeviation cd = new ChangeDeviation(deviatedNote, (x - prevX)
                / (double) WIDTH_PER_BEAT, 0);
            deviatedNoteControler.update(cd);
          } else {
            // deviatedNote.changeDeviation(0, (width - prevWidth)
            // / (double) PianoRollPanel.WIDTH_PER_BEAT);
            ChangeDeviation cd = new ChangeDeviation(deviatedNote, 0,
                (width - prevWidth) / (double) WIDTH_PER_BEAT);
            deviatedNoteControler.update(cd);
          }
        } else {
          int msecLength = (int) (getDeviatedPerformance().getSequence().getMicrosecondLength() / 1000);
          int panelWidth = getPreferredSize().width;
          if (noteon) {
            // deviatedNote.changeAttackInMsec(x * msecLength / panelWidth);
            deviatedNoteControler.update(new ChangeAttackInMSec(deviatedNote, x
                * msecLength / panelWidth));
          } else {
            // deviatedNote.changeReleaseInMsec((x + width) * msecLength
            // / panelWidth);
            deviatedNoteControler.update(new ChangeReleaseInMSec(deviatedNote,
                (x + width) * msecLength / panelWidth));
          }
        }
      }
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
