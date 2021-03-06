package jp.crestmuse.cmx.amusaj.sp;
import jp.crestmuse.cmx.amusaj.filewrappers.PeakSet;

public class HarmonicsExtractor2 extends HarmonicsExtractor {

  public double f0;

  public HarmonicsExtractor2(double f0) {
    super();
    this.f0 = f0;
  }

  public void execute(Object[] src, TimeSeriesCompatible[] dest)
    throws InterruptedException {
    PeakSet peakset = (PeakSet)src[0];
    dest[0].add(extractHarmonics(peakset, f0));
  }

  public Class[] getInputClasses() {
    return new Class[] { PeakSet.class };
  }

}