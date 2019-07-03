package jp.crestmuse.cmx.math;

public abstract class AbstractComplexMatrixImpl implements ComplexMatrix,Cloneable {

  public ComplexNumber get(int i, int j) {
    return new ComplexNumber(getReal(i, j), getImag(i, j));
  }

  public void set(int i, int j, double re, double im) {
    setReal(i, j, re);
    setImag(i, j, im);
  }

  public void set(int i, int j, ComplexNumber value) {
    set(i, j, value.real, value.imag);
  }

  public ComplexMatrix clone() throws CloneNotSupportedException {
    ComplexMatrix newmatrix = MathUtils.createComplexMatrix(nrows(), ncols());
    for (int i = 0; i < nrows(); i++)
      for (int j = 0; j < ncols(); j++)
        newmatrix.set(i, j, getReal(i, j), getImag(i, j));
    return newmatrix;
  }

  public String toString() {
    return MathUtils.toString1(this);
  }

  public String encode() {
    return MathUtils.toString2(this);
  }
}