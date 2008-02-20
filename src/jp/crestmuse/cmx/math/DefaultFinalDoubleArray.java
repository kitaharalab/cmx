package jp.crestmuse.cmx.math;

/*
class DefaultFinalDoubleArray implements DoubleArray,Cloneable {
  private final double[] values;
  private int from, thru;

  DefaultFinalDoubleArray(double[] values) {
    this.values = values;
    from = 0;
    thru = values.length;
  }

  DefaultFinalDoubleArray(double[] values, int from, int thru) {
    this.values = values;
    this.from = from;
    this.thru = thru;
  }

  public int length() {
    return thru - from;
  }

  public double get(int index) {
    return values[index + from];
  }

  public void set(int index, double value) {
    throw new UnsupportedOperationException("This DoubleArray is final");
  }

  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }

  public double[] toArray() {
    if (from == 0 && thru == values.length) {
      return values;
    } else {
      double[] newarray = new double[thru - from];
      System.arraycopy(values, from, newarray, 0, thru - from);
      return newarray;
    }
  }

  public DoubleArray subarray(int from, int thru) {
    return new DefaultFinalDoubleArray(values, this.from + from, 
                                       this.from + thru);
  }
}
*/