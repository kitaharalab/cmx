package jp.crestmuse.cmx.misc;
import jp.crestmuse.cmx.math.*;
import java.util.*;
import java.util.concurrent.*;

public class MutablePeaks {
  private int nFrames;
  private int timeunit;
  private int bytesize;
  private BlockingQueue<PeakSet> queue;
  private DoubleArrayFactory factory;

  MutablePeaks(int nFrames, int timeunit, DoubleArrayFactory factory) {
    this.factory = factory;
    this.nFrames = nFrames;
    this.timeunit = timeunit;
    bytesize = 0;
    queue = LinkedBlockingQueue<PeakSet>();
  }

  public Queue<PeakSet> getQueue() {
    return queue;
  }

  public int frames() {
    return nFrames;
  }

  public int timeunit() {
    return timeunit;
  }

  public void addPeakSet(PeakSet peakset) {
    queue.put(peakset);
  }
}
  