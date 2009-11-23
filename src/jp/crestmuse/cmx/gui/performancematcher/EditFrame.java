package jp.crestmuse.cmx.gui.performancematcher;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import jp.crestmuse.cmx.gui.deveditor.model.DeviatedPerformance;
import jp.crestmuse.cmx.gui.deveditor.view.PianoRollPanel;

public class EditFrame extends JFrame {

  public static Dimension VIEWPORT_DIM = new Dimension(720, 320);

  public EditFrame(DeviatedPerformance deviatedPerformance) throws IOException,
      InvalidMidiDataException {
    performancePanel = new PianoRollPanel(deviatedPerformance);
    scorePanel = new PianoRollPanel(deviatedPerformance);
    scorePanel.setHideDeviateNote(true);

    PerformanceMatcherController controller = new PerformanceMatcherController(
        performancePanel, scorePanel);
    performancePanel.addMouseListener(new PerformancePanelMouseListener(
        performancePanel, controller));
    scorePanel.addMouseListener(new ScorePanelMouseListener(scorePanel,
        controller));

    final JScrollPane north = new JScrollPane();
    north.setViewportView(performancePanel);
    north.getViewport().setPreferredSize(VIEWPORT_DIM);
    final JViewport south = new JViewport();
    south.setView(scorePanel);
    south.setPreferredSize(VIEWPORT_DIM);
    north.getViewport().addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        south.setViewPosition(north.getViewport().getViewPosition());
      }
    });
    add(north, BorderLayout.CENTER);
    add(south, BorderLayout.SOUTH);
    pack();
  }

  private PianoRollPanel performancePanel;
  private PianoRollPanel scorePanel;

}
