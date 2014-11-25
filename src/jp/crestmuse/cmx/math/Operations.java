package jp.crestmuse.cmx.math;
import org.apache.commons.math.stat.descriptive.rank.*;
import org.apache.commons.math.linear.*;
import org.apache.commons.math.util.*;
import static java.lang.Math.*;
import java.util.*;
import static jp.crestmuse.cmx.math.MathUtils.*;

public class Operations {

  private static final DoubleArrayFactory factory = 
    DoubleArrayFactory.getFactory();
  private static final BooleanArrayFactory bfactory = 
    BooleanArrayFactory.getFactory();
  private static final ComplexArrayFactory cfactory = 
    ComplexArrayFactory.getFactory();
    private static final DoubleMatrixFactory mfactory = 
	DoubleMatrixFactory.getFactory();

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

  public static void set(DoubleArray x, int from, int thru, double value) {
    for (int i = from; i < thru; i++)
      x.set(i, value);
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

    public static DoubleArray sub(DoubleArray x, double y) {
	int length = x.length();
	DoubleArray z = factory.createArray(length);
	for (int i = 0; i < length; i++)
	    z.set(i, x.get(i) - y);
	return z;
    }

    public static DoubleArray minus(DoubleArray x, double y) {
	return sub(x, y);
    }

    public static void subX(DoubleArray x, DoubleArray y) {
	int length = x.length();
	for (int i = 0; i < length; i++)
	    x.set(i, x.get(i) - y.get(i));
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

    public static void mulX(DoubleArray x, DoubleArray y) {
	int length = x.length();
	for (int i = 0; i < length; i++)
	    x.set(i, x.get(i) * y.get(i));
    }

  public static void mulX(DoubleArray x, double y) {
    int length = x.length();
    for (int i = 0; i < length; i++)
      x.set(i, x.get(i) * y);
  }

    public static void mulX(DoubleArray x, int i, double y ) {
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
    
    public static void divX(DoubleArray x, DoubleArray y) {
	int length = x.length();
	for (int i = 0; i < length; i++)
	    x.set(i, x.get(i) / y.get(i));
    }
		
  public static void divX(DoubleArray x, double y) {
    int length = x.length();
    for (int i = 0; i < length; i++)
      x.set(i, x.get(i) / y);
  }

  public static void divX(DoubleArray x, int i, double y) {
    x.set(i, x.get(i) / y);
  }

  public static double sum(DoubleArray x, int from, int thru) {
    double sum = 0.0;
    for (int i = from; i < thru; i++)
      sum += x.get(i);
    return sum;
  }

  public static double mean(DoubleArray x) {
    return sum(x, 0, x.length()) / x.length();
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

    public static double absmax(DoubleArray x) {
	double max = Double.NEGATIVE_INFINITY;
	int length = x.length();
	double value;
	for (int i = 0; i < length; i++)
	    if ((value = Math.abs(x.get(i))) > max)
		max = value;
	return max;
    }

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

    public static BooleanArray and(BooleanArray x, BooleanArray y) {
	int length = x.length();
	BooleanArray z = bfactory.createArray(length);
	for (int i = 0; i < length; i++)
	    z.set(i, x.get(i) && y.get(i));
	return z;
    }

  public static DoubleArray remove(DoubleArray x, int index) {
    int length = x.length();
    DoubleArray z = factory.createArray(length - 1);
    for (int i = 0; i < index ; i++) {
      z.set(i, x.get(i));
    }
    for (int i = index; i < length - 1; i++) {
      z.set(i, x.get(i+1));
    }
    return z;
  }

  public static DoubleArray removeX(DoubleArray x, int index) {
    int length = x.length();
    for (int i = index; i < length - 1; i++) {
      x.set(i, x.get(i+1));
    }
    return x.subarrayX(0, length-1);
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

    public static ComplexNumber getAt(ComplexArray x, int index) {
	return x.get(index);
    }

    public static void putAt(ComplexArray x, int index, 
				      ComplexNumber value) {
	x.set(index, value);
    }

    public static void putAt(ComplexArray x, int index, 
				      double[] value) {
	x.set(index, value[0], value[1]);
    }

    public static DoubleArray getReal(ComplexArray x) {
	int length = x.length();
	DoubleArray z = factory.createArray(length);
	for (int i = 0; i < length; i++) 
	    z.set(i, x.getReal(i));
	return z;
    }

    public static DoubleArray getImag(ComplexArray x) {
	int length = x.length();
	DoubleArray z = factory.createArray(length);
	for (int i = 0; i < length; i++)
	    z.set(i, x.getImag(i));
	return z;
    }

    public static ComplexArray add(ComplexArray x, ComplexArray y) {
	int length = x.length();
	ComplexArray z = cfactory.createArray(length);
	for (int i = 0; i < length; i++) {
	    z.setReal(i, x.getReal(i) + y.getReal(i));
	    z.setImag(i, x.getImag(i) + y.getImag(i));
	}
	return z;
    }

    public static ComplexArray plus(ComplexArray x, ComplexArray y) {
	return add(x, y);
    }

    public static ComplexArray sub(ComplexArray x, ComplexArray y) {
	int length = x.length();
	ComplexArray z = cfactory.createArray(length);
	for (int i = 0; i < length; i++) {
	    z.setReal(i, x.getReal(i) - y.getReal(i));
	    z.setImag(i, x.getImag(i) - y.getImag(i));
	}
	return z;
    }

    public static ComplexArray minus(ComplexArray x, ComplexArray y) {
	return sub(x, y);
    }

    public static ComplexArray mul(ComplexArray x, ComplexArray y) {
	int length = x.length();
	ComplexArray z = cfactory.createArray(length);
	for (int i = 0; i < length; i++) {
	    z.setReal(i, x.getReal(i)*y.getReal(i)-x.getImag(i)*y.getImag(i));
	    z.setImag(i, x.getImag(i)*y.getReal(i)+x.getReal(i)*y.getImag(i));
	}
	return z;
    }

    public static ComplexArray multiply(ComplexArray x, ComplexArray y) {
	return mul(x, y);
    }

    public static ComplexArray div(ComplexArray x, ComplexArray y) {
	int length = x.length();
	ComplexArray z = cfactory.createArray(length);
	for (int i = 0; i < length; i++) {
	    double a = x.getReal(i);
	    double b = x.getImag(i);
	    double c = y.getReal(i);
	    double d = y.getImag(i);
	    z.setReal(i, (a*c + b*d) / (c*c + d*d));
	    z.setImag(i, (b*c - a*d) / (c*c + d*d));
	}
	return z;
    }


    public static double getAt(DoubleMatrix x, int[] indices) {
	return x.get(indices[0], indices[1]);
    }

    public static void putAt(DoubleMatrix x, int[] indices, double value) {
	x.set(indices[0], indices[1], value);
    }

    public static DoubleMatrix add(DoubleMatrix x, DoubleMatrix y) {
	int nrows = x.nrows();
	int ncols = x.ncols();
	DoubleMatrix z = mfactory.createMatrix(nrows, ncols);
	for (int i = 0; i < nrows; i++)
	    for (int j = 0; j < ncols; j++)
		z.set(i, j, x.get(i, j) + y.get(i, j));
	return z;
    }

    public static DoubleMatrix plus(DoubleMatrix x, DoubleMatrix y) {
	return add(x, y);
    }

    public static DoubleMatrix add(DoubleMatrix x, DoubleArray y) {
	int nrows = x.nrows();
	int ncols = x.ncols();
	DoubleMatrix z = mfactory.createMatrix(nrows, ncols);
	for (int i = 0; i < nrows; i++)
	    for (int j = 0; j < ncols; j++)
		z.set(i, j, x.get(i, j) + y.get(j));
	return z;
    }

    public static DoubleMatrix plus(DoubleMatrix x, DoubleArray y) {
	return add(x, y);
    }

    public static DoubleMatrix sub(DoubleMatrix x, DoubleMatrix y) {
	int nrows = x.nrows();
	int ncols = x.ncols();
	DoubleMatrix z = mfactory.createMatrix(nrows, ncols);
	for (int i = 0; i < nrows; i++)
	    for (int j = 0; j < ncols; j++)
		z.set(i, j, x.get(i, j) - y.get(i, j));
	return z;
    }

    public static DoubleMatrix minus(DoubleMatrix x, DoubleMatrix y) {
	return sub(x, y);
    }

    public static DoubleMatrix sub(DoubleMatrix x, DoubleArray y) {
	int nrows = x.nrows();
	int ncols = x.ncols();
	DoubleMatrix z = mfactory.createMatrix(nrows, ncols);
	for (int i = 0; i < nrows; i++)
	    for (int j = 0; j < ncols; j++)
		z.set(i, j, x.get(i, j) - y.get(j));
	return z;
    }

    public static DoubleMatrix minus(DoubleMatrix x, DoubleArray y) {
	return sub(x, y);
    }
    
    public static DoubleMatrix mul(DoubleMatrix x, double y) {
	int nrows = x.nrows();
	int ncols = x.ncols();
	DoubleMatrix z = mfactory.createMatrix(nrows, ncols);
	for (int i = 0; i < nrows; i++)
	    for (int j = 0; j < ncols; j++)
		z.set(i, j, x.get(i, j) * y);
	return z;
    }
    
    public static DoubleMatrix multiply(DoubleMatrix x, double y) {
	return mul(x, y);
    }

    public static DoubleMatrix mul(DoubleMatrix x, DoubleMatrix y) {
	int l = x.nrows();
	int m = x.ncols();
	int n = y.ncols();
	DoubleMatrix z = mfactory.createMatrix(l, n);
	for (int i = 0; i < l; i++) {
	    for (int j = 0; j < n; j++) {
		double value = 0;
		for (int k = 0; k < m; k++) 
		    value += x.get(i, k) * y.get(k, j);
		z.set(i, j, value);
	    }
	}
	return z;
    }

    public static DoubleMatrix multiply(DoubleMatrix x, DoubleMatrix y) {
	return mul(x, y);
    }
	

    public static DoubleMatrix div(DoubleMatrix x, DoubleArray y) {
	int nrows = x.nrows();
	int ncols = x.ncols();
	DoubleMatrix z = mfactory.createMatrix(nrows, ncols);
	for (int i = 0; i < nrows; i++)
	    for (int j = 0; j < ncols; j++)
		z.set(i, j, x.get(i, j) / y.get(j));
	return z;
    }

    public static DoubleMatrix divide(DoubleMatrix x, DoubleArray y) {
	return div(x, y);
    }

    public static DoubleMatrix div(DoubleMatrix x, double y) {
	int nrows = x.nrows();
	int ncols = x.ncols();
	DoubleMatrix z = mfactory.createMatrix(nrows, ncols);
	for (int i = 0; i < nrows; i++)
	    for (int j = 0; j < ncols; j++)
		z.set(i, j, x.get(i, j) / y);
	return z;
    }

    public static DoubleMatrix divide(DoubleMatrix x, double y) {
	return div(x, y);
    }

    public static DoubleMatrix transposeX(DoubleMatrix x) {
	return new TransposedDoubleMatrix(x);
    }

    private static class TransposedDoubleMatrix extends AbstractDoubleMatrixImpl {
	private DoubleMatrix x;
	private TransposedDoubleMatrix(DoubleMatrix x) {
	    this.x = x;
	}
	public int nrows() {
	    return x.ncols();
	}
	public int ncols() {
	    return x.ncols();
	}
	public double get(int i, int j) {
	    return x.get(j, i);
	}
	public void set(int i, int j, double value) {
	    x.set(j, i, value);
	}
    }

    public static DoubleArray getRow(DoubleMatrix x, int index) {
	int nrows = x.nrows();
	int ncols = x.ncols();
	DoubleArray z = factory.createArray(ncols);
	for (int j = 0; j < ncols; j++)
	    z.set(j, x.get(index, j));
	return z;
    }

    public static DoubleArray getColumn(DoubleMatrix x, int index) {
	int nrows = x.nrows();
	int ncols = x.ncols();
	DoubleArray z = factory.createArray(nrows);
	for (int i = 0; i < nrows; i++)
	    z.set(i, x.get(i, index));
	return z;
    }
    
    public static boolean containsNaNInf(DoubleMatrix x) {
	int nrows = x.nrows();
	int ncols = x.ncols();
	for (int i = 0; i < nrows; i++)
	    for (int j = 0; j < ncols; j++)
		if (Double.isNaN(x.get(i, j)) || Double.isInfinite(x.get(i, j)))
		    return true;
	return false;
    }
    
    public static boolean isNaNInf(DoubleMatrix x, int i, int j) {
	double value = x.get(i, j);
	return Double.isNaN(value) || Double.isInfinite(value);
    }
    
    public static boolean isNaN(DoubleMatrix x, int i, int j) {
	return Double.isNaN(x.get(i, j));
    }

    public static boolean isInf(DoubleMatrix x, int i, int j) {
	return Double.isInfinite(x.get(i, j));
    }

    public static DoubleArray sumrows(DoubleMatrix x) {
	int nrows = x.nrows();
	int ncols = x.ncols();
	DoubleArray z = factory.createArray(ncols);
	for (int j = 0; j < ncols; j++) {
	    double value = 0;
	    for (int i = 0; i < nrows; i++)
		value += x.get(i, j);
	    z.set(j, value);
	}
	return z;
    }

    public static DoubleArray sumcols(DoubleMatrix x) {
	int ncols = x.ncols();
	int nrows = x.nrows();
	DoubleArray z = factory.createArray(nrows);
	for (int i = 0; i < nrows; i++) {
	    double value = 0;
	    for (int j = 0; j < ncols; j++)
		value += x.get(i, j);
	    z.set(i, value);
	}
	return z;
    }

    public static DoubleArray meanrows(DoubleMatrix x) {
	int nrows = x.nrows();
	int ncols = x.ncols();
	DoubleArray z = factory.createArray(ncols);
	for (int j = 0; j < ncols; j++) {
	    double value = 0;
	    for (int i = 0; i < nrows; i++)
		value += x.get(i, j);
	    z.set(j, value / nrows);
	}
	return z;
    }

    public static DoubleArray meancols(DoubleMatrix x) {
	int ncols = x.ncols();
	int nrows = x.nrows();
	DoubleArray z = factory.createArray(nrows);
	for (int i = 0; i < nrows; i++) {
	    double value = 0;
	    for (int j = 0; j < ncols; j++)
		value += x.get(i, j);
	    z.set(i, value / ncols);
	}
	return z;
    }

    public static DoubleArray stdrows(DoubleMatrix x) {
	x = sub(x, meanrows(x));
	int nrows = x.nrows();
	int ncols = x.ncols();
	DoubleArray z = factory.createArray(ncols);
	double value;
	double N = (double)(nrows - 1);
	for (int j = 0; j < ncols; j++) {
	    double S = 0;
	    for (int i = 0; i < nrows; i++) 
		S += (value = x.get(i, j)) * value;
	    z.set(j, Math.sqrt(S / N));
	}
	return z;
    }

    public static DoubleArray stdcols(DoubleMatrix x) {
	x = sub(x, meancols(x));
	int nrows = x.nrows();
	int ncols = x.ncols();
	DoubleArray z = factory.createArray(nrows);
	double value;
	double N = (double)(ncols - 1);
	for (int i = 0; i < nrows; i++) {
	    double S = 0;
	    for (int j = 0; j < ncols; j++)
		S += (value = x.get(i, j)) * value;
	    z.set(i, Math.sqrt(S / N));
	}
	return z;
    }

    public static double max(DoubleMatrix x) {
	int nrows = x.nrows();
	int ncols = x.ncols();
	double max = Double.NEGATIVE_INFINITY;
	double value;
	for (int i = 0; i < nrows ; i++)
	    for (int j = 0; j < ncols; j++)
		if ((value = x.get(i, j)) > max)
		    max = value;
	return max;
    }

    public static double min(DoubleMatrix x) {
	int nrows = x.nrows();
	int ncols = x.ncols();
	double min = Double.POSITIVE_INFINITY;
	double value;
	for (int i = 0; i < nrows; i++)
	    for (int j = 0; j < ncols; j++)
		if ((value = x.get(i, j)) < min)
		    min = value;
	return min;
    }

    public static DoubleMatrix cov(DoubleMatrix x) {
	DoubleMatrix xx = sub(x, meanrows(x));
	return mul(transposeX(xx), xx);
    }


    public static DoubleMatrix[] svd(DoubleMatrix x) {
	SingularValueDecomposition 
	   svd = new SingularValueDecompositionImpl(toRealMatrix(x));
	DoubleMatrix[] result = new DoubleMatrix[3];
	result[0] = toDoubleMatrix(svd.getU());
	result[1] = toDoubleMatrix(svd.getS());
	result[2] = toDoubleMatrix(svd.getV());
	return result;
    }

    public static DoubleMatrix[] eig(DoubleMatrix x) {
      EigenDecomposition 
        eig = new EigenDecompositionImpl
        (toRealMatrix(x), 
         org.apache.commons.math.util.MathUtils.SAFE_MIN);
	DoubleMatrix[] result = new DoubleMatrix[2];
	result[0] = toDoubleMatrix(eig.getV());
	result[1] = toDoubleMatrix(eig.getD());
	return result;
    }
    
    public static DoubleMatrix[] lu(DoubleMatrix x) {
	LUDecomposition lu = new LUDecompositionImpl(toRealMatrix(x));
	DoubleMatrix[] result = new DoubleMatrix[3];
	result[0] = toDoubleMatrix(lu.getL());
	result[1] = toDoubleMatrix(lu.getU());
	result[2] = toDoubleMatrix(lu.getP());
	return result;
    }

    public static double det(DoubleMatrix x) {
	LUDecomposition lu = new LUDecompositionImpl(toRealMatrix(x));
	return lu.getDeterminant();
    }

    public static DoubleMatrix inv(DoubleMatrix x) {
	LUDecomposition lu = new LUDecompositionImpl(toRealMatrix(x));
	return toDoubleMatrix(lu.getSolver().getInverse());
    }
	
    public static DoubleMatrix[] qr(DoubleMatrix x) {
	QRDecomposition qr = new QRDecompositionImpl(toRealMatrix(x));
	DoubleMatrix[] result = new DoubleMatrix[2];
	result[0] = toDoubleMatrix(qr.getQ());
	result[1] = toDoubleMatrix(qr.getR());
	return result;
    }

    public static boolean isSquare(DoubleMatrix x) {
	return x.nrows() == x.ncols();
    }

    public static DoubleArray diag(DoubleMatrix x) {
	if (!isSquare(x)) 
	    throw new MathException("Matrix should be square");
	int length = x.nrows();
	DoubleArray z = factory.createArray(length);
	for (int i = 0; i < length; i++)
	    z.set(i, x.get(i, i));
	return z;
    }
    
    public static DoubleMatrix normalize(DoubleMatrix x) {
	DoubleArray mean = meanrows(x);
	DoubleArray std = stdrows(x);
	for (int i = 0; i < std.length(); i++)
	    if (std.get(i) == 0) std.set(i, 1.0);
	return div(sub(x, mean), std);
    }

    public static double innerproduct(DoubleArray x, DoubleArray y) {
	double value = 0;
	int length = x.length();
	for (int i = 0; i < length; i++)
	    value += x.get(i) * y.get(i);
	return value;
    }

    public static double norm(DoubleArray x) {
	return Math.sqrt(innerproduct(x, x));
    }

    public static DoubleMatrix toMatrixV(DoubleArray array) {
	return new MyMatrixV(array);
    }

    private static class MyMatrixV extends AbstractDoubleMatrixImpl {
	private DoubleArray array;
	private MyMatrixV(DoubleArray array) {
	    this.array = array;
	}
	public int nrows() {
	    return array.length();
	}
	public int ncols() {
	    return 1;
	}
	public double get(int i, int j) {
	    if (j == 0)
		return array.get(i);
	    else
		throw new IllegalStateException();
	}
	public void set(int i, int j, double value) {
	    if (j == 0)
	        array.set(i, value);
	    else
		throw new IllegalStateException();
	}
    }

    public static DoubleMatrix toMatrixH(DoubleArray array) {
	return new MyMatrixH(array);
    }

    private static class MyMatrixH extends AbstractDoubleMatrixImpl {
	private DoubleArray array;
	private MyMatrixH(DoubleArray array) {
	    this.array = array;
	}
	public int nrows() {
	    return 1;
	}
	public int ncols() {
	    return array.length();
	}
	public double get(int i, int j) {
	    if (i == 0)
		return array.get(j);
	    else
		throw new IllegalStateException();
	}
	public void set(int i, int j, double value) {
	    if (i == 0)
		array.set(j, value);
	    else
		throw new IllegalStateException();
	}
    }

  public static DoubleArray conv(DoubleArray x, DoubleArray y) {
    int xlength = x.length();
    int ylength = y.length();
    double[] z = new double[xlength];
    if (x instanceof DefaultDoubleArray && y instanceof DefaultDoubleArray) {
      double[] xx = x.toArray();
      double[] yy = y.toArray();
      for (int t = 0; t < xlength; t++)
	for (int k = 0; k < ylength; k++)
	  if (t - k >= 0)
	    z[t] += xx[t-k] * yy[k];
    } else {
      for (int t = 0; t < xlength; t++)
	for (int k = 0; k < ylength; k++)
	  if (t - k >= 0)
	    z[t] += x.get(t-k) * y.get(k);
    }
    return factory.createArray(z);
  }
  

  public static DoubleArray abs(DoubleArray x) {
    int length = x.length();
    double[] z = new double[length];
    for (int i = 0; i < length; i++) 
      z[i] = Math.abs(x.get(i));
    return factory.createArray(z);
  }

  /** Important: Previous version had a bug in this method. */
  public static DoubleArray abs(ComplexArray x) {
    int length = x.length();
    double[] z = new double[length];
    for (int i = 0; i < length; i++) {
      double a = x.getReal(i);
      double b = x.getImag(i);
      z[i] = Math.sqrt(a * a + b * b);
    }
    return factory.createArray(z);
  }

  public static DoubleArray dB(DoubleArray x, double base) {
    int length = x.length();
    double[] z = new double[length];
    for (int i = 0; i < length; i++) 
      z[i] = 10 * (Math.log(x.get(i)) - Math.log(base)) / Math.log(10);
    return factory.createArray(z);
  }
      
  public static DoubleArray sigmoid(DoubleArray x, double gain) {
    int length = x.length();
    double[] z = new double[length];
    for (int i = 0; i < length; i++) 
      z[i] = 1 / (1 + Math.exp(-gain * x.get(i)));
    return factory.createArray(z);
  }

  public static DoubleArray autocorr(DoubleArray x) {
    int length = x.length();
    double[] z = new double[length/2];
    for (int t = 0; t < length / 2; t++) 
      for (int tau = 0; tau < length / 2; tau++)  
        z[tau] += x.get(t) * x.get(t + tau);
    return factory.createArray(z);
  }
}


