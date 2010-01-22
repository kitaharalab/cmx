package jp.crestmuse.cmx.math;
import jp.crestmuse.cmx.misc.*;

public interface DoubleMatrix extends Encodable{
  int nrows();
  int ncols();
  double get(int i, int j);
  void set(int i, int j, double value);
    Object clone() throws CloneNotSupportedException;
    String encode();
}
