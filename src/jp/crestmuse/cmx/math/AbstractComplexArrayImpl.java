package jp.crestmuse.cmx.math;

public abstract class AbstractComplexArrayImpl implements ComplexArray{

  public ComplexNumber get(int index) {
    return new ComplexNumber(getReal(index), getImag(index));
  }

  public void set(int index, double re, double im) {
    setReal(index, re);
    setImag(index, im);
  }

  public void set(int index, ComplexNumber value) {
    setReal(index, value.real);
    setImag(index, value.imag);
  }

}