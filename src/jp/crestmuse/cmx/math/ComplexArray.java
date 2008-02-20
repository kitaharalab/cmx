package jp.crestmuse.cmx.math;

public interface ComplexArray {
  int length();
  double getReal(int index);
  double getImag(int index);
  void setReal(int index, double value);
  void setImag(int index, double value);
  void set(int index, double re, double im);
  ComplexArray subarrayX(int from, int thru);
}