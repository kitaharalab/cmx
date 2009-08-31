package jp.crestmuse.cmx.gui.deveditor.controller;

import javax.sound.midi.InvalidMidiDataException;

import jp.crestmuse.cmx.gui.deveditor.model.DeviatedPerformance.DeviatedNote;

public class SetMissNote extends DeviatedNoteCommand {

  private boolean isMissNote;

  public SetMissNote(DeviatedNote deviatedNote, boolean isMissNote) {
    super(deviatedNote);
    this.isMissNote = isMissNote;
  }

  public void invoke() {
    try {
      deviatedNote.setMissNote(isMissNote);
    } catch (InvalidMidiDataException e) {
      e.printStackTrace();
    }
  }

  public void redo() {
    invoke();
  }

  public void undo() {
    try {
      deviatedNote.setMissNote(!isMissNote);
    } catch (InvalidMidiDataException e) {
      e.printStackTrace();
    }
  }

}
