package jp.crestmuse.cmx.math;
import org.apache.commons.math.stat.descriptive.rank.*;
import static java.lang.Math.*;
import java.util.*;

public class Operations {

  private static final DoubleArrayFactory factory = 
    DoubleArrayFactory.getFactory();
  private static final BooleanArrayFactory bfactory = 
    BooleanArrayFactory.getFactory();
  private static final ComplexArrayFactory cfactory = 
    ComplexArrayFactory.getFactory();

  private static final Median median = new Median();

  public Operations() { }

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

    public static void putAt(DoubleArray x, int i, double value) {
	x.set(i, value);
    }

    public static double getAt(DoubleArray x, int i) {
	return x.get(i);
    }

  public static DoubleArray add(DoubleArray x, DoubleArray y) {
    int length = x.length();
    DoubleArray z = factory.createArray(length);
    for (int i = 0; i < length; i++)
      z.set(i, x.get(i) + y.get(i));
    return z;
  }

    public static DoubleArray plus(DoubleArray x, DoubleArray y) {
	return add(x, y);
    }

  public static void addX(DoubleArray x, DoubleArray y) {
    int length = x.length();
    for (int i = 0; i < length; i++)
      x.set(i, x.get(i) + y.get(i));
  }

  public static DoubleArray add(DoubleArray x, double y) {
    int length = x.length();
    DoubleArray z = factory.createArray(length);
    for (int i = 0; i < length; i++)
      z.set(i, x.get(i) + y);
    return z;
  }

    public static DoubleArray plus(DoubleArray x, double y) {
	return add(x, y);
    }

  public static void addX(DoubleArray x, double y) {
    int length = x.length();
    for (int i = 0; i < length; i++)
      x.set(i, x.get(i) + y);
  }

  public static void addX(DoubleArray x, DoubleArray y, int from) {
    int length = y.length();
    for (int i = 0; i < length; i++)
      x.set(i + from, x.get(i + from) + y.get(i));
  }

  public static void addX(DoubleArray x, int i, double y) {
    x.set(i, x.get(i) + y);
  }

  public static DoubleArray sub(DoubleArray x, DoubleArray y) {
    int length = x.length();
    DoubleArray z = factory.createArray(length);
    for (int i = 0; i < length; i++)
      z.set(i, x.get(i) - y.get(i));
    return z;
  }

    public static DoubleArray minus(DoubleArray x, DoubleArray y) {
	return sub(x, y);
    }

  public static void subX(DoubleArray x, double y) {
    int length = x.length();
    for (int i = 0; i < length; i++)
      x.set(i, x.get(i) - y);
  }

    public static DoubleArray mul(DoubleArray x, double y) {
	int length = x.length();
	DoubleArray z = factory.createArray(length);
	for (int i = 0; i < length; i++)
	    z.set(i, x.get(i) * y);
	return z;
    }

    public static DoubleArray multiply(DoubleArray x, double y) {
	return mul(x, y);
    }

  public static DoubleArray mul(DoubleArray x, DoubleArray y) {
    int length = x.length();
    DoubleArray z = factory.createArray(length);
    for (int i = 0; i < length; i++)
      z.set(i, x.get(i) * y.get(i));
    return z;
  }

    public static DoubleArray multiply(DoubleArray x, DoubleArray y) {
	return multiply(x,y);
    }

  public static void mulX(DoubleArray x, double y) {
    int length = x.length();
    for (int i = 0; i < length; i++)
      x.set(i, x.get(i) * y);
  }

  public static DoubleArray div(DoubleArray x, double y) {
    int length = x.length();
    DoubleArray z = factory.createArray(length);
    for (int i = 0; i < length; i++)
      z.set(i, x.get(i) / y);
    return z;
  }

    public static DoubleArray divide(DoubleArray x, double y) {
	return div(x, y);
    }

    public static DoubleArray div(DoubleArray x, DoubleArray y) {
	int length = x.length();
	DoubleArray z = factory.createArray(length);
	for (int i = 0; i < length; i++)
	    z.set(i, x.get(i) / y.get(i));
	return z;
    }

    public static DoubleArray divide(DoubleArray x, DoubleArray y) {
	return div(x, y);
    }
    
  public static void divX(DoubleArray x, double y) {
    int length = x.length();
    for (int i = 0; i < length; i++)
      x.set(i, x.get(i) / y);
  }

  public static double sum(DoubleArray x, int from, int thru) {
    double sum = 0.0;
    for (int i = from; i < thru; i++)
      sum += x.get(i);
    return sum;
  }

  public static double sum(DoubleArray x) {
    return sum(x, 0, x.length());
//    double sum = 0.0;
//    int length = x.length();
//    for (int i = 0; i < length; i++)
//      sum += x.get(i);
//    return sum;
  }

