package jp.crestmuse.cmx.amusaj.sp;
import jp.crestmuse.cmx.amusaj.filewrappers.*;
import jp.crestmuse.cmx.math.*;
import jp.crestmuse.cmx.misc.*;

import java.util.*;

public class FeatureExtractionModule2 extends SPModule {

  private FeatureExtractor<HarmonicsTimeSeries> fe;

  public FeatureExtractionModule2(FeatureExtractor<HarmonicsTimeSeries> fe) {
    this.fe = fe;
  }

  public void execute(SPElement[] src, TimeSeriesCompatible<SPElement>[] dest)
    throws InterruptedException {
    HarmonicsTimeSeries h = (HarmonicsTimeSeries)src[0];
//    System.err.println(h);
    int n = dest.length;
    fe.extractFeatures(h);
    for (int i = 0; i < n; i++) {
      TimeSeriesCompatible ts = dest[i];
      ts.add(new SPDoubleArray(fe.getFeature(i)));
      ts.setAttribute("type", fe.getFeatureType(i));
    }
//    fe.nextFrame();
  }

  public Class<SPElement>[] getInputClasses() {
    return new Class[]{ HarmonicsTimeSeries.class };
  }

  public Class<SPElement>[] getOutputClasses() {
    Class[] ret = new Class[fe.nFeatureTypes()];
    Arrays.fill(ret, SPDoubleArray.class);
    return ret;
  }

}

