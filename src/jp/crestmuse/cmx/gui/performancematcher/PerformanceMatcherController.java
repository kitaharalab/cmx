package jp.crestmuse.cmx.gui.performancematcher;

import javax.sound.midi.InvalidMidiDataException;

import jp.crestmuse.cmx.gui.deveditor.model.DeviatedPerformance.DeviatedNote;
import jp.crestmuse.cmx.gui.deveditor.view.PianoRollPanel;
import jp.crestmuse.cmx.gui.deveditor.view.PianoRollPanel.PrintableDeviatedNote;
import jp.crestmuse.cmx.gui.deveditor.view.PianoRollPanel.PrintableOriginalNote;

public class PerformanceMatcherController {

  private PianoRollPanel performancePanel;
  private PianoRollPanel scorePanel;
  private PrintableDeviatedNote selectedDeviatedNote;

  public PerformanceMatcherController(PianoRollPanel performancePanel,
      PianoRollPanel scorePanel) {
    this.performancePanel = performancePanel;
    this.scorePanel = scorePanel;
  }

  public void selectPerformance(PrintableDeviatedNote selectedNote) {
    performancePanel.noteSelected(selectedNote.getDeviatedNote());
    scorePanel.noteSelected(selectedNote.getDeviatedNote());
    selectedDeviatedNote = selectedNote;
  }

  public void selectScore(PrintableOriginalNote selectedNote) {
    if (selectedDeviatedNote.getDeviatedNote() == selectedNote.getPair().getDeviatedNote())
      return;
    try {
      DeviatedNote srcDn = selectedDeviatedNote.getDeviatedNote();
      DeviatedNote dstDn = selectedNote.getPair().getDeviatedNote();
      dstDn.changeDeviation((srcDn.onset(480) - dstDn.onset(480)) / 480.0,
          (srcDn.offset(480) - dstDn.offset(480)) / 480.0);
      srcDn.setMissNote(true);
      performancePanel.updateScale();
      performancePanel.repaint();
    } catch (InvalidMidiDataException e) {
      e.printStackTrace();
    }
  }

}
