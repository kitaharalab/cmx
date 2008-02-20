package jp.crestmuse.cmx.amusaj.filewrappers;
import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.math.*;
import jp.crestmuse.cmx.misc.*;
import org.w3c.dom.*;
import java.nio.*;
import java.util.*;

public class Peaks extends NodeInterface 
  implements TimeSeriesCompatible<PeakSet>  {
  private int nFrames;
  private int timeunit;
  private int bytesize;
  private java.util.Queue<PeakSet> queue;
  private QueueWrapper qwrap;
  private DoubleArrayFactory factory;
  
  Peaks(Node node) {
    super(node);
    factory = DoubleArrayFactory.getFactory();
    nFrames = getAttributeInt("frames");
    timeunit = getAttributeInt("timeunit");
    ByteBuffer buff = ByteBuffer.wrap(Base64.decode(getText()));
    queue = new LinkedList<PeakSet>();
    bytesize = 0;
    for (int n = 0; n < nFrames; n++) {
      int nPeaks = buff.getInt();
      PeakSet peakset = new PeakSet(nPeaks);
      bytesize += 4 + 4 * nPeaks;
      for (int i = 0; i < nPeaks; i++) 
        peakset.setPeak(i, buff.getFloat(), buff.getFloat(), buff.getFloat(), 
                        buff.getFloat(), buff.getFloat());
      queue.add(peakset);
    }
    qwrap = new QueueWrapper(queue, nFrames);
  }

  protected String getSupportedNodeName() {
    return "peaks";
  }

  public QueueReader<PeakSet> getQueueReader() {
    return qwrap.createReader();
  }

//  public void finalizeQueueReader() {
//    qwrap.finalizeReader();
//  }

  public int frames() {
    return nFrames;
  }

  public int bytesize() {
    return bytesize;
  }

  public int timeunit() {
    return timeunit;
  }

  public int dim() {
    throw new UnsupportedOperationException();
  }

  public void add(PeakSet peakset) {
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

  public static void addPeaksToWrapper(TimeSeriesCompatible<PeakSet> peaks, 
                                       String nodename, 
                                       CMXFileWrapper wrapper) {
    ByteBuffer buff = ByteBuffer.allocate(peaks.bytesize());
    QueueReader<PeakSet> queue = peaks.getQueueReader();
    int nFrames = peaks.frames();
    try {
      for (int n = 0; n < nFrames; n++) {
        PeakSet peakset = queue.take();
        int nPeaks = peakset.nPeaks();
        buff.putInt(nPeaks);
        for (int i = 0; i < nPeaks; i++) {
          buff.putFloat((float)peakset.freq(i));
          buff.putFloat((float)peakset.power(i));
          buff.putFloat((float)peakset.phase(i));
          buff.putFloat((float)peakset.iid(i));
          buff.putFloat((float)peakset.ipd(i));
        }
      }
    } catch (InterruptedException e) {}
    wrapper.addChild(nodename);
    Iterator<Map.Entry<String,String>> it = peaks.getAttributeIterator();
    while (it.hasNext()) {
      Map.Entry<String,String> e = it.next();
      wrapper.setAttribute(e.getKey(), e.getValue());
    }
    wrapper.setAttribute("bytesize", peaks.bytesize());
    wrapper.setAttribute("frames", peaks.frames());
    wrapper.setAttribute("timeunit", peaks.timeunit());
    wrapper.addText(Base64.encode(buff.array()));
    wrapper.returnToParent();
  }

/*
  public static void addPeaksToWrapper(java.util.Queue<PeakSet> queue, 
                                       int bytesize, int nFrames, 
                                       int timeunit, String nodename, 
                                       CMXFileWrapper wrapper) {
    ByteBuffer buff = ByteBuffer.allocate(bytesize);
    for (int n = 0; n < nFrames; n++) {
      PeakSet peakset = queue.poll();
      int nPeaks = peakset.nPeaks();
      buff.putInt(nPeaks);
      for (int i = 0; i < nPeaks; i++) {
        buff.putFloat((float)peakset.freq(i));
        buff.putFloat((float)peakset.power(i));
        buff.putFloat((float)peakset.phase(i));
        buff.putFloat((float)peakset.iid(i));
        buff.putFloat((float)peakset.ipd(i));
      }
    }
    wrapper.addChild(nodename);
    wrapper.setAttribute("bytesize", bytesize);
    wrapper.setAttribute("frames", nFrames);
    wrapper.setAttribute("timeunit", timeunit);
    wrapper.addText(Base64.encode(buff.array()));
  }
*/
}
