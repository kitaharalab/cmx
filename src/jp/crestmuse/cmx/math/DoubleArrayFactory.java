package jp.crestmuse.cmx.math;
import java.lang.reflect.*;

public abstract class DoubleArrayFactory {

  public static final DoubleArrayFactory getFactory() 
    throws ClassNotFoundException, InstantiationException,
    IllegalAccessException {
    String className = System.getProperty("doubleArrayFactory");
    if (className == null)
      className = "jp.crestmuse.cmx.math.DefaultDoubleArrayFactory";
    return (DoubleArrayFactory)Class.forName(className).newInstance();
  }

  public abstract DoubleArray createArray(int length);
  public abstract DoubleArray createArray(double[] array);
}
