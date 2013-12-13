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
  

}