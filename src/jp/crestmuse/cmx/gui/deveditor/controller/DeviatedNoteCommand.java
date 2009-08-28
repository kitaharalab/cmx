package jp.crestmuse.cmx.gui.deveditor.controller;

import jp.crestmuse.cmx.gui.deveditor.model.DeviatedPerformance.DeviatedNote;

public abstract class DeviatedNoteCommand implements Command {

  protected DeviatedNote deviatedNote;

  public DeviatedNoteCommand(DeviatedNote deviatedNote) {
    this.deviatedNote = deviatedNote;
  }

  public DeviatedNote getDeviatedNote() {
    return deviatedNote;
  }

}
