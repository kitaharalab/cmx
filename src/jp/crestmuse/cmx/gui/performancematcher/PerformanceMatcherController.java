package jp.crestmuse.cmx.gui.performancematcher;

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
    System.out.println(selectedDeviatedNote.getDeviatedNote() == selectedNote.getPair().getDeviatedNote());
//      return;
  }

}
