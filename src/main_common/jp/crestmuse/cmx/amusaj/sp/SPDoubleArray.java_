package jp.crestmuse.cmx.amusaj.sp;
import jp.crestmuse.cmx.math.*;

public class SPDoubleArray implements DoubleArray,SPElementEncodable {
  private DoubleArray array;
    //  private boolean hasNext;

  public SPDoubleArray(DoubleArray array) {
    this.array = array;
    //    this.hasNext = hasNext;
  }

  public int length() {
    return array.length();
  }

  public double get(int index) {
    return array.get(index);
  }

  public void set(int index, double value) {
    array.set(index, value);
  }

  public Object clone() throws CloneNotSupportedException {
    return array.clone();
  }

  public double[] toArray() {
    return array.toArray();
  }

  public DoubleArray subarrayX(int from, int thru) {
    return array.subarrayX(from, thru);
  }

  public String encode() {
    return jp.crestmuse.cmx.math.Utils.toString2(array);
  }

    //  public boolean hasNext() {
    //    return hasNext;
    //  }

}
