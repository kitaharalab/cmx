package jp.crestmuse.cmx.math;

public class DimensionMismatchException extends IllegalArgumentException {
  public DimensionMismatchException(int wrong, int excepted) {
    super("Dimension mismatch: " + wrong + "\t(excepted: " + excepted + ")");
  }
}
