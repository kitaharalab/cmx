package jp.crestmuse.cmx.math;

public interface DoubleMatrix {
  int nrows();
  int ncols();
  double get(int i, int j);
  void set(int i, int j, double value);
}
