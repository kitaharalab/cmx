package jp.crestmuse.cmx.math;
import jp.crestmuse.cmx.misc.Encodable;

public interface DoubleMatrix extends Matrix, Encodable{
//  int nrows();
//  int ncols();
  double get(int i, int j);
  void set(int i, int j, double value);
    Object clone() throws CloneNotSupportedException;
    String encode();
}
