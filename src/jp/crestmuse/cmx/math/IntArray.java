package jp.crestmuse.cmx.math;
import jp.crestmuse.cmx.misc.*;

public interface IntArray extends Array,Encodable {
  int get(int index);
  void set(int index, int value);
//  Object clone() throws CloneNotSupportedException;
  int[] toArray();
//  DoubleArray subarrayX(int from, int thru);
  String encode();
}
