package jp.crestmuse.cmx.gui.deveditor.controller;

import java.util.LinkedList;
import java.util.List;

import jp.crestmuse.cmx.gui.deveditor.model.DeviatedPerformance.DeviatedNote;

public class DeviatedNoteControler {

  private CommandInvoker commandInvoker;
  private List<DeviatedNoteListener> listeners;

  public DeviatedNoteControler(CommandInvoker commandInvoker) {
    this.commandInvoker = commandInvoker;
    listeners = new LinkedList<DeviatedNoteListener>();
  }

  public void addDeviatedNoteListener(DeviatedNoteListener listener) {
    listeners.add(listener);
  }

  public void select(DeviatedNote selectedNote) {
    for(DeviatedNoteListener l : listeners)
      l.noteSelected(selectedNote);
  }

  public void update(DeviatedNoteCommand command) {
    commandInvoker.invoke(command);
    for(DeviatedNoteListener l : listeners)
      l.noteUpdated(command.getDeviatedNote());
  }

}
