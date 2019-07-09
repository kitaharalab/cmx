package jp.crestmuse.cmx.amusaj.sp;
import jp.crestmuse.cmx.math.DoubleArray;

public class DoubleArrayWithTicktime implements DoubleArray {
  private DoubleArray array;
  public long music_position;

  public DoubleArrayWithTicktime(DoubleArray array, long position) {
    this.array = array;
    this.music_position = position;
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
    return array.encode();
  }

}
