package jp.crestmuse.cmx.math;

public interface DoubleArray {
  int length();
  double get(int index);
  void set(int index, double value);
  double[] toArray();
  DoubleArray subarrayX(int from, int thru);
}
