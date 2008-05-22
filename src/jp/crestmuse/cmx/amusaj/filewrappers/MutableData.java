package jp.crestmuse.cmx.amusaj.filewrappers;
import jp.crestmuse.cmx.misc.*;
import jp.crestmuse.cmx.math.*;
import java.util.*;
import java.util.concurrent.*;

public abstract class MutableData<D> implements TimeSeriesCompatible<D> {
  private int nFrames;
  private int timeunit;
  BlockingQueue<D> queue;
  private QueueWrapper<D> qwrap;
  private Map<String,String> attr;

  MutableData(int nFrames, int timeunit) {
    this.nFrames = nFrames;
    this.timeunit = timeunit;
    queue = new LinkedBlockingQueue<D>();
    qwrap = new QueueWrapper<D>(queue, nFrames);
    attr = new HashMap<String,String>();
  }

  public QueueReader<D> getQueueReader() {
    return qwrap.createReader();
  }

  public int frames() {
    return nFrames;
  }

  public int timeunit() {
    return timeunit;
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