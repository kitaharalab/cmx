package jp.crestmuse.cmx.math;

public class DefaultDoubleMatrixFactory extends DoubleMatrixFactory {
  public DoubleMatrix createMatrix(int nrows, int ncols) {
    return new DefaultDoubleMatrix(nrows, ncols);
  }

  public DoubleMatrix createMatrix(double[][] values) {
    return new DefaultDoubleMatrix(values);
  }

  public DoubleMatrix createSparseMatrix(int nrows, int ncols) {
    return new DefaultSparseDoubleMatrix(nrows, ncols);
  }
}
