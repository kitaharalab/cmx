package jp.crestmuse.cmx.math;
import jp.crestmuse.cmx.sound.SignalProc;

@Deprecated /** @deprecated */
public class SP {

  public SP() {}

  @Deprecated
  public static DoubleArray changeRate(DoubleArray x, 
                                       int orgrate, int newrate) {
    return SignalProc.changeRate(x, orgrate, newrate);
  }       

  @Deprecated
  public static DoubleArray cutLastSmallSignal(DoubleArray x, double thrs) {
    return SignalProc.cutLastSmallSignal(x, thrs);
  }

  @Deprecated
  public static int detectOnset(DoubleArray x, int n, double thrs) {
    return SignalProc.detectOnset(x, n, thrs);
  }


}