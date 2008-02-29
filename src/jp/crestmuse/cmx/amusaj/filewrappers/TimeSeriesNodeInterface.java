package jp.crestmuse.cmx.amusaj.filewrappers;
import java.util.*;
import java.nio.*;
import org.w3c.dom.*;
import jp.crestmuse.cmx.math.*;
import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.misc.*;

public abstract class TimeSeriesNodeInterface extends NodeInterface 
implements TimeSeriesCompatible<DoubleArray> {

  private int dim;
  private int nFrames;
  private int timeunit;
  private java.util.Queue<DoubleArray> queue;
  private QueueWrapper<DoubleArray> qwrap;
  private static final DoubleArrayFactory factory = 
    DoubleArrayFactory.getFactory();

  protected TimeSeriesNodeInterface(Node node) {
    super(node);
    dim = getAttributeInt("dim");
    nFrames = getAttributeInt("frames");
    if (hasAttribute("timeunit"))
      timeunit = getAttributeInt("timeunit");
    ByteBuffer buff = ByteBuffer.wrap(Base64.decode(getText()));
//    ByteBuffer buff = 
//      ByteBuffer.wrap(Base64.decodeBase64(getText().getBytes()));
    queue = new LinkedList<DoubleArray>();
    qwrap = new QueueWrapper(queue, nFrames);
    for (int n = 0; n < nFrames; n++) {
      DoubleArray array = factory.createArray(dim);
      for (int i = 0; i < dim; i++)
        array.set(i, (double)buff.getFloat());
      queue.add(array);
    }
  }

  public QueueReader<DoubleArray> getQueueReader() {
    return qwrap.createReader();
  }
  
//  public void finalizeQueueReader() {
//    qwrap.finalizeReader();
//  }

  public int dim() {
    return dim;
  }

  public int frames() {
    return nFrames;
  }

  public int timeunit() {
    return timeunit;
  }

  public int bytesize() {
    return 4 * dim;
  }

  public void add(DoubleArray array) {
    throw new UnsupportedOperationException();
  }

  public void setAttribute(String key, String value) {
    throw new UnsupportedOperationException();
  }

  public void setAttribute(String key, int value) {
    throw new UnsupportedOperationException();
  }

  public void setAttribute(String key, double value) {
    throw new UnsupportedOperationException();
  }

  public Iterator<Map.Entry<String,String>> getAttributeIterator() {
    return new AttrIterator(node().getAttributes());
  }

  public static void addTimeSeriesToWrapper(TimeSeriesCompatible ts, 
                                            String nodename, 
                                            CMXFileWrapper wrapper) {
    int dim = ts.dim();
    int nFrames = ts.frames();
    QueueReader<DoubleArray> queue = ts.getQueueReader();
    ByteBuffer buff = ByteBuffer.allocate(dim * nFrames * 4);
    try {
      for (int n =  0; n < nFrames; n++) {
        DoubleArray array = queue.take();
        for (int i = 0; i < dim; i++)
          buff.putFloat((float)array.get(i));
      }
    } catch (InterruptedException e) {}
    String s = Base64.encode(buff.array());
    wrapper.addChild(nodename);
    Iterator<Map.Entry<String,String>> it = ts.getAttributeIterator();
    while (it.hasNext()) {
      Map.Entry<String,String> e = it.next();
      wrapper.setAttribute(e.getKey(), e.getValue());
    }
    wrapper.setAttribute("dim", dim);
    wrapper.setAttribute("frames", nFrames);
    wrapper.setAttribute("timeunit", ts.timeunit());
    wrapper.addText(s);
    wrapper.returnToParent();
  }

/*
  public static void addTimeSeriesToWrapper(Queue<DoubleArray> queue, 
                                            int dim, int nFrames, 
                                            int timeunit, 
                                            String nodename, 
                                            CMXFileWrapper wrapper) {
    ByteBuffer buff = ByteBuffer.allocate(dim * nFrames * 4);
    for (int n =  0; n < nFrames; n++) {
      DoubleArray array = queue.poll();
      for (int i = 0; i < dim; i++)
        buff.putFloat((float)array.get(i));
    }
    String s = Base64.encode(buff.array());
    wrapper.addChild(nodename);
    wrapper.setAttribute("dim", dim);
    wrapper.setAttribute("frames", nFrames);
    wrapper.setAttribute("timeunit", timeunit);
    wrapper.addText(s);
  }
*/


  

}
