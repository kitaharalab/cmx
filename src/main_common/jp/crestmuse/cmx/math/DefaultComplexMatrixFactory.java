package jp.crestmuse.cmx.math;

public class DefaultComplexMatrixFactory extends ComplexMatrixFactory {
  public ComplexMatrix createMatrix(int nrows, int ncols) {
    return new DefaultComplexMatrix(nrows, ncols);
  }
}