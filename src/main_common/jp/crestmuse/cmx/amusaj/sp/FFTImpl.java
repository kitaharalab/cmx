package jp.crestmuse.cmx.amusaj.sp;
import org.apache.commons.math.complex.Complex;
import org.apache.commons.math.transform.FastFourierTransformer;

import jp.crestmuse.cmx.math.ComplexArray;
import jp.crestmuse.cmx.math.ComplexNumber;
import jp.crestmuse.cmx.math.DoubleArray;

public class FFTImpl implements FFT {
  private double[] buff = null;
//  private ComplexArrayFactory factory = ComplexArrayFactory.getFactory();
  private FastFourierTransformer fft = new FastFourierTransformer();

  public ComplexArray executeR2C(DoubleArray in, double[] window) {
    int length = in.length();
    if (buff == null || buff.length != length)
      buff = new double[length];
    if (window == null)
      for (int i = 0; i < length; i++)
        buff[i] = in.get(i);
    else
      for (int i = 0; i < length; i++)
	buff[i] = in.get(i) * window[i];
    //    try {
      Complex[] result = fft.transform(buff);
      return (new MyComplexArray(result)).subarrayX(0, length/2+1);
//      ComplexArray array = factory.createArray(result.length/2+1);
//      for (int i = 0; i < result.length/2+1; i++)
//        array.set(i, result[i].getReal(), result[i].getImaginary());
//      return array;
//    } catch (org.apache.commons.math.MathException e) {
//      throw new MathException(e);
//    }
  }

  private class MyComplexArray implements ComplexArray {
    private Complex[] values;
    private int from, thru;
    MyComplexArray(Complex[] values) {
      this.values = values;
      from = 0;
      thru = values.length;
    }
    public int length() {
      return thru - from;
    }
    public double getReal(int index) {
      return values[from + index].getReal();
    }
    public double getImag(int index) {
      return values[from + index].getImaginary();
    }
      public ComplexNumber get(int index) {
	  return new ComplexNumber(getReal(index), getImag(index));
      }
    public void setReal(int index, double value) {
      throw new UnsupportedOperationException();
    }
    public void setImag(int index, double value) {
      throw new UnsupportedOperationException();
    }
    public void set(int index, double re, double im) {
      throw new UnsupportedOperationException();
    }
      public void set(int index, ComplexNumber value) {
	  throw new UnsupportedOperationException();
      }
    public ComplexArray subarrayX(int from, int thru) {
      MyComplexArray newarray = new MyComplexArray(values);
      newarray.from = this.from + from;
      newarray.thru = this.from + thru;
      return newarray;
    }
  }
}