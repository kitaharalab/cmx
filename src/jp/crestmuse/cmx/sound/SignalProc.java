package jp.crestmuse.cmx.sound;
import jp.crestmuse.cmx.math.*;
import static jp.crestmuse.cmx.math.Utils.*;
import static jp.crestmuse.cmx.math.Operations.*;

public class SignalProc {

  private SignalProc() {}

  // kari
  public static DoubleArray changeRate(DoubleArray x, 
                                       int orgrate, int newrate) {
    if (orgrate == newrate) {
      return x;
    } else if (newrate > orgrate && newrate % orgrate == 0) {
      return changeRateUp(x, newrate / orgrate);
    } else if (orgrate > newrate && orgrate % newrate == 0) {
      return changeRateDown(x, orgrate / newrate);
    } else {
      int midrate = orgrate * newrate / gcd(orgrate, newrate);
      DoubleArray y = changeRateUp(x, midrate / orgrate);
      return changeRateDown(y, midrate / newrate);
    }
  }       

  private static DoubleArray changeRateUp(DoubleArray x, int r) {
    DoubleArray z = createDoubleArray(r * x.length());
    for (int i = 0; i < x.length(); i++)
      for (int j = 0; j < r; j++)
        z.set(i * r + j, x.get(i));
    return z;
  }

  private static DoubleArray changeRateDown(DoubleArray x, int r) {
    DoubleArray z = createDoubleArray(x.length() / r);
    for (int i = 0; i < x.length() / r; i++)
      z.set(i, x.get(i * r));
    System.err.println(z.length());
    return z;
  }

  private static int gcd(int m, int n) {
    while (n != 0) {
      int t = m % n;
      m = n;
      n = t;
    }
    return Math.abs(m);
  }


  public static DoubleArray cutLastSmallSignal(DoubleArray x, double thrs) {
    thrs = thrs * max(x);
    int index;
    for (index = x.length() - 1; index >= 0; index--)
      if (Math.abs(x.get(index)) >= thrs)
        break;
    return x.subarrayX(0, index);
  }

  public static int detectOnset(DoubleArray x, int n, double thrs) {
    int cmp = 0;
    int lastcmp = 0;
    int onset = 0;
    int repeat = 0;
    for (int i = 0; i < x.length(); i++) {
      lastcmp = cmp;
      double x1 = x.get(i);
      if (x1 > thrs) cmp = 1;
      else if (x1 < -thrs) cmp = -1;
      else cmp = 0;
      if (cmp * lastcmp == -1) repeat++;
      if (repeat >= n) return i;
    }
    return -1;
  }

  

  public static DoubleArray dwt_loop(DoubleArray s0, 
                                     DoubleArray g, DoubleArray h, int n) {
    int thru = s0.length();
//    DoubleArray s1 = createDoubleArray(thru);
    DoubleArray s1 = cloneArray(s0);
    for (int i = 0; i < n; i++) {
      dwt(s0, s1, g, h, 0, thru);
      s0 = cloneArray(s1);
      thru = thru / 2;
    }
    return s1;
  }

  public static DoubleArray dwt(DoubleArray s0, 
                                DoubleArray g, DoubleArray h) {
    DoubleArray s1 = createDoubleArray(s0.length());
    dwt(s0, s1, g, h, 0, s0.length());
    return s1;
  }
                                

  static void dwt(DoubleArray s0, DoubleArray s1, 
                         DoubleArray g, DoubleArray h, 
                         int from, int thru) {
    int length = thru - from;
//    int length = s0.length();
    int filter_length = g.length();
//    DoubleArray s1 = createDoubleArray(length);
    set(s1, from, thru, 0.0);
    for (int n = 0; n < length/2; n++) {
      for (int k = 0; k < filter_length; k++) {
        int i = (2 * n + k) % length;
        addX(s1, from+n, g.get(k) * s0.get(from+i));
//        s1.set(n, s1.get(n) + g.get(k) * s0.get(i));
        addX(s1, from+n+length/2, h.get(k) * s0.get(from+i));
//        s1.set(n+length/2, s1.get(n+length/2) + h.get(k) + s0.get(i));
      }
    }
//    for (int n = 0; n < length/2; n++)
//      divX(s1, n, 2.0);
////    divX(s1, 2.0);
  }

  public static DoubleArray idwt_loop(DoubleArray s1, 
                                      DoubleArray p, DoubleArray q, int n) {
    int thru = s1.length();
//    DoubleArray s0 = createDoubleArray(thru);
    DoubleArray s0 = cloneArray(s1);
    for (int i = 0; i < n; i++) {
      thru = thru / 2;
    }
    for (int i = 0; i < n; i++) {
      thru = thru * 2;
      idwt(s1, s0, p, q, 0, thru);
      s1 = cloneArray(s0);
    }
    return s0;
  }


  public static DoubleArray idwt(DoubleArray s1, 
                                 DoubleArray p, DoubleArray q) {
    DoubleArray s0 = createDoubleArray(s1.length());
    idwt(s1, s0, p, q, 0, s1.length());
    return s0;
  }

  static void idwt(DoubleArray s1, DoubleArray s0, 
                   DoubleArray p, DoubleArray q,
                   int from, int thru) {
    int length = thru - from;
//    int length = s1.length();
    int filter_length = p.length();
//    DoubleArray s0 = createDoubleArray(length);
    DoubleArray s0_LPF = createDoubleArray(length);
    DoubleArray s0_HPF = createDoubleArray(length);
    set(s0, from, thru, 0.0);
//    for (int n = 0; n < length / 2; n++)
//      mulX(s1, n, 2.0);
    for (int n = 0; n < length / 2; n++) {
      s0_LPF.set(2 * n, s1.get(from + n));
      s0_HPF.set(2 * n, s1.get(from + n + length / 2));
    }
    for (int n = 0; n < length; n++) {
      for (int k = 0; k < filter_length; k++) {
        int i = (n - k + length) % length;
        addX(s0, from+n, p.get(k) * s0_LPF.get(i) + q.get(k) * s0_HPF.get(i));
      }
    }
  }
                 
  public static ComplexArray fft(DoubleArray x, double[] win) {
    return fft.executeR2C(x, win);
  }


}