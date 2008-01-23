package jp.crestmuse.cmx.amusaj.filewrappers;
import jp.crestmuse.cmx.misc.*;
import jp.crestmuse.cmx.math.*;
import java.util.*;
import java.util.concurrent.*;

/********************************************************************
 *オブジェクト生成後に値を自由に変更できる時系列オブジェクトです. 
 *********************************************************************/
public class MutableTimeSeries implements TimeSeriesCompatible {
  private int dim = -1;
  private int nFrames;
  private int timeunit;
  private BlockingQueue<DoubleArray> queue;
  private QueueWrapper<DoubleArray> qwrap;
  private Map<String,String> attr;

  private static final DoubleArrayFactory factory = 
    DoubleArrayFactory.getFactory();

  public MutableTimeSeries(int nFrames, int timeunit) {
    this.nFrames = nFrames;
    this.timeunit = timeunit;
    queue = new LinkedBlockingQueue<DoubleArray>();
    qwrap = new QueueWrapper(queue, nFrames);
    attr = new HashMap<String,String>();
  }

  public QueueReader<DoubleArray> getQueueReader() {
    return qwrap.createReader();
  }

  public int frames() {
    return nFrames;
  }

  public int timeunit() {
    return timeunit;
  }

  public int dim() {
    return dim;
  }

  public void add(DoubleArray array) throws InterruptedException {
    if (dim == -1) {
      dim = array.length();
      queue.put(array);
    } else if (dim == array.length()) {
      queue.put(array);
    } else {
      throw new IllegalStateException("unmatch dimension");
    }
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
