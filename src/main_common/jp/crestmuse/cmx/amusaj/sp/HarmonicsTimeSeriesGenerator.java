package jp.crestmuse.cmx.amusaj.sp;
import java.util.LinkedList;

import jp.crestmuse.cmx.amusaj.filewrappers.PeakSet;

public class HarmonicsTimeSeriesGenerator extends SPModule {
  private int nFrames;
  private int nHarmonics;
  private boolean setParams = false;
  private LinkedList<HTS> hlist = new LinkedList<HTS>();

  private class HTS {
    HarmonicsTimeSeries h;
    int next;
    HTS() {
      h = new HarmonicsTimeSeries(nFrames, nHarmonics);
      next = 0;
    }
  }

  private void setParams() {
    AmusaParameterSet params = AmusaParameterSet.getInstance();
    nHarmonics = params.getParamInt("harmonics", "NUM_OF_HARMONICS");
    nFrames = params.getParamInt("harmonics", "NUM_OF_FRAMES");
    setParams = true;
//    setParams();
  }

  public Class[] getInputClasses() {
    return new Class[] { PeakSet.class };
  }

  public Class[] getOutputClasses() {
    return new Class[] { HarmonicsTimeSeries.class };
  }

  public void execute(Object[] src, TimeSeriesCompatible[] dest)
    throws InterruptedException {
    if (!setParams) setParams();
    PeakSet peakset = (PeakSet)src[0];
    hlist.add(new HTS());
    for (HTS h : hlist) {
      h.h.set(h.next, peakset);
      h.next++;
    }
//    System.err.println(nFrames);
//    System.err.println(hlist.getFirst().next);
    if (hlist.getFirst().next >= nFrames) 
      dest[0].add(hlist.removeFirst().h);
  }
}
      