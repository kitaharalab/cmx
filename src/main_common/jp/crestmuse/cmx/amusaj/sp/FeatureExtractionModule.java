package jp.crestmuse.cmx.amusaj.sp;
import jp.crestmuse.cmx.amusaj.filewrappers.*;
import jp.crestmuse.cmx.math.*;
import jp.crestmuse.cmx.misc.*;

import java.util.*;

public class FeatureExtractionModule extends SPModule {

  private FeatureExtractor<DoubleArray> fe;
  private String[] types;

  public FeatureExtractionModule(FeatureExtractor<DoubleArray> fe) {
    this.fe = fe;
    types = new String[fe.nFeatureTypes()];
    for (int i = 0; i < types.length; i++)
      types[i] = fe.getFeatureType(i);
  }

  public void execute(Object[] src, TimeSeriesCompatible[] dest)
    throws InterruptedException {
    DoubleArray wav = (DoubleArray)src[0];
    int n = dest.length;
    fe.extractFeatures(wav);
    for (int i = 0; i < n; i++) {
      TimeSeriesCompatible ts = dest[i];
      DoubleArray feats = fe.getFeature(i);
      if (feats != null) {
        ts.add(feats);
        ts.setAttribute("type", fe.getFeatureType(i));
      }
    }
//    fe.nextFrame();
  }

//  protected String getParamCategory() {
//    return "feature";
//  }

  public String[] getFeatureTypes() {
    return types;
  }

  public int getChannelFor(String featureType) {
    for (int i = 0; i < types.length; i++)
      if (featureType.equals(types[i]))
	return i;
    return -1;
  }
//  public FeatureExtractor<SPDoubleArray> getFeatureExtractor() {
//    return fe;
//  }

  public Class[] getInputClasses() {
    return new Class[]{ DoubleArray.class };
  }

  public Class[] getOutputClasses() {
    Class[] ret = new Class[fe.nFeatureTypes()];
    Arrays.fill(ret, DoubleArray.class);
    return ret;
  }

}