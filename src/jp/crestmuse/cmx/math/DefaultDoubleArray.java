package jp.crestmuse.cmx.math;

class DefaultDoubleArray implements DoubleArray {
  private double[] values;

  DefaultDoubleArray(int length) {
    values = new double[length];
  }

  DefaultDoubleArray(double[] values) {
    this.values = values;
  }

  public int length() {
    return values.length;
  }

  public double get(int index) {
    return values[index];
  }

  public void set(int index, double value) {
    values[index] = value;
  }
}
