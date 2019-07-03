package jp.crestmuse.cmx.math;

public class MathException extends IllegalStateException {
  public MathException() {
    super();
  }
  public MathException(String s) {
    super(s);
  }
  public MathException(Exception e) {
    super(e.toString());
  }
}
