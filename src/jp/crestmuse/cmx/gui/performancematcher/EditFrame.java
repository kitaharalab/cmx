package jp.crestmuse.cmx.gui.performancematcher;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import jp.crestmuse.cmx.filewrappers.InvalidFileTypeException;
import jp.crestmuse.cmx.gui.deveditor.model.DeviatedPerformance;
import jp.crestmuse.cmx.gui.deveditor.view.PianoRollPanel;

public class EditFrame extends JFrame {

  public static Dimension VIEWPORT_DIM = new Dimension(720, 320);
  private PianoRollPanel performancePanel;
  private PianoRollPanel scorePanel;
  private FrameController frameController;

  public EditFrame(DeviatedPerformance deviatedPerformance, FrameController fc)
      throws IOException, InvalidMidiDataException {
    performancePanel = new PianoRollPanel(deviatedPerformance);
    scorePanel = new PianoRollPanel(deviatedPerformance);
    this.frameController = fc;
    scorePanel.setHideDeviateNote(true);

    PerformanceMatcherController controller = new PerformanceMatcherController(
        performancePanel, scorePanel, fc);
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

    addKeyListener(new KeyListener() {
      public void keyTyped(KeyEvent e) {
      }

      public void keyReleased(KeyEvent e) {
      }

      public void keyPressed(KeyEvent e) {
        if (e.getKeyChar() == 'r') {
          setVisible(false);
          Thread t = new Thread(new Runnable() {
            public void run() {
              try {
                frameController.reGenerateDeviation();
              } catch (Exception e) {
                e.printStackTrace();
              }
            }
          });
          t.start();
        } else if (e.getKeyChar() == 's') {
          try {
            frameController.export("out.xml");
          } catch (Exception e1) {
            e1.printStackTrace();
          }
        }
      }
    });
  }

}
