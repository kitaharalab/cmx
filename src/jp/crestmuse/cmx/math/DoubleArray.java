package jp.crestmuse.cmx.math;

public interface DoubleArray {
  int length();
  double get(int index);
  void set(int index, double value);
  Object clone() throws CloneNotSupportedException;
  double[] toArray();
  DoubleArray subarrayX(int from, int thru);
}
