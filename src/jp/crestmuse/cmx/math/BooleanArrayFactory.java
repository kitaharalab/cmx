package jp.crestmuse.cmx.math;

public abstract class BooleanArrayFactory extends MathFactory {
  private static BooleanArrayFactory factory = null;

  public static synchronized BooleanArrayFactory getFactory() {
    if (factory == null)
      factory = (BooleanArrayFactory)getFactory("booleanArrayFactory", 
                                                "DefaultBooleanArrayFactory");
    return factory;
  }

  public abstract BooleanArray createArray(int length);
  public abstract BooleanArray createArray(boolean[] array);
}
