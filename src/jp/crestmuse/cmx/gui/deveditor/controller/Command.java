package jp.crestmuse.cmx.gui.deveditor.controller;

public interface Command {

  public void invoke();
  public void undo();
  public void redo();

}
