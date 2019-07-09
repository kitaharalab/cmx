package jp.crestmuse.cmx.amusaj.sp;
import java.util.Arrays;

import jp.crestmuse.cmx.math.DoubleArray;

public class FeatureExtractionModule2 extends SPModule {

  private FeatureExtractor<HarmonicsTimeSeries> fe;

  public FeatureExtractionModule2(FeatureExtractor<HarmonicsTimeSeries> fe) {
    this.fe = fe;
  }

  public void execute(Object[] src, TimeSeriesCompatible[] dest)
    throws InterruptedException {
    HarmonicsTimeSeries h = (HarmonicsTimeSeries)src[0];
//    System.err.println(h);
    int n = dest.length;
    fe.extractFeatures(h);
    for (int i = 0; i < n; i++) {
      TimeSeriesCompatible ts = dest[i];
      ts.add(fe.getFeature(i));
      ts.setAttribute("type", fe.getFeatureType(i));
    }
//    fe.nextFrame();
  }

  public Class[] getInputClasses() {
    return new Class[]{ HarmonicsTimeSeries.class };
  }

  public Class[] getOutputClasses() {
    Class[] ret = new Class[fe.nFeatureTypes()];
    Arrays.fill(ret, DoubleArray.class);
    return ret;
  }

}

