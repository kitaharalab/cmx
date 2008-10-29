package jp.crestmuse.cmx.math;
import jp.crestmuse.cmx.misc.*;

public interface DoubleArray extends Array,Encodable {
  double get(int index);
  void set(int index, double value);
  Object clone() throws CloneNotSupportedException;
  double[] toArray();
  DoubleArray subarrayX(int from, int thru);
}
