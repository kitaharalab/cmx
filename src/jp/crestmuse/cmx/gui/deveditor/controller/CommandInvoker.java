package jp.crestmuse.cmx.gui.deveditor.controller;

import java.util.Stack;

public class CommandInvoker {

  private Stack<Command> undoStack = new Stack<Command>();
  private Stack<Command> redoStack = new Stack<Command>();

  public void invoke(Command command) {
    command.invoke();
    redoStack.clear();
    undoStack.push(command);
  }

  public void undo() {
    if(undoStack.isEmpty())
      return;
    Command command = undoStack.pop();
    command.undo();
    redoStack.push(command);
  }

  public void redo() {
    if(redoStack.isEmpty())
      return;
    Command command = redoStack.pop();
    command.redo();
    undoStack.push(command);
  }

}
