package jp.crestmuse.cmx.amusaj.sp;
import jp.crestmuse.cmx.math.*;
import jp.crestmuse.cmx.sound.*;

// This is bad!!
// We should reconsider how to design this.
public class SamplingRateChanger extends SPModule {
  private int oldRate, newRate, nch;
  private Class[] clazz;
  public SamplingRateChanger(int oldRate, int newRate, int nch) {
    this.oldRate = oldRate;
    this.newRate = newRate;
    this.nch = nch;
    clazz = new Class[nch];
    for (int i = 0; i < nch; i++)
      clazz[i] = DoubleArray.class;
  }

  public Class[] getInputClasses() {
    return clazz;
  }

  public Class[] getOutputClasses() {
    return clazz;
  }

  public void execute(Object[] src, TimeSeriesCompatible[] dest) 
    throws InterruptedException {
    for (int i = 0; i < nch; i++)
      dest[i].add(SignalProc.changeRate((DoubleArray)src[i], oldRate, newRate));
  }
}
