package jp.crestmuse.cmx.misc;

public class ProgramBugException extends IllegalStateException {
  public ProgramBugException() {
    super();
  }

  public ProgramBugException(String s) {
    super(s);
  }
}