package jp.crestmuse.cmx.math;
import java.util.*;

class DefaultSparseDoubleMatrix implements DoubleMatrix {
  private HashMap<IntPair,Double> values;
  private int nrows, ncols;

  DefaultSparseDoubleMatrix(int nrows, int ncols) {
    this.nrows = nrows;
    this.ncols = ncols;
    values = new HashMap<IntPair,Double>();
  }

  public int nrows() {
    return nrows;
  }

  public int ncols() {
    return ncols;
  }

  public double get(int i, int j) {
    if (i >= nrows || j >= ncols || i < 0 || j < 0)
      throw new ArrayIndexOutOfBoundsException();
    IntPair pair = new IntPair(i, j);
    if (values.containsKey(pair))
      return values.get(pair);
    else
      return 0.0;
  }

  public void set(int i, int j, double value) {
    if (i >= nrows || j >= ncols || i < 0 || j < 0)
      throw new ArrayIndexOutOfBoundsException();
    values.put(new IntPair(i, j), value);
  }

  private class IntPair {
    private int i, j;
    private IntPair(int i, int j) {
      this.i = i;
      this.j = j;
    }
    public boolean equals(Object o) {
      IntPair another = (IntPair)o;
      return (i == another.i) && (j == another.j);
    }
    public int hashCode() {
      return i * nrows + j;
    }
  }
}