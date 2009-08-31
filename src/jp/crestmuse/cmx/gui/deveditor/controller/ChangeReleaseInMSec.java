package jp.crestmuse.cmx.gui.deveditor.controller;

import javax.sound.midi.InvalidMidiDataException;

import jp.crestmuse.cmx.gui.deveditor.model.DeviatedPerformance.DeviatedNote;

public class ChangeReleaseInMSec extends DeviatedNoteCommand {

  private int targetMSec;
  private int prevMSec;

  public ChangeReleaseInMSec(DeviatedNote deviatedNote, int targetMSec) {
    super(deviatedNote);
    this.targetMSec = targetMSec;
    prevMSec = deviatedNote.offsetInMSec();
  }

  public void invoke() {
    try {
      deviatedNote.changeReleaseInMsec(targetMSec);
    } catch (InvalidMidiDataException e) {
      e.printStackTrace();
    }
  }

  public void redo() {
    invoke();
  }

  public void undo() {
    try {
      deviatedNote.changeReleaseInMsec(prevMSec);
    } catch (InvalidMidiDataException e) {
      e.printStackTrace();
    }
  }

}
