package jp.crestmuse.cmx.amusaj.sp;

import jp.crestmuse.cmx.math.*;
import static jp.crestmuse.cmx.math.Operations.*;

public class Utils {
  
  private Utils(){}

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


}