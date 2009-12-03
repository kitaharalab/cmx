package jp.crestmuse.cmx.amusaj.sp;
import jp.crestmuse.cmx.amusaj.filewrappers.*;

public class HarmonicsExtractor2 extends HarmonicsExtractor {

  public double f0;

  public HarmonicsExtractor2(double f0) {
    super();
    this.f0 = f0;
  }

  public void execute(SPElement[] src, 
                      TimeSeriesCompatible<SPElement>[] dest)
    throws InterruptedException {
    PeakSet peakset = (PeakSet)src[0];
    dest[0].add(extractHarmonics(peakset, f0));
  }

}