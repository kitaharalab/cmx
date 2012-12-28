package jp.crestmuse.cmx.math;

class DefaultComplexMatrix extends AbstractComplexMatrixImpl {
  
  private double[][] re, im;
  private int nrows, ncols;

  DefaultComplexMatrix(int nrows, int ncols) {
    re = new double[nrows][ncols];
    im = new double[nrows][ncols];
    this.nrows = nrows;
    this.ncols = ncols;
  }

  public int nrows() {
    return nrows;
  }

  public int ncols() {
    return ncols;
  }

  public double getReal(int i, int j) {
    return re[i][j];
  }

  public double getImag(int i, int j) {
    return im[i][j];
  }

  public void setReal(int i, int j, double value) {
    re[i][j] = value;
  }

  public void setImag(int i, int j, double value) {
    im[i][j] = value;
  }
}