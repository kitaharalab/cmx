package jp.crestmuse.cmx.amusaj.sp;
import jp.crestmuse.cmx.amusaj.filewrappers.*;
import jp.crestmuse.cmx.math.*;
import jp.crestmuse.cmx.misc.*;

import java.util.*;

//public class FeatureExtractionModule extends SPModule<SPDoubleArray,SPDoubleArray>{
//  implements ProducerConsumerCompatible<DoubleArray,DoubleArray> {
public class FeatureExtractionModule extends SPModule {
//  private Map<String,Object> params = null;
  private FeatureExtractor fe;

  public FeatureExtractionModule(FeatureExtractor fe) {
    this.fe = fe;
  }
/*
  public int getInputChannels() {
    return 1;
  }

  public int getOutputChannels() {
    return fe.nFeatureTypes();
  }

//  public TimeSeriesCompatible createOutputInstance(int nFrames, int timeunit) {
//    return new MutableDoubleArrayTimeSeries(nFrames, timeunit);
//  }

  public void execute(List<QueueReader<SPDoubleArray>> src, 
                      List<TimeSeriesCompatible<SPDoubleArray>> dest) 
    throws InterruptedException {
    SPDoubleArray wav = src.get(0).take();
    int n = getOutputChannels();
    fe.extractFeatures(wav);
    for (int i = 0; i < n; i++) {
      TimeSeriesCompatible ts = dest.get(i);
      ts.add(new SPDoubleArray(fe.getFeature(i), wav.hasNext()));
      ts.setAttribute("type", fe.getFeatureType(i));
    }
    fe.nextFrame();
  }
*/
public void execute(SPElement[] src, TimeSeriesCompatible<SPElement>[] dest)
    throws InterruptedException {
  SPDoubleArray wav = (SPDoubleArray)src[0];
  int n = dest.length;
  fe.extractFeatures(wav);
  for (int i = 0; i < n; i++) {
    TimeSeriesCompatible ts = dest[i];
    ts.add(new SPDoubleArray(fe.getFeature(i), wav.hasNext()));
    ts.setAttribute("type", fe.getFeatureType(i));
  }
  fe.nextFrame();
}

public Class<SPElement>[] getInputClasses() {
  return new Class[]{ SPDoubleArray.class };
}

public Class<SPElement>[] getOutputClasses() {
  Class[] ret = new Class[fe.nFeatureTypes()];
  Arrays.fill(ret, SPDoubleArray.class);
  return ret;
}

//  public void setParams(Map<String,Object> params) {
//    this.params = params;
//  }
}