package jp.crestmuse.cmx.amusaj.sp;
import jp.crestmuse.cmx.amusaj.filewrappers.*;
import jp.crestmuse.cmx.math.*;
import jp.crestmuse.cmx.misc.*;

import java.util.*;

public class FeatureExtractionModule extends SPModule<DoubleArray,DoubleArray>{
//  implements ProducerConsumerCompatible<DoubleArray,DoubleArray> {
  
//  private Map<String,Object> params = null;
  private FeatureExtractor fe;

  public FeatureExtractionModule(FeatureExtractor fe) {
    this.fe = fe;
  }

  public int getInputChannels() {
    return 1;
  }

  public int getOutputChannels() {
    return fe.nFeatureTypes();
  }

//  public TimeSeriesCompatible createOutputInstance(int nFrames, int timeunit) {
//    return new MutableDoubleArrayTimeSeries(nFrames, timeunit);
//  }

  public void execute(List<QueueReader<DoubleArray>> src, 
                      List<TimeSeriesCompatible<DoubleArray>> dest) 
    throws InterruptedException {
    DoubleArray wav = src.get(0).take();
    int n = getOutputChannels();
    fe.extractFeatures(wav);
    for (int i = 0; i < n; i++) {
      TimeSeriesCompatible ts = dest.get(i);
      ts.add(fe.getFeature(i));
      ts.setAttribute("type", fe.getFeatureType(i));
    }
    fe.nextFrame();
  }

//  public void setParams(Map<String,Object> params) {
//    this.params = params;
//  }
}