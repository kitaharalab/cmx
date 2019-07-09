package jp.crestmuse.cmx.math;
import jp.crestmuse.cmx.misc.Encodable;

public interface ComplexMatrix extends Matrix, Encodable {
  double getReal(int i, int j);
  double getImag(int i, int j);
  ComplexNumber get(int i, int j);
  void setReal(int i, int j, double value);
  void setImag(int i, int j, double value);
  void set(int i, int j, double re, double im);
  void set(int i, int j, ComplexNumber value);
  ComplexMatrix clone() throws CloneNotSupportedException;
  String encode();
}