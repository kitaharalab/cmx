package jp.crestmuse.cmx.gui.deveditor.controller;

import java.awt.AWTException;
import java.awt.Cursor;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import jp.crestmuse.cmx.gui.deveditor.view.MainFrame;
import jp.crestmuse.cmx.gui.deveditor.view.PianoRollPanel;
import jp.crestmuse.cmx.gui.deveditor.view.PianoRollPanel.PrintableDeviatedNote;

public class PrintableDeviatedNoteMoveController implements MouseListener,
    MouseMotionListener {

  private PianoRollPanel pianoRollPanel;
  private NoteMoveHandle holdNote;
  private DeviatedNoteControler deviatedNoteController;

  public PrintableDeviatedNoteMoveController(PianoRollPanel pianoRollPanel,
      DeviatedNoteControler deviatedNoteController) {
    this.pianoRollPanel = pianoRollPanel;
    this.deviatedNoteController = deviatedNoteController;
  }

  public void mouseClicked(MouseEvent e) {
    if (e.getClickCount() == 2 && !pianoRollPanel.existHoverNote()
        && holdNote == null) {
      if (MainFrame.getInstance().getShowAsTickTime())
        MainFrame.getInstance().setPlayPosition(
            pianoRollPanel.getDeviatedPerformance().getSequence().getTickLength()
                * e.getX() / pianoRollPanel.getPreferredSize().width);
      else
        MainFrame.getInstance().setPlayPosition(
            pianoRollPanel.getDeviatedPerformance().getSequence().getMicrosecondLength()
                * e.getX() / pianoRollPanel.getPreferredSize().width);
    } else if (e.getButton() == MouseEvent.BUTTON3
        && pianoRollPanel.existHoverNote()) {
      pianoRollPanel.releaseHoverNote();
      pianoRollPanel.repaint();
    }
  }

  public void mouseEntered(MouseEvent e) {
  }

  public void mouseExited(MouseEvent e) {
  }

  public void mousePressed(MouseEvent e) {
    if (e.getButton() != MouseEvent.BUTTON1)
      return;
    PrintableDeviatedNote selectedNote = pianoRollPanel.getSelectedNote();
    if (selectedNote != null) {
      if (selectedNote.isMouseOver(e.getX(), e.getY())) {
        if (e.getX() > selectedNote.getX() + selectedNote.getWidth() / 2) {
          holdNote = new OffsetMoveHandle(selectedNote.getX(),
              selectedNote.getX(), selectedNote.getWidth());
        } else {
          holdNote = new OnsetMoveHandle(selectedNote.getX()
              + selectedNote.getWidth(), selectedNote.getX(),
              selectedNote.getWidth());
        }
        try {
          Point p = MouseInfo.getPointerInfo().getLocation();
          Robot r = new Robot();
          r.mouseMove(p.x + holdNote.press(e.getX()), p.y);
        } catch (AWTException e1) {
          e1.printStackTrace();
        }
        return;
      } else
        holdNote = null;
    }
    for (PrintableDeviatedNote d : pianoRollPanel.getDeviatedNotes()) {
      if (d.show() && d.isMouseOver(e.getX(), e.getY())) {
        deviatedNoteController.select(d.getDeviatedNote());
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
      pianoRollPanel.repaint();
    }
  }

  public void mouseMoved(MouseEvent e) {
    if (holdNote != null)
      return;
    pianoRollPanel.releaseHoverNote();
    for (PrintableDeviatedNote p : pianoRollPanel.getDeviatedNotes())
      if (p.isMouseOver(e.getX(), e.getY()) && p.show()) {
        pianoRollPanel.setHoverNote(p);
        if (p.isMouseOnRight(e.getX(), e.getY()))
          pianoRollPanel.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
        else
          pianoRollPanel.setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
        break;
      }
    if (pianoRollPanel.getHoverNote() == null)
      pianoRollPanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    pianoRollPanel.repaint();
  }

  private abstract class NoteMoveHandle {
    // TODO 実時刻表時のとき
    int limit, prevX, prevWidth;

    private NoteMoveHandle(int limit, int prevX, int prevWidth) {
      this.limit = limit;
      this.prevX = prevX;
      this.prevWidth = prevWidth;
    }

    abstract int press(int posX);

    abstract void drag(int posX);

    abstract void release();
  }

  private class OnsetMoveHandle extends NoteMoveHandle {

    private OnsetMoveHandle(int limit, int prevX, int prevWidth) {
      super(limit, prevX, prevWidth);
    }

    int press(int posX) {
      return prevX - posX;
    }

    void drag(int posX) {
      PrintableDeviatedNote pd = pianoRollPanel.getSelectedNote();
      pd.setX(Math.min(posX, limit - 1));
      pd.setWidth(limit - pd.getX());
    }

    void release() {
      PrintableDeviatedNote pd = pianoRollPanel.getSelectedNote();
      if (MainFrame.getInstance().getShowAsTickTime()) {
        ChangeDeviation cd = new ChangeDeviation(pd.getDeviatedNote(),
            (pd.getX() - prevX) / (double) PianoRollPanel.WIDTH_PER_BEAT, 0);
        deviatedNoteController.update(cd);
      } else {
        int msecLength = (int) (pianoRollPanel.getDeviatedPerformance().getSequence().getMicrosecondLength() / 1000);
        int panelWidth = pianoRollPanel.getPreferredSize().width;
        deviatedNoteController.update(new ChangeAttackInMSec(
            pd.getDeviatedNote(), pd.getX() * msecLength / panelWidth));
      }
    }
  }

  private class OffsetMoveHandle extends NoteMoveHandle {

    OffsetMoveHandle(int limit, int prevX, int prevWidth) {
      super(limit, prevX, prevWidth);
    }

    int press(int posX) {
      return prevX + prevWidth - posX;
    }

    void drag(int posX) {
      PrintableDeviatedNote pd = pianoRollPanel.getSelectedNote();
      pd.setWidth(Math.max(posX - pd.getX(), 1));
    }

    void release() {
      PrintableDeviatedNote pd = pianoRollPanel.getSelectedNote();
      if (MainFrame.getInstance().getShowAsTickTime()) {
        ChangeDeviation cd = new ChangeDeviation(pd.getDeviatedNote(), 0,
            (pd.getWidth() - prevWidth)
                / (double) PianoRollPanel.WIDTH_PER_BEAT);
        deviatedNoteController.update(cd);
      } else {
        int msecLength = (int) (pianoRollPanel.getDeviatedPerformance().getSequence().getMicrosecondLength() / 1000);
        int panelWidth = pianoRollPanel.getPreferredSize().width;
        deviatedNoteController.update(new ChangeReleaseInMSec(
            pd.getDeviatedNote(), (pd.getX() + pd.getWidth()) * msecLength
                / panelWidth));
      }
    }

  }

}
