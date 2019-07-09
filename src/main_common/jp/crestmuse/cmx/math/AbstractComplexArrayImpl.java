package jp.crestmuse.cmx.math;
import jp.crestmuse.cmx.misc.Encodable;

public abstract class AbstractComplexArrayImpl implements ComplexArray,Encodable {

  public void set(int index, double re, double im) {
    setReal(index, re);
    setImag(index, im);
  }

  public void set(int index, ComplexNumber value) {
    set(index, value.real, value.imag);
  }

  public ComplexNumber get(int index) {
    return new ComplexNumber(getReal(index), getImag(index));
  }
  
  public ComplexArray clone() throws CloneNotSupportedException {
    ComplexArray newarray = Utils.createComplexArray(length());
    for (int i = 0; i < length(); i++)
      newarray.set(i, getReal(i), getImag(i));
    return newarray;
  }

  public String toString() {
    return MathUtils.toString1(this);
  }

  public String encode() {
    return MathUtils.toString2(this);
  }

  public ComplexArray subarrayX(int from, int thru) {
    try {
      ComplexArray newarray = clone();
      return newarray.subarrayX(from, thru);
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException();
    }
  }
}
