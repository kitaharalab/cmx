package jp.crestmuse.cmx.math;

public class DefaultDoubleArrayFactory extends DoubleArrayFactory {
  public DoubleArray createArray(int length) {
    return new DefaultDoubleArray(length);
  }

  public DoubleArray createArray(double[] values) {
    return new DefaultDoubleArray(values);
  }

  public DoubleArray createArray(int length, double value) {
    return new DefaultDoubleArray(length, value);
  }
}