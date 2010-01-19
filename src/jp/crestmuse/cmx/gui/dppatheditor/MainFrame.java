package jp.crestmuse.cmx.gui.dppatheditor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.Map.Entry;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import jp.crestmuse.cmx.filewrappers.SCCXMLWrapper;
import jp.crestmuse.cmx.gui.deveditor.model.DeviatedPerformance;
import jp.crestmuse.cmx.gui.deveditor.model.DeviatedPerformance.DeviatedNote;
import jp.crestmuse.cmx.gui.deveditor.view.DeviatedPerformancePlayer;
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
  private DeviatedPerformancePlayer player;
  private PfmPanel pfmPanel;
  private JScrollPane north;
  private TempoCurve tempo;

  public MainFrame(DeviatedPerformance deviatedPerformance,
      PerformanceMatcher3 pm3, int[] score2pfm, DeviatedPerformancePlayer player) {
    this.pm3 = pm3;
    this.score2pfm = score2pfm;
    this.player = player;
    pfmNotes = new LinkedList<PfmNote>();
    scoreNotes = new LinkedList<ScoreNote>();
    int lastOffset = 0;
    for (DeviatedNote dn : deviatedPerformance.getDeviatedNotes()) {
      pfmNotes.add(new PfmNote(dn));
      if (!dn.isExtraNote()) {
        scoreNotes.add(new ScoreNote(dn));
        pfmNotes.getLast().pair = scoreNotes.getLast();
        scoreNotes.getLast().pair = pfmNotes.getLast();
      }
      lastOffset = Math.max(lastOffset, dn.offset());
    }
    
    // performance
    pfmPanel = new PfmPanel();
    ScorePanel scorePanel = new ScorePanel();
    Dimension dim = new Dimension(tick2position(lastOffset),
        notenum2position(128));
    pfmPanel.setPreferredSize(dim);
    scorePanel.setPreferredSize(dim);
    north = new JScrollPane();
    north.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
    north.getViewport().setPreferredSize(VIEWPORT_DIM);
    north.setViewportView(pfmPanel);
    
    // score
    final JViewport south = new JViewport();
    south.setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
    south.setPreferredSize(VIEWPORT_DIM);
    south.setView(scorePanel);
    north.getViewport().addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        south.setViewPosition(north.getViewport().getViewPosition());
      }
    });
    north.getViewport().setViewPosition(new Point(0, dim.height / 2));
    
    // tempo
    tempo = new TempoCurve(deviatedPerformance, dim.width);
    final JViewport tempoView = new JViewport();
    tempoView.setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
    tempoView.setPreferredSize(new Dimension(VIEWPORT_DIM.width, TempoCurve.PANEL_HEIGHT));
    tempoView.setView(tempo);
    north.getViewport().addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        int x = north.getViewport().getViewPosition().x;
        tempoView.setViewPosition(new Point(x, 0));
      }
    });
    
    // button
    JPanel buttons = new JPanel();
    final JButton play = new JButton("play");
    play.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(MainFrame.this.player.isNowPlaying()) {
          MainFrame.this.player.stop();
          play.setText("play");
        }else{
          MainFrame.this.player.play();
          play.setText("stop");
          Thread t = new Thread(new Repainter());
          t.start();
        }
      }
    });
    buttons.add(play);

    JPanel all = new JPanel();
    all.setLayout(new BoxLayout(all, BoxLayout.Y_AXIS));
    all.add(north);
    all.add(south);
    all.add(tempoView);
    all.add(buttons);
    add(all);
    pack();
  }

  public int[] getScore2pfm() {
    return score2pfm;
  }

  private int tick2position(int tick) {
    return (int) (tick * WIDTH_PER_TICK);
  }
  
  private int position2tick(int position) {
    return (int)(player.getCurrentSequence().getTickLength() * position / pfmPanel.getWidth());
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
      if (selectedPfmNote != null)
        selectedPfmNote.drawAsSelected(g);
      g.setColor(Color.GREEN);
      int x = tick2position((int) player.getTickPosition());
      g.drawLine(x, 0, x, getHeight());
    }

    private class PfmPanelMouseAdapter extends MouseAdapter {

      public void mousePressed(MouseEvent e) {
        for (PfmNote pn : pfmNotes) {
          if (pn.isClicked(e.getX(), e.getY())) {
            selectedPfmNote = pn;
            selectedScoreNote = pn.pair;
            MainFrame.this.repaint();
            return;
          }
        }
        selectedPfmNote = null;
        selectedScoreNote = null;
        if(e.getClickCount() >= 2)
          player.setTickPosition(position2tick(e.getX()));
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
      if (selectedScoreNote != null)
        selectedScoreNote.drawAsSelected(g);
    }

    class ScorePanelMouseAdapter extends MouseAdapter {

      public void mousePressed(MouseEvent e) {
        for (ScoreNote sn : scoreNotes)
          if (sn.isClicked(e.getX(), e.getY())) {
            int dst = pm3.getMusicxmlwrappernote2Index().get(
                sn.deviatedNote.getNote());
            if (sn == selectedScoreNote) {
              score2pfm[dst] = -1;
              sn.pair = null;
              selectedPfmNote.pair = null;
              selectedScoreNote = null;
              selectedPfmNote.edited();
              MainFrame.this.repaint();
              return;
            }
            if (selectedPfmNote.pair == null) {
              int index = -1;
              int diff = Integer.MAX_VALUE;
              for (Entry<SCCXMLWrapper.Note, Integer> en : pm3.getExtraNoteMap().entrySet()) {
                if (en.getKey().notenum() != sn.deviatedNote.notenum())
                  continue;
                int d = Math.abs(en.getKey().onsetInMSec()
                    - sn.deviatedNote.onsetInMSec());
                if (d < diff) {
                  index = en.getValue();
                  diff = d;
                }
              }
              if (index != -1) {
                score2pfm[dst] = index;
                if (sn.pair != null)
                  sn.pair.pair = null;
                selectedPfmNote.pair = sn;
                sn.pair = selectedPfmNote;
                selectedScoreNote = sn;
                selectedPfmNote.edited();
                MainFrame.this.repaint();
              }
              return;
            }
            int src = pm3.getMusicxmlwrappernote2Index().get(
                selectedPfmNote.deviatedNote.getNote());
            score2pfm[dst] = score2pfm[src];
            score2pfm[src] = -1;
            if (sn.pair != null)
              sn.pair.pair = null;
            sn.pair = selectedPfmNote;
            selectedPfmNote.pair.pair = null;
            selectedPfmNote.pair = sn;
            selectedScoreNote = sn;
            selectedPfmNote.edited();
            MainFrame.this.repaint();
            break;
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
      if (deviatedNote.getIsMissNote()) {
        g.setColor(Color.BLACK);
        g.drawString("miss", x, y);
      }
    }

    void drawAsSelected(Graphics g) {
      g.setColor(Color.BLACK);
      g.fillRect(x, y, width, height);
      if (deviatedNote.getIsMissNote()) {
        g.setColor(Color.RED);
        g.drawString("miss", x, y);
      }
    }

    void edited() {
      this.color = Color.BLUE;
      this.roundColor = Color.BLUE;
    }

  }
  
  private class Repainter implements Runnable {

    public void run() {
      while(player.isNowPlaying()) {
        pfmPanel.repaint();
        int x = tick2position((int)player.getTickPosition()) - north.getWidth() / 2;
        x = Math.max(x, 0);
        Point p = north.getViewport().getViewPosition();
        p.x = x;
        north.getViewport().setViewPosition(p);
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }

  }

}