  public static double sumodd(DoubleArray x) {
    double sum = 0.0;
    int length = x.length();
    for (int i = 1; i < length; i += 2)
      sum += x.get(i);
    return sum;
  }

  public static double sumeven(DoubleArray x) {
    double sum = 0.0;
    int length = x.length();
    for (int i = 0; i < length; i += 2)
      sum += x.get(i);
    return sum;
  }
  

  public static double min(DoubleArray x) {
    return min(x, new MinResult());
  }

    public static int argmin(DoubleArray x) {
	MinResult result = new MinResult();
	min(x, result);
	return result.argmin;
    }


  public static class MinResult {
    public double min;
    public int argmin;
  }

  public static double min(DoubleArray x, MinResult result) {
//    MinResult result = new MinResult();
    result.min = Double.POSITIVE_INFINITY;
    int length = x.length();
    double value;
    for (int i = 0; i < length; i++)
      if ((value = x.get(i)) < result.min) {
        result.min = value;
        result.argmin = i;
      }
    return result.min;
  }

/*
  public static double min(DoubleArray x) {
    double min = Double.POSITIVE_INFINITY;
    int length = x.length();
    double value;
    for (int i = 0; i < length; i++)
      if ((value = x.get(i)) < min)
        min = value;
    return min;
  }
*/

  public static class MaxResult {
    public double max;
    public int argmax;
    public double max2nd;
    public int argmax2nd;
    public double max3rd;
    public int argmax3rd;
  }

  public static double max(DoubleArray x) {
    return max(x, new MaxResult());
  }

    public static int argmax(DoubleArray x) {
	MaxResult result = new MaxResult();
	max(x, result);
	return result.argmax;
    }

  public static double max(DoubleArray x, MaxResult result) {
//    MaxResult result = new MaxResult();
    result.max = Double.NEGATIVE_INFINITY;
    result.max2nd = Double.NEGATIVE_INFINITY;
    result.max3rd = Double.NEGATIVE_INFINITY;
    int length = x.length();
    double value;
    for (int i = 0; i < length; i++)
      if ((value = x.get(i)) > result.max) {
        result.max3rd = result.max2nd;
        result.argmax3rd = result.argmax3rd;
        result.max2nd = result.max;
        result.argmax2nd = result.argmax;
        result.max = value;
        result.argmax = i;
      } else if (value > result.max2nd) {
        result.max3rd = result.max2nd;
        result.argmax3rd = result.argmax3rd;
        result.max2nd = value;
        result.argmax2nd = i;
      } else if (value > result.max3rd) {
        result.max3rd = value;
        result.argmax3rd = i;
      }
    return result.max;
  }


/*
  public static MaxResult max(DoubleArray x) {
    MaxResult result = new MaxResult();
    result.max = Double.NEGATIVE_INFINITY;
    int length = x.length();
    double value;
    for (int i = 0; i < length; i++)
      if ((value = x.get(i)) > result.max) {
        result.max = value;
        result.argmax = i;
      }
    return result;
  }
*/

