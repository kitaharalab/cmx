package jp.crestmuse.cmx.misc;

public class InvalidChordSymbolException extends IllegalArgumentException {
  public InvalidChordSymbolException() {
    super();
  }

  public InvalidChordSymbolException(String s) {
    super(s);
  }
}
