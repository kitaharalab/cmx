package jp.crestmuse.cmx.math;

class DoubleMatrixImplAsArray extends AbstractDoubleMatrixImpl {
  private double[] values;
  private int nrows, ncols;

  DoubleMatrixImplAsArray(int nrows, int ncols, double[] values) {
    this.values = values;
    this.nrows = nrows;
    this.ncols = ncols;
  }

  public int nrows() {
    return nrows;
  }

  public int ncols() {
    return ncols;
  }

  public double get(int i, int j) {
    return values[i + j * nrows];
  }

  public void set(int i, int j, double value) {
    values[i + j * nrows] = value;
  }
}
                                    