  public static double median(DoubleArray x) {
    return median.evaluate(x.toArray());
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

  public static double nn2cent(double x) {
    return x * 100 - 1200;
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

  public static double Hz2cent(double x) {
    return 1200 * log2(x / 440 / 0.037162722343835);
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

  public static BooleanArray greaterThan(DoubleArray x, double y) {
    int length = x.length();
    BooleanArray b = bfactory.createArray(length);
    for (int i = 0; i < length; i++)
      b.set(i, x.get(i) > y);
    return b;
  }

  public static int nGreaterThan(DoubleArray x, double y) {
    int length = x.length();
    int n = 0;
    for (int i = 0; i < length; i++)
      if (x.get(i) > y)
        n++;
    return n;
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

  public static DoubleArray mask(DoubleArray x, BooleanArray mask, double y) {
    int length = x.length();
    DoubleArray z = factory.createArray(length);
    for (int i = 0; i < length; i++) {
      if (mask.get(i)) 
        z.set(i, y);
      else
        z.set(i, x.get(i));
    }
    return z;
  }
  
  public static final int Hz2nn(double x) {
    return 57 + (int)(12 * log(x / 220.0) / log(2));
  }
  
  public static final double nn2Hz(double nn) {
    return 220 * pow(2, (nn - 57.0) / 12.0);
  }


  public static void logX(DoubleArray x, int a) {
    int length = x.length();
    double logA = log(a);
    for (int i = 0; i < length; i++)
      x.set(i, log(x.get(i)) / logA);
  }

  public static double ratioTrue(BooleanArray x) {
    int nTrues = 0;
    int length = x.length();
    for (int i = 0; i < length; i++)
      if (x.get(i))
        nTrues++;
    return (double)nTrues / (double)length;
  }

  public static DoubleArray diff(DoubleArray x) {
    int length = x.length();
    DoubleArray z = factory.createArray(length - 1);
    for (int i = 0; i < length - 1; i++) 
      z.set(i, x.get(i+1) - x.get(i));
    return z;
  }


  public static DoubleArray sdiff(DoubleArray x, int srange) {
    int N = srange * (srange + 1) * (2 * srange + 1) / 3;
    DoubleArray z = factory.createArray(x.length());
    int shift;
    for (int t = 0; t < x.length(); t++) {
      z.set(t, 0.0);
      for (int i = 1; i <= srange; i++) {
        shift = Math.min(i, Math.min(
                  Math.abs(t - 0), 
                  Math.abs(x.length() - 1 - t)
                ));
        addX(z, t, (x.get(t + shift) - x.get(t - shift)) * (double)i);
      }
    }
    divX(z, N);
    return z;
  }


  public static int nZeroCross(DoubleArray x) {
    int n = 0; 
    for (int i = 0; i < x.length() - 1; i++)
      if (x.get(i) * x.get(i+1) < 0)
        n++;
    return n;
  }

  public static DoubleArray sort(DoubleArray x) {
    double[] array = (double[])x.toArray().clone();
    Arrays.sort(array);
    return factory.createArray(array);
  }

  public static double iqr(DoubleArray x) {
    DoubleArray xx = sort(x);
    int idxL = (int)(0.25 * (double)xx.length());
    int idxR = (int)(0.75 * (double)xx.length());
    return xx.get(idxR) - xx.get(idxL);
  }

  public static DoubleArray sgsmooth(DoubleArray x, int srange) {
    int N = (4 * srange * srange - 1) * (2 * srange + 3) / 3;
    int weight;
    int shift;
    DoubleArray z = factory.createArray(x.length());
    for (int t = 0; t < x.length(); t++) {
      weight = 3 * srange * (srange + 1) - 1;
      z.set(t, x.get(t) * (double)weight);
      for (int i = 1; i <= srange; i++) {
        weight = 3 * srange * (srange + 1) - 1 - 5 * i * i;
        shift = Math.min(i, Math.min(
                  Math.abs(t - 0), Math.abs(x.length() - 1 - t)
                ));
        addX(z, t, (x.get(t + shift) + x.get(t - shift)) * (double)weight);
      }
    }
    divX(z, N);
    return z;
  }

/*
  // KARI
  public static void sgsmoothX(DoubleArray x, int srange) {
    int N = (4 * srange * srange - 1) * (2 * srange + 3) / 3;
    int weight;
    int shift;
    DoubleArray clone = (DoubleArray)x.clone();
    for (int t = 0; t < x.length(); t++) {
      weight = 3 * srange * (srange + 1) - 1;
      x.set(t, clone.get(t) * (double)weight);
      for (int i = 1; i <= srange; i++) {
        weight = 3 * srange * (srange + 1) - 1 - 5 * i * i;
        shift = Math.min(i, Math.min(
                  Math.abs(t - 0), Math.abs(clone.length() - 1 - t)
                ));
        addX(x, t, (clone.get(t+shift) + clone.get(t-shift)) * (double)weight);
      }
    }
    divX(x, N);
  }
*/

  public static DoubleArray concat(DoubleArray[] arrays) {
    int length = 0;
    for (int i = 0; i < arrays.length; i++) 
      length += arrays[i].length();
    double[] z = new double[length];
    int idxNext = 0;
    for (int i = 0; i < arrays.length; i++) {
      int thislength = arrays[i].length();
      System.arraycopy(arrays[i].toArray(), 0, z, idxNext, thislength);
      idxNext += thislength;
    }
    return factory.createArray(z);
  }

  public static DoubleArray sum(DoubleArray[] arrays) {
    if (arrays.length == 1)
      return arrays[0];
    DoubleArray sum = add(arrays[0], arrays[1]);
    for (int i = 2; i < arrays.length; i++)
      addX(sum, arrays[i]);
    return sum;
  }

  public static DoubleArray sum(DoubleArray[] arrays, int from, int thru) {
    if (arrays.length == 1)
      return arrays[0].subarrayX(from, thru);
    DoubleArray sum = add(arrays[0].subarrayX(from, thru), 
                          arrays[1].subarrayX(from, thru));
    for (int i = 2; i < arrays.length; i++)
      addX(sum, arrays[i].subarrayX(from, thru));
    return sum;
  }

  public static DoubleArray mean(DoubleArray[] arrays) {
    DoubleArray sum = sum(arrays);
    divX(sum, arrays.length);
    return sum;
  }

  public static DoubleArray mean(DoubleArray[] arrays, int from, int thru) {
    DoubleArray sum = sum(arrays, from, thru);
    divX(sum, arrays.length);
    return sum;
  }



}


