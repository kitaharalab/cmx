package jp.crestmuse.cmx.amusaj.sp;
import jp.crestmuse.cmx.amusaj.filewrappers.*;
import jp.crestmuse.cmx.math.*;
import jp.crestmuse.cmx.misc.*;

import java.util.*;

public class FeatureExtractionModule extends SPModule {

  private FeatureExtractor<SPDoubleArray> fe;
  private String[] types;

  public FeatureExtractionModule(FeatureExtractor<SPDoubleArray> fe) {
    this.fe = fe;
    types = new String[fe.nFeatureTypes()];
    for (int i = 0; i < types.length; i++)
      types[i] = fe.getFeatureType(i);
  }

  public void execute(SPElement[] src, TimeSeriesCompatible<SPElement>[] dest)
    throws InterruptedException {
    SPDoubleArray wav = (SPDoubleArray)src[0];
    int n = dest.length;
    fe.extractFeatures(wav);
    for (int i = 0; i < n; i++) {
      TimeSeriesCompatible ts = dest[i];
      DoubleArray feats = fe.getFeature(i);
      if (feats != null) {
        ts.add(new SPDoubleArray(feats));
        ts.setAttribute("type", fe.getFeatureType(i));
      }
    }
//    fe.nextFrame();
  }

  public String[] getFeatureTypes() {
    return types;
  }

//  public FeatureExtractor<SPDoubleArray> getFeatureExtractor() {
//    return fe;
//  }

  public Class<SPElement>[] getInputClasses() {
    return new Class[]{ SPDoubleArray.class };
  }

  public Class<SPElement>[] getOutputClasses() {
    Class[] ret = new Class[fe.nFeatureTypes()];
    Arrays.fill(ret, SPDoubleArray.class);
    return ret;
  }

}