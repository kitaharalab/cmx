package jp.crestmuse.cmx.filewrappers;

public class InvalidElementException extends IllegalArgumentException {
  public InvalidElementException() {
    super();
  }

  public InvalidElementException(String s) {
    super(s);
  }
}