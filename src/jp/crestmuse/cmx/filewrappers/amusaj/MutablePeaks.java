package jp.crestmuse.cmx.filewrappers.amusaj;
import jp.crestmuse.cmx.math.*;
import java.util.*;
import java.util.concurrent.*;

public class MutablePeaks implements PeaksCompatible {
  private int nFrames;
  private int timeunit;
  private int bytesize;
  private BlockingQueue<PeakSet> queue;
  private Map<String,String> attr;

  private static final DoubleArrayFactory factory = 
    DoubleArrayFactory.getFactory();

  public MutablePeaks(int nFrames, int timeunit) {
    this.nFrames = nFrames;
    this.timeunit = timeunit;
    bytesize = 0;
    queue = new LinkedBlockingQueue<PeakSet>();
    attr = new HashMap<String,String>();
  }

  public java.util.Queue<PeakSet> getQueue() {
    return queue;
  }

  public int frames() {
    return nFrames;
  }

  public int bytesize() {
    return bytesize;
  }

  public int timeunit() {
    return timeunit;
  }

  public void addPeakSet(PeakSet peakset) throws InterruptedException {
    queue.put(peakset);
    bytesize += 4 + 4 * peakset.nPeaks();
  }

  public String getAttribute(String key) {
    return attr.get(key);
  }

  public int getAttributeInt(String key) {
    return Integer.parseInt(getAttribute(key));
  }

  public double getAttributeDouble(String key) {
    return Double.parseDouble(getAttribute(key));
  }

  public void setAttribute(String key, String value) {
    attr.put(key, value);
  }

  public void setAttribute(String key, int value) {
    attr.put(key, String.valueOf(value));
  }

  public void setAttribute(String key, double value) {
    attr.put(key, String.valueOf(value));
  }

  public Iterator<Map.Entry<String,String>> getAttributeIterator() {
    return attr.entrySet().iterator();
  }
}
  