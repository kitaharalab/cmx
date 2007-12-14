package jp.crestmuse.cmx.math;

class DefaultDoubleMatrix implements DoubleMatrix {
  private double[][] values;
  private int nrows, ncols;

  DefaultDoubleMatrix(int nrows, int ncols) {
    values = new double[nrows][ncols];
    this.nrows = nrows;
    this.ncols = ncols;
  }

  DefaultDoubleMatrix(double[][] values) {
    this.values = values;
  }

  public int nrows() {
    return nrows;
  }

  public int ncols() {
    return ncols;
  }

  public double get(int i, int j) {
    return values[i][j];
  }

  public void set(int i, int j, double value) {
    values[i][j] = value;
  }
}
