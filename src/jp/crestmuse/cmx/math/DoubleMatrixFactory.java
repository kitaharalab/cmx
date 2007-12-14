package jp.crestmuse.cmx.math;

public abstract class DoubleMatrixFactory {
  public static final DoubleMatrixFactory getFactory() 
    throws ClassNotFoundException, InstantiationException,
    IllegalAccessException {
    String className = System.getProperty("doubleMatrixFactory");
    if (className == null)
      className = "jp.crestmuse.cmx.math.DefaultDoubleMatrixFactory";
    return (DoubleMatrixFactory)Class.forName(className).newInstance();
  }

  public abstract DoubleMatrix createMatrix(int nrows, int ncols);
  public abstract DoubleMatrix createMatrix(double[][] values);
  public abstract DoubleMatrix createSparseMatrix(int nrows, int ncols);
}
