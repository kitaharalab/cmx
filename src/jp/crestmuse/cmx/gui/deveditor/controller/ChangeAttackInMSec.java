package jp.crestmuse.cmx.gui.deveditor.controller;

import javax.sound.midi.InvalidMidiDataException;

import jp.crestmuse.cmx.gui.deveditor.model.DeviatedPerformance.DeviatedNote;

public class ChangeAttackInMSec extends DeviatedNoteCommand {

  private int targetMSec;
  private int prevMSec;

  public ChangeAttackInMSec(DeviatedNote deviatedNote, int targetMSec) {
    super(deviatedNote);
    prevMSec = deviatedNote.onsetInMSec();
    this.targetMSec = targetMSec;
  }

  public void invoke() {
    try {
      deviatedNote.changeAttackInMsec(targetMSec);
    } catch (InvalidMidiDataException e) {
      e.printStackTrace();
    }
  }

  public void redo() {
    invoke();
  }

  public void undo() {
    try {
      deviatedNote.changeAttackInMsec(prevMSec);
    } catch (InvalidMidiDataException e) {
      e.printStackTrace();
    }
  }

}
