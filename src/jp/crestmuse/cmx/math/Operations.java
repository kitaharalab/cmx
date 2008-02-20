package jp.crestmuse.cmx.math;

public class Operations {

  private static final DoubleArrayFactory factory = 
    DoubleArrayFactory.getFactory();
  private static final BooleanArrayFactory bfactory = 
    BooleanArrayFactory.getFactory();
  private static final ComplexArrayFactory cfactory = 
    ComplexArrayFactory.getFactory();

  private Operations() { }

  public static DoubleArray subarray(DoubleArray x, int from, int thru) {
    int length = thru - from;
    DoubleArray z = factory.createArray(length);
    for (int i = 0; i < length; i++)
      z.set(i, x.get(from + i));
    return z;
  }

  public static ComplexArray subarray(ComplexArray x, int from, int thru) {
    int length = thru - from;
    ComplexArray z = cfactory.createArray(length);
    for (int i = 0; i < length; i++)
      z.set(i, x.getReal(from + i), x.getImag(from + i));
    return z;
  }

  public static DoubleArray add(DoubleArray x, DoubleArray y) {
    int length = x.length();
    DoubleArray z = factory.createArray(length);
    for (int i = 0; i < length; i++)
      z.set(i, x.get(i) + y.get(i));
    return z;
  }

  public static DoubleArray add(DoubleArray x, double y) {
    int length = x.length();
    DoubleArray z = factory.createArray(length);
    for (int i = 0; i < length; i++)
      z.set(i, x.get(i) + y);
    return z;
  }

  public static void divX(DoubleArray x, double y) {
    int length = x.length();
    for (int i = 0; i < length; i++)
      x.set(i, x.get(i) / y);
  }

  public static double sum(DoubleArray x) {
    double sum = 0.0;
    int length = x.length();
    for (int i = 0; i < length; i++)
      sum += x.get(i);
    return sum;
  }

  public static double min(DoubleArray x) {
    double min = Double.POSITIVE_INFINITY;
    int length = x.length();
    double value;
    for (int i = 0; i < length; i++)
      if ((value = x.get(i)) < min)
        min = value;
    return min;
  }

  public static boolean containsNaNInf(DoubleArray x) {
    int length = x.length();
    for (int i = 0; i < length; i++)
      if (Double.isNaN(x.get(i)) || Double.isInfinite(x.get(i)))
        return true;
    return false;
  }

  public static DoubleArray makeArithmeticSeries(double a, double d, int n) {
    DoubleArray z = factory.createArray(n);
    for (int i = 0; i < n; i++)
      z.set(i, a + (double)i * d);
    return z;
  }

  public static DoubleArray makeArithmeticSeries(double a, int n) {
    return makeArithmeticSeries(a, 1.0, n);
  }

  public static DoubleArray makeArithmeticSeries(int n) {
    return makeArithmeticSeries(1.0, 1.0, n);
  }

  public static DoubleArray nn2cent(DoubleArray x) {
    int length = x.length();
    DoubleArray z = factory.createArray(length);
    for (int i = 0; i < length; i++)
      z.set(i, x.get(i) * 100 - 1200);
    return z;
  }

  public static void nn2centX(DoubleArray x) {
    int length = x.length();
    for (int i = 0; i < length; i++)
      x.set(i, x.get(i) * 100 - 1200);
  }

  public static void Hz2centX(DoubleArray x) {
    int length = x.length();
    for (int i = 0; i < length; i++)
      x.set(i, 1200 * log2(x.get(i) / 440 / 0.037162722343835));
  }
    
  public static DoubleArray Hz2cent(DoubleArray x) {
    int length = x.length();
    DoubleArray z = factory.createArray(length);
    for (int i = 0; i < length; i++)
      z.set(i, 1200 * log2(x.get(i) / 440 / 0.037162722343835));
    return z;
  }

  private static double log2(double x) {
    return Math.log(x) / Math.log(2.0);
  }

  public static BooleanArray lessThan(DoubleArray x, double y) {
    int length = x.length();
    BooleanArray b = bfactory.createArray(length);
    for (int i = 0; i < length; i++) 
      b.set(i, x.get(i) < y);
    return b;
  }

  public static BooleanArray or(BooleanArray x, BooleanArray y) {
    int length = x.length();
    BooleanArray z = bfactory.createArray(length);
    for (int i = 0; i < length; i++)
      z.set(i, x.get(i) || y.get(i));
    return z;
  }

  public static DoubleArray removeMask(DoubleArray x, BooleanArray mask) {
//    if (x.length() != mask.length())
//      throw new AmusaDimensionException();
    int length = x.length();
    int nFalses = 0;
    for (int i = 0; i < length; i++)
      if (!mask.get(i)) nFalses++;
    DoubleArray z = factory.createArray(nFalses);
    int k = 0;
    for (int i = 0; i < length; i++) {
      if (!mask.get(i)) {
        z.set(k, x.get(i));
        k++;
      }
    }
    return z;
  }


}
