package jp.crestmuse.cmx.math;

/*
public class DefaultComplexArray2 implements ComplexArray {
  private double[] values;
  private int from, thru;

  DefaultComplexArray2(double[] values) {
    this.values = values;
    from = 0;
    thru = values.length / 2;
  }

  public int length() {
    return thru - from;
  }

  public double getReal(int index) {
    return values[(from + index) * 2];
  }

  public double getImag(int index) {
    return values[(from + index) * 2 + 1];
  }

  public void setReal(int index, double value) {
    values[(from + index) * 2] = value;
  }

  public void setImag(int index, double value) {
    values[(from + index) * 2 + 1] = value;
  }

  public void set(int index, double re, double im) {
    values[(from + index) * 2] = re;
    values[(from + index) * 2 + 1] = im;
  }

  public ComplexArray subarrayX(int from, int thru) {
    DefaultComplexArray2 newarray = new DefaultComplexArray2(values);
    newarray.from = this.from + from;
    newarray.thru = this.from + thru;
    return newarray;
  }
}
*/