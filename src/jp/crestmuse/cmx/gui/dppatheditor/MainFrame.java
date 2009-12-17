package jp.crestmuse.cmx.gui.dppatheditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.Map.Entry;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import jp.crestmuse.cmx.filewrappers.SCCXMLWrapper;
import jp.crestmuse.cmx.gui.deveditor.model.DeviatedPerformance;
import jp.crestmuse.cmx.gui.deveditor.model.DeviatedPerformance.DeviatedNote;
import jp.crestmuse.cmx.misc.PerformanceMatcher3;

public class MainFrame extends JFrame {

  private static Dimension VIEWPORT_DIM = new Dimension(720, 320);
  private static final float WIDTH_PER_TICK = 0.075f;
  private static final int HEIGHT_PER_NOTE = 16;
  private LinkedList<PfmNote> pfmNotes;
  private LinkedList<ScoreNote> scoreNotes;
  private PfmNote selectedPfmNote;
  private ScoreNote selectedScoreNote;
  private PerformanceMatcher3 pm3;
  private int[] score2pfm;

  public MainFrame(DeviatedPerformance deviatedPerformance, PerformanceMatcher3 pm3, int[] score2pfm) {
    this.pm3 = pm3;
    this.score2pfm = score2pfm;
    pfmNotes = new LinkedList<PfmNote>();
    scoreNotes = new LinkedList<ScoreNote>();
    int lastOffset = 0;
    for (DeviatedNote dn : deviatedPerformance.getDeviatedNotes()) {
      pfmNotes.add(new PfmNote(dn));
      if(!dn.isExtraNote()) {
        scoreNotes.add(new ScoreNote(dn));
        pfmNotes.getLast().pair = scoreNotes.getLast();
        scoreNotes.getLast().pair = pfmNotes.getLast();
      }
      lastOffset = Math.max(lastOffset, dn.offset());
    }
    PfmPanel pfmPanel = new PfmPanel();
    ScorePanel scorePanel = new ScorePanel();
    Dimension dim = new Dimension(tick2position(lastOffset),
        notenum2position(128));
    pfmPanel.setPreferredSize(dim);
    scorePanel.setPreferredSize(dim);
    final JScrollPane north = new JScrollPane();
    north.getViewport().setPreferredSize(VIEWPORT_DIM);
    north.setViewportView(pfmPanel);
    final JViewport south = new JViewport();
    south.setPreferredSize(VIEWPORT_DIM);
    south.setView(scorePanel);
    north.getViewport().addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        south.setViewPosition(north.getViewport().getViewPosition());
      }
    });
    north.getViewport().setViewPosition(new Point(0, dim.height / 2));
    add(north, BorderLayout.CENTER);
    add(south, BorderLayout.SOUTH);
    pack();
  }
  
  public int[] getScore2pfm() {
    return score2pfm;
  }

  private int tick2position(int tick) {
    return (int) (tick * WIDTH_PER_TICK);
  }

  private int notenum2position(int notenum) {
    return notenum * HEIGHT_PER_NOTE;
  }

  private class PfmPanel extends JPanel {
    
    PfmPanel() {
      addMouseListener(new PfmPanelMouseAdapter());
    }

    public void paint(Graphics g) {
      super.paint(g);
      for (PfmNote pn : pfmNotes)
        pn.draw(g);
      if(selectedPfmNote != null)
        selectedPfmNote.drawAsSelected(g);
    }
    
    private class PfmPanelMouseAdapter extends MouseAdapter {

      public void mousePressed(MouseEvent e) {
        for(PfmNote pn : pfmNotes) {
          if(pn.isClicked(e.getX(), e.getY())) {
            selectedPfmNote = pn;
            selectedScoreNote = pn.pair;
            MainFrame.this.repaint();
            return;
          }
        }
        selectedPfmNote = null;
        selectedScoreNote = null;
        MainFrame.this.repaint();
      }

    }

  }

  private class ScorePanel extends JPanel {
    
    ScorePanel() {
      addMouseListener(new ScorePanelMouseAdapter());
    }

    public void paint(Graphics g) {
      super.paint(g);
      for (ScoreNote sn : scoreNotes)
        sn.draw(g);
      if(selectedScoreNote != null)
        selectedScoreNote.drawAsSelected(g);
    }
    
    class ScorePanelMouseAdapter extends MouseAdapter {
      
      public void mousePressed(MouseEvent e) {
        for(ScoreNote sn : scoreNotes)
          if(sn.isClicked(e.getX(), e.getY())) {
            int dst = pm3.getMusicxmlwrappernote2Index().get(sn.deviatedNote.getNote());
            if(sn == selectedScoreNote) {
              score2pfm[dst] = -1;
              sn.pair = null;
              selectedPfmNote.pair = null;
              selectedScoreNote = null;
              MainFrame.this.repaint();
              return;
            }
            if(selectedPfmNote.pair == null) {
              int index = -1;
              int diff = Integer.MAX_VALUE;
              for(Entry<SCCXMLWrapper.Note, Integer> en : pm3.getExtraNoteMap().entrySet()) {
                if(en.getKey().notenum() != sn.deviatedNote.notenum())
                  continue;
                int d = Math.abs(en.getKey().onsetInMSec() - sn.deviatedNote.onsetInMSec());
                if(d < diff) {
                  index = en.getValue();
                  diff = d;
                }
              }
              if(index != -1) {
                score2pfm[dst] = index;
                if(sn.pair != null)
                  sn.pair.pair = null;
                selectedPfmNote.pair = sn;
                sn.pair = selectedPfmNote;
                selectedScoreNote = sn;
                MainFrame.this.repaint();
              }
              return;
            }
            int src = pm3.getMusicxmlwrappernote2Index().get(selectedPfmNote.deviatedNote.getNote());
            score2pfm[dst] = score2pfm[src];
            score2pfm[src] = -1;
            if(sn.pair != null)
              sn.pair.pair = null;
            sn.pair = selectedPfmNote;
            selectedPfmNote.pair.pair = null;
            selectedPfmNote.pair = sn;
            selectedScoreNote = sn;
            MainFrame.this.repaint();
          }
      }

    }

  }

  private class ScoreNote {

    ScoreNote pair;
    DeviatedNote deviatedNote;
    int x, y, width, height;
    Color color;

    ScoreNote(DeviatedNote note) {
      deviatedNote = note;
      x = tick2position(note.onsetOriginal()) + 1;
      y = notenum2position(note.notenum()) + 1;
      width = tick2position(note.offsetOriginal()) - x - 2;
      height = notenum2position(note.notenum() + 1) - y - 2;
      color = Color.BLACK;
    }

    void draw(Graphics g) {
      g.setColor(color);
      g.drawRect(x, y, width, height);
    }
    
    void drawAsSelected(Graphics g) {
      g.setColor(color);
      g.fillRect(x, y, width, height);
    }
    
    boolean isClicked(int px, int py) {
      return px >= x && px <= x + width && py >= y && py <= y + height;
    }

  }

  private class PfmNote extends ScoreNote {

    boolean enable;
    int voice;
    Color roundColor;

    PfmNote(DeviatedNote note) {
      super(note);
      x = tick2position(note.onset()) + 1;
      width = tick2position(note.offset()) - x - 2;
      if (deviatedNote.isExtraNote()) {
        voice = 1;
        color = new Color(255, 255, 0, deviatedNote.velocity() * 2);
        roundColor = Color.YELLOW;
      } else if (deviatedNote.getNote().voice() == 1) {
        voice = 2;
        color = new Color(255, 0, 0, deviatedNote.velocity() * 2);
        roundColor = Color.RED;
      } else if (deviatedNote.getNote().voice() == 2) {
        voice = 4;
        color = new Color(255, 127, 0, deviatedNote.velocity() * 2);
        roundColor = Color.ORANGE;
      } else if (deviatedNote.getNote().voice() == 3) {
        voice = 8;
        color = new Color(255, 175, 175, deviatedNote.velocity() * 2);
        roundColor = Color.PINK;
      } else if (deviatedNote.getNote().voice() == 4) {
        voice = 16;
        color = new Color(255, 0, 255, deviatedNote.velocity() * 2);
        roundColor = Color.MAGENTA;
      } else {
        voice = 32;
        color = new Color(255, 0, 127, deviatedNote.velocity() * 2);
        roundColor = new Color(255, 0, 127);
      }
    }
    
    void draw(Graphics g) {
      g.setColor(color);
      g.fillRect(x - 1, y - 1, width + 2, height + 2);
      g.setColor(roundColor);
      g.drawRect(x, y, width, height);
    }
    
    void drawAsSelected(Graphics g) {
      g.setColor(Color.BLACK);
      g.fillRect(x, y, width, height);
    }

  }

}
