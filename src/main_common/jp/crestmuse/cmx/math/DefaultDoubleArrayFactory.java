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

//  public DoubleArray createFinalArray(double[] values) {
//    return new DefaultFinalDoubleArray(values);
//  }

//  public DoubleArray createFinalArray(double[] values, int from, int thru) {
//    return new DefaultFinalDoubleArray(values, from, thru);
//  }
}