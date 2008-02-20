package jp.crestmuse.cmx.amusaj.sp;
import jp.crestmuse.cmx.amusaj.filewrappers.*;
import jp.crestmuse.cmx.math.*;
import jp.crestmuse.cmx.misc.*;

import java.util.*;

public class FeatureExtractionModuleFromWaveform 
  implements ProducerConsumerCompatible<DoubleArray,DoubleArray> {
  
  private Map<String,Object> params = null;
  private FeatureExtractorFromWaveform fe;

  public FeatureExtractionModuleFromWaveform
  (FeatureExtractorFromWaveform fe) {
    this.fe = fe;
  }

  public int getInputChannels() {
    return 1;
  }

  public int getOutputChannels() {
    return fe.nFeatureTypes();
  }

  public TimeSeriesCompatible createOutputInstance(int nFrames, int timeunit) {
    return new MutableTimeSeries(nFrames, timeunit);
  }

  public void execute(List<QueueReader<DoubleArray>> src, 
                      List<TimeSeriesCompatible<DoubleArray>> dest) 
    throws InterruptedException {
    DoubleArray wav = src.get(0).take();
    int n = getOutputChannels();
    for (int i = 0; i < n; i++) {
      TimeSeriesCompatible ts = dest.get(i);
      ts.add(fe.extractFeatures(wav, i));
      ts.setAttribute("feattype", fe.getFeatureType(i));
    }
  }

  public void setParams(Map<String,Object> params) {
    this.params = params;
  }
}