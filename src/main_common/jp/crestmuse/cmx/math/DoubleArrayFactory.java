package jp.crestmuse.cmx.math;

public abstract class DoubleArrayFactory extends MathFactory {
  private static DoubleArrayFactory factory = null;

  public static synchronized DoubleArrayFactory getFactory() {
    if (factory == null) {
      factory = (DoubleArrayFactory)getFactory("doubleArrayFactory", 
                                               "DefaultDoubleArrayFactory");
    }
    return factory;
  }
        
/*
  public static synchronized final DoubleArrayFactory getFactory() {
    if (factory == null) {
      try {
        String className = System.getProperty("doubleArrayFactory");
        if (className == null)
          className = "jp.crestmuse.cmx.math.DefaultDoubleArrayFactory";
        factory = (DoubleArrayFactory)Class.forName(className).newInstance();
      } catch (ClassNotFoundException e) {
        throw new DoubleArrayFactoryException(e.toString());
      } catch (InstantiationException e) {
        throw new DoubleArrayFactoryException(e.toString());
      } catch (IllegalAccessException e) {
        throw new DoubleArrayFactoryException(e.toString());
        }
    }
    return factory;
  }
*/

  public abstract DoubleArray createArray(int length);
  public abstract DoubleArray createArray(double[] array);
  public abstract DoubleArray createArray(int length, double value);
//  public abstract DoubleArray createFinalArray(double[] array);
//  public abstract DoubleArray createFinalArray(double[] array, 
//                                               int from, int thru);
}
