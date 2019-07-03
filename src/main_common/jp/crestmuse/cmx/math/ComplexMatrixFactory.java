package jp.crestmuse.cmx.math;

public abstract class ComplexMatrixFactory extends MathFactory {
  private static ComplexMatrixFactory factory = null;
  public static synchronized ComplexMatrixFactory getFactory() {
    if (factory == null) {
      factory = (ComplexMatrixFactory)getFactory("complexMatrixFactory", 
                                                 "DefaultComplexMatrixFactory");
    }
    return factory;
  }

  public abstract ComplexMatrix createMatrix(int nrows, int ncols);
}
