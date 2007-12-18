package jp.crestmuse.cmx.filewrappers.amusaj;
import java.util.*;
import java.nio.*;
import org.w3c.dom.*;
import jp.crestmuse.cmx.math.*;
import jp.crestmuse.cmx.filewrappers.*;

public abstract class TimeSeriesNodeInterface extends NodeInterface 
implements TimeSeriesCompatible {

  private int dim;
  private int nFrames;
  private int timeunit;
  private Queue<DoubleArray> queue;
  private static final DoubleArrayFactory factory = 
    DoubleArrayFactory.getFactory();

  protected TimeSeriesNodeInterface(Node node) {
    super(node);
    dim = getAttributeInt("dim");
    nFrames = getAttributeInt("frames");
    timeunit = getAttributeInt("timeunit");
    ByteBuffer buff = ByteBuffer.wrap(Base64.decode(getText()));
//    ByteBuffer buff = 
//      ByteBuffer.wrap(Base64.decodeBase64(getText().getBytes()));
    queue = new LinkedList<DoubleArray>();
    for (int n = 0; n < nFrames; n++) {
      DoubleArray array = factory.createArray(dim);
      for (int i = 0; i < dim; i++)
        array.set(i, (double)buff.getFloat());
      queue.add(array);
    }
  }

  public Queue<DoubleArray> getQueue() {
    return queue;
  }

  public int dim() {
    return dim;
  }

  public int frames() {
    return nFrames;
  }

  public int timeunit() {
    return timeunit;
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
    java.util.Queue<DoubleArray> queue = ts.getQueue();
    ByteBuffer buff = ByteBuffer.allocate(dim * nFrames * 4);
    for (int n =  0; n < nFrames; n++) {
      DoubleArray array = queue.poll();
      for (int i = 0; i < dim; i++)
        buff.putFloat((float)array.get(i));
    }
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