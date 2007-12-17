package jp.crestmuse.cmx.misc;
import jp.crestmuse.cmx.math.*;
import java.util.*;
import java.util.concurrent.*;

public class MutablePeaks {
  private int nFrames;
  private int timeunit;
  private int bytesize;
  private BlockingQueue<PeakSet> queue;
  private static final DoubleArrayFactory factory = 
    DoubleArrayFactory.getFactory();

  MutablePeaks(int nFrames, int timeunit) {
    this.nFrames = nFrames;
    this.timeunit = timeunit;
    bytesize = 0;
    queue = new LinkedBlockingQueue<PeakSet>();
  }

  public java.util.Queue<PeakSet> getQueue() {
    return queue;
  }

  public int frames() {
    return nFrames;
  }

  public int timeunit() {
    return timeunit;
  }

  public void addPeakSet(PeakSet peakset) throws InterruptedException {
    queue.put(peakset);
  }
}
  