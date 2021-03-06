package jp.crestmuse.cmx.amusaj.sp;

import jp.crestmuse.cmx.math.DoubleArrayFactory;

public class SPUtils {
  
  SPUtils(){}

  private static DoubleArrayFactory factory = DoubleArrayFactory.getFactory();


  public static final double[] rectangle(int size) {
    double[] x = new double[size];

    for (int i = 0; i < size; i++)
      x[i] = 1.0;

    return x;
  }

  public static final double[] hamming(int size) {
    double[] x = new double[size];

    for (int i = 0; i < size; i++)
      x[i] = 0.54 - 0.46 * Math.cos(2.0 * Math.PI * i / size);

    return x;
  }

  public static final double[] hanning(int size) {
    double[] x = new double[size];

    for (int i = 0; i < size; i++)
      x[i] = 0.5 - 0.5 * Math.cos(2.0 * Math.PI * i / size);

    return x;
  }

  public static final double[] gaussian(int size, double stddev) {
    double[] x = new double[size];
    double center = size * 0.5;
    double sd2 = 1.0 / stddev;

    for (int i = 0; i < size; i++) {
      double d = (i - center) * sd2;
      x[i] = Math.exp(-0.5 * d * d);
    }

    return x;
  }

  public static final double[] gaussian(int size) {
    return gaussian(size, size / 6.0);
  }

/*
    public static final SPDoubleArray createSPDoubleArray(int length) {
	return new SPDoubleArray(factory.createArray(length));
    }

    public static final SPDoubleArray createSPDoubleArray(double[] x) {
	return new SPDoubleArray(factory.createArray(x));
    }

    public static final SPDoubleArray createSPDoubleArray(List<BigDecimal> x) {
	return new SPDoubleArray(createDoubleArray(x));
    }

    public static final SPDoubleArray create1dimSPDoubleArray(double x) {
	DoubleArray array = factory.createArray(1);
	array.set(0, x);
	return new SPDoubleArray(array);
    }
*/
}