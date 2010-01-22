package jp.crestmuse.cmx.math;
import jp.crestmuse.cmx.misc.*;

class DefaultComplexArray implements ComplexArray {
  private double[] re, im;
  private int from, thru;

  DefaultComplexArray(int length) {
    re = new double[length];
    im = new double[length];
    from = 0;
    thru = length;
  }
  
  DefaultComplexArray(double[] re, double[] im) {
    this.re = re;
    this.im = im;
    from = 0;
    thru = re.length;
    if (thru != im.length)
      throw new MathException("inconsistent data size");
  }

  public int length() {
    return thru - from;
  }
    
  public double getReal(int index) {
    return re[from + index];
  }

  public double getImag(int index) {
    if (im == null)
      return 0.0;
    else
      return im[from + index];
  }

    public ComplexNumber get(int index) {
	return new ComplexNumber(getReal(index), getImag(index));
    }

  public void setReal(int index, double value) {
    re[from + index] = value;
  }

  public void setImag(int index, double value) {
    if (im == null)
      im = new double[thru - from];
    im[from + index] = value;
  }

  public void set(int index, double re, double im) {
    setReal(index, re);
    setImag(index, im);
  }

    public void set(int index, ComplexNumber value) {
	set(index, value.real, value.imag);
    }

  public ComplexArray subarrayX(int from, int thru) {
    DefaultComplexArray newarray = new DefaultComplexArray(re, im);
    newarray.from = this.from + from;
    newarray.thru = this.from + thru;
    return newarray;
  }

}