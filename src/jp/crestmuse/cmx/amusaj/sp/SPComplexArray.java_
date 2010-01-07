package jp.crestmuse.cmx.amusaj.sp;
import jp.crestmuse.cmx.math.*;

public class SPComplexArray implements ComplexArray,SPElementEncodable {
  private ComplexArray array;
    //  private boolean hasNext;

  public SPComplexArray(ComplexArray array) {
    this.array = array;
    //    this.hasNext = hasNext;
  }

  public int length() {
    return array.length();
  }

  public double getReal(int index) {
    return array.getReal(index);
  }

  public double getImag(int index) {
    return array.getImag(index);
  }

  public void set(int index, double re, double im ) {
    array.set(index, re, im);
  }

  public void setReal(int index, double value) {
    array.setReal(index, value);
  }

  public void setImag(int index, double value) {
    array.setImag(index, value);
  }

  public ComplexArray subarrayX(int from, int thru) {
    return array.subarrayX(from, thru);
  }

  public String encode() {
    return jp.crestmuse.cmx.math.Utils.toString2(array);
  }

    //  public boolean hasNext() {
    //    return hasNext;
    //  }

}
