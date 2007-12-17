package jp.crestmuse.cmx.filewrappers.amusaj;
import java.util.*;
import java.nio.*;
import org.w3c.dom.*;
import org.apache.commons.codec.binary.*;
import jp.crestmuse.cmx.math.*;

public abstract class TimeSeriesNodeInterface extends NodeInterface {
  private int dim;
  private int nFrames;
  private int timeunit;
  private Queue<DoubleArray> queue;
  private static final Base64 base64 = new Base64();
  private static DoubleArrayFactory factory;

  protected TimeSeriesNodeInterface(Node node, 
                                    DoubleArrayFactory factory) {
    super(node);
    this.factory = factory;
    dim = getAttributeInt("dim");
    nFrames = getAttributeInt("frames");
    timeunit = getAttributeInt("timeunit");
    ByteBuffer buff = 
      ByteBuffer.wrap(base64.decodeBase64(getText().getBytes()));
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
    String s = new String(base64.encodeBase64Chunked(buff.array()));
    wrapper.addChild(nodename);
    wrapper.setAttribute("dim", dim);
    wrapper.setAttribute("frames", nFrames);
    wrapper.setAttribute("timeunit", timeunit);
    wrapper.addText(s);
  }

}