package jp.crestmuse.cmx.amusaj.sp;
import jp.crestmuse.cmx.math.ComplexArray;
import jp.crestmuse.cmx.math.ComplexNumber;

public class ComplexArrayWithTicktime implements ComplexArray {
  private ComplexArray array;
  public long music_position;

  public ComplexArrayWithTicktime(ComplexArray array, long position) {
    this.array = array;
    this.music_position = position;
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

  public ComplexNumber get(int index) {
    return array.get(index);
  }

  public void setReal(int index, double value) {
    array.setReal(index, value);
  }

  public void setImag(int index, double value) {
    array.setImag(index, value);
  }
  
  public void set(int index, double re, double im) {
    array.set(index, re, im);
  }

  public void set(int index, ComplexNumber value) {
    array.set(index, value);
  }

//  public Object clone() throws CloneNotSupportedException {
//    return array.clone();
//  }

//  public double[] toArray() {
//    return array.toArray();
//  }

  public ComplexArray subarrayX(int from, int thru) {
    return array.subarrayX(from, thru);
  }

//  public String encode() {
//    return array.encode();
//  }

}
