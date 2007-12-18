package jp.crestmuse.cmx.math;

public class Operations {

  private static final DoubleArrayFactory factory = 
    DoubleArrayFactory.getFactory();

  private Operations() { }


  public static DoubleArray subarray(DoubleArray x, int from, int thru) {
    int length = thru - from;
    DoubleArray z = factory.createArray(length);
    for (int i = 0; i < length; i++)
      z.set(i, x.get(from + i));
    return z;
  }

  public static DoubleArray add(DoubleArray x, DoubleArray y) {
    int length = x.length();
    DoubleArray z = factory.createArray(length);
    for (int i = 0; i < length; i++)
      z.set(i, x.get(i) + y.get(i));
    return z;
  }

  public static void divX(DoubleArray x, double y) {
    int length = x.length();
    for (int i = 0; i < length; i++)
      x.set(i, x.get(i) / y);
  }

            
    

}
