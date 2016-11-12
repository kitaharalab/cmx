package jp.crestmuse.cmx.math;

class DefaultDoubleArrayWithNegativeIndex extends DefaultDoubleArray {

  DefaultDoubleArrayWithNegativeIndex(int length) {
    super(length);
  }

  DefaultDoubleArrayWithNegativeIndex(double[] values) {
    super(values);
  }

  DefaultDoubleArrayWithNegativeIndex(int length, double value) {
    super(length, value);
  }

  public double get(int index) {
    if (index < 0)
      return super.get(length() + index);
    else
      return super.get(index);
  }

  public void set(int index, double value) {
    if (index < 0)
      super.set(length() + index, value);
    else 
      super.set(index, value);
  }
}