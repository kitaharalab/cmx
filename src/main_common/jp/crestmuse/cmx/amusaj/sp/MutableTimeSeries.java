package jp.crestmuse.cmx.amusaj.sp;
import jp.crestmuse.cmx.amusaj.sp.*;
import jp.crestmuse.cmx.misc.*;
import jp.crestmuse.cmx.math.*;
import java.util.*;
import java.util.concurrent.*;

public class MutableTimeSeries<D> implements TimeSeriesCompatible<D> {
  private int nFrames;
  private int timeunit;
  private int dim = -1;
  private BlockingQueue<D> queue;
  private QueueWrapper<D> qwrap;
  private Map<String,String> attr;
  private boolean complete = false;

  public MutableTimeSeries() {
//  public MutableTimeSeries(int nFrames, int timeunit) {
//    this.nFrames = nFrames;
//    this.timeunit = timeunit;
    queue = new LinkedBlockingQueue<D>();
    qwrap = new QueueWrapper<D>(queue);
    attr = new HashMap<String,String>();
  }

  public QueueReader<D> getQueueReader() {
    return qwrap.createReader();
  }

  public int dim() {
    return dim;
  }

//  public int frames() {
//    return queue.size();
//    return nFrames;
//  }

//  public int timeunit() {
//    return timeunit;
//  }

  public void add(D d) throws InterruptedException {
    if (d instanceof Array) {
      int dim1 = ((Array)d).length();
      if (dim == dim1) {
        queue.put(d);
      } else if (dim == -1) {
        dim = dim1;
        queue.put(d);
      } else {
        throw new IllegalStateException("unmatch dimension");
      }
    } else {
      queue.put(d);
    }
    /*
    if (!d.hasNext())
      complete =true;
    */
    if(d instanceof SPTerminator) complete = true;
  }

  public boolean isComplete() {
    return complete;
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
