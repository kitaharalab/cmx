package jp.crestmuse.cmx.math;

public abstract class DoubleMatrixFactory extends MathFactory {
  private static DoubleMatrixFactory factory = null;
  public static synchronized DoubleMatrixFactory getFactory() {
    if (factory == null) {
      factory = (DoubleMatrixFactory)getFactory("doubleMatrixFactory", 
                                               "DefaultDoubleMatrixFactory");
    }
    return factory;
  }

  public abstract DoubleMatrix createMatrix(int nrows, int ncols);
  public abstract DoubleMatrix createMatrix(double[][] values);
  public abstract DoubleMatrix createSparseMatrix(int nrows, int ncols);
}
