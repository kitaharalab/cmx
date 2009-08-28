package jp.crestmuse.cmx.gui.deveditor.controller;

import jp.crestmuse.cmx.gui.deveditor.model.DeviatedPerformance.DeviatedNote;

public interface DeviatedNoteListener {
  public void noteSelected(DeviatedNote selectedNote);
  public void noteUpdated(DeviatedNote updatedNote);
}
