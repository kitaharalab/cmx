package jp.crestmuse.cmx.math;

public abstract class ComplexArrayFactory extends MathFactory {
  private static ComplexArrayFactory factory = null;

  public static synchronized ComplexArrayFactory getFactory() {
    if (factory == null)
      factory = (ComplexArrayFactory)getFactory("complexArrayFactory", 
                                                "DefaultComplexArrayFactory");
    return factory;
  }

  public abstract ComplexArray createArray(int length);
  public abstract ComplexArray createArray(double[] re, double[] im);
//  public abstract ComplexArray createArray(double[] array);
//  public abstract ComplexArray createArray2(double[] array);
}