package jp.crestmuse.cmx.math;

class DefaultDoubleArray extends AbstractDoubleArrayImpl {
  private double[] values;
  private int from, thru;

  DefaultDoubleArray(int length) {
    values = new double[length];
    from = 0;
    thru = length;
  }

  DefaultDoubleArray(double[] values) {
    this.values = values;
    from = 0;
    thru = values.length;
  }

  DefaultDoubleArray(int length, double value) {
    values = new double[length];
    for (int i = 0; i < length; i++)
      values[i] = value;
    from = 0;
    thru = values.length;
  }

  public int length() {
    return thru - from;
  }

  public double get(int index) {
    return values[index + from];
  }

  public void set(int index, double value) {
    values[index + from] = value;
  }

  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }

  public double[] toArray() {
    if (from == 0 && thru == values.length) {
      return values;
    } else {
      double[] newarray = new double[thru - from];
      System.arraycopy(values, from, newarray, 0, thru-from);
      return newarray;
    }
  }

//  public String getEncodeFormatName() {
//    return "array";
//  }

/*
  public DoubleArray subarray(int from, int thru) {
    double[] newarray = new double[thru - from];
    System.arraycopy(values, from, newarray, 0, thru - from);
    return new DefaultDoubleArray(newarray);
  }
*/

  public DoubleArray subarrayX(int from, int thru) {
    DefaultDoubleArray newarray = new DefaultDoubleArray(values);
    newarray.from = this.from + from;
    newarray.thru = this.from + thru;
    return newarray;
  }
    
}
